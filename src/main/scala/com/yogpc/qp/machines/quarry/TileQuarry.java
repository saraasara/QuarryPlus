package com.yogpc.qp.machines.quarry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.BreakResult;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.EnchantmentLevel;
import com.yogpc.qp.machines.MachineStorage;
import com.yogpc.qp.machines.PowerTile;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidDrainable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.Nullable;

public class TileQuarry extends PowerTile implements BlockEntityClientSerializable, CheckerLog, MachineStorage.HasStorage, EnchantmentLevel.HasEnchantments {
    private static final Marker MARKER = MarkerManager.getMarker("TileQuarry");
    private static final EnchantmentRestriction RESTRICTION = EnchantmentRestriction.builder()
        .add(Enchantments.EFFICIENCY)
        .add(Enchantments.UNBREAKING)
        .add(Enchantments.FORTUNE)
        .add(Enchantments.SILK_TOUCH)
        .build();
    @Nullable
    public Target target;
    public QuarryState state = QuarryState.FINISHED;
    @Nullable
    private Area area;
    // May be unmodifiable
    private List<EnchantmentLevel> enchantments = new ArrayList<>();
    public MachineStorage storage = new MachineStorage();
    public double headX, headY, headZ;
    private boolean bedrockRemove = false;

    public TileQuarry(BlockPos pos, BlockState state) {
        super(QuarryPlus.ModObjects.QUARRY_TYPE, pos, state, 10000 * ONE_FE);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        if (target != null)
            nbt.put("target", target.toNbt());
        nbt.putString("state", state.name());
        if (area != null)
            nbt.put("area", area.toNBT());
        {
            var enchantments = new NbtCompound();
            this.enchantments.forEach(e ->
                enchantments.putInt(Objects.requireNonNull(e.enchantmentID(), "Invalid enchantment. " + e.enchantment()).toString(), e.level()));
            nbt.put("enchantments", enchantments);
        }
        nbt.putDouble("headX", headX);
        nbt.putDouble("headY", headY);
        nbt.putDouble("headZ", headZ);
        nbt.put("storage", storage.toNbt());
        nbt.putBoolean("bedrockRemove", bedrockRemove);
        return super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        target = nbt.contains("target") ? Target.fromNbt(nbt.getCompound("target")) : null;
        state = QuarryState.valueOf(nbt.getString("state"));
        area = Area.fromNBT(nbt.getCompound("area")).orElse(null);
        {
            var enchantments = nbt.getCompound("enchantments");
            this.enchantments = enchantments.getKeys().stream()
                .map(k -> Pair.of(Registry.ENCHANTMENT.get(new Identifier(k)), enchantments.getInt(k)))
                .filter(p -> p.getKey() != null)
                .map(EnchantmentLevel::new)
                .toList();
        }
        headX = nbt.getDouble("headX");
        headY = nbt.getDouble("headY");
        headZ = nbt.getDouble("headZ");
        storage.readNbt(nbt.getCompound("storage"));
        bedrockRemove = nbt.getBoolean("bedrockRemove");
    }

    @Override
    public final NbtCompound toClientTag(NbtCompound tag) {
        if (area != null)
            tag.put("area", area.toNBT());
        tag.putString("state", state.name());
        tag.putDouble("headX", headX);
        tag.putDouble("headY", headY);
        tag.putDouble("headZ", headZ);
        return tag;
    }

    @Override
    public final void fromClientTag(NbtCompound tag) {
        area = Area.fromNBT(tag.getCompound("area")).orElse(null);
        state = QuarryState.valueOf(tag.getString("state"));
        headX = tag.getDouble("headX");
        headY = tag.getDouble("headY");
        headZ = tag.getDouble("headZ");
    }

    public void setArea(@Nullable Area area) {
        this.area = area;
        QuarryPlus.LOGGER.debug(MARKER, "Quarry({}) Area changed to {}.", pos, area);
        if (area != null) {
            headX = area.minX();
            headY = area.minY();
            headZ = area.minZ();
        }
    }

    @Nullable
    public Area getArea() {
        return area;
    }

    public void setState(QuarryState quarryState, BlockState blockState) {
        if (this.state != quarryState) {
            this.state = quarryState;
            sync();
            if (world != null) {
                world.setBlockState(pos, blockState.with(BlockQuarry.WORKING, quarryState.isWorking), Block.NOTIFY_LISTENERS);
                if (!world.isClient && !quarryState.isWorking) {
                    logUsage();
                }
            }
            QuarryPlus.LOGGER.debug(MARKER, "Quarry({}) State changed to {}.", pos, quarryState);
        }
    }

    public void setBedrockRemove(boolean bedrockRemove) {
        this.bedrockRemove = bedrockRemove;
    }

    public ServerWorld getTargetWorld() {
        return (ServerWorld) getWorld();
    }

    public static void tick(World world, BlockPos pos, BlockState state, TileQuarry quarry) {
        // In server world.
        quarry.state.tick(world, pos, state, quarry);
    }

    public BreakResult breakBlock(BlockPos targetPos) {
        return breakBlock(targetPos, true);
    }

    public BreakResult breakBlock(BlockPos targetPos, boolean requireEnergy) {
        var targetWorld = getTargetWorld();
        // Gather Drops
        if (targetPos.getX() % 3 == 0 && targetPos.getZ() % 3 == 0) {
            targetWorld.getEntitiesByClass(ItemEntity.class, new Box(targetPos).expand(5), Predicate.not(i -> i.getStack().isEmpty()))
                .forEach(i -> {
                    storage.addItem(i.getStack());
                    i.kill();
                });
        }
        // Check breakable
        var blockState = targetWorld.getBlockState(targetPos);
        if (blockState.isAir() || !canBreak(targetWorld, targetPos, blockState)) {
            return BreakResult.SKIPPED;
        }
        // Fluid remove
        // Fluid at near
        assert area != null;
        {
            boolean flagMinX = targetPos.getX() - 1 == area.minX();
            boolean flagMaxX = targetPos.getX() + 1 == area.maxX();
            boolean flagMinZ = targetPos.getZ() - 1 == area.minZ();
            boolean flagMaxZ = targetPos.getZ() + 1 == area.maxZ();
            if (flagMinX) {
                if (removeFluidAtEdge(targetWorld, new BlockPos(area.minX(), targetPos.getY(), targetPos.getZ()), requireEnergy)) {
                    return BreakResult.NOT_ENOUGH_ENERGY;
                }
            }
            if (flagMaxX) {
                if (removeFluidAtEdge(targetWorld, new BlockPos(area.maxX(), targetPos.getY(), targetPos.getZ()), requireEnergy)) {
                    return BreakResult.NOT_ENOUGH_ENERGY;
                }
            }
            if (flagMinZ) {
                if (removeFluidAtEdge(targetWorld, new BlockPos(targetPos.getX(), targetPos.getY(), area.minZ()), requireEnergy)) {
                    return BreakResult.NOT_ENOUGH_ENERGY;
                }
            }
            if (flagMaxZ) {
                if (removeFluidAtEdge(targetWorld, new BlockPos(targetPos.getX(), targetPos.getY(), area.maxZ()), requireEnergy)) {
                    return BreakResult.NOT_ENOUGH_ENERGY;
                }
            }
            if (flagMinX && flagMinZ) {
                if (removeFluidAtEdge(targetWorld, new BlockPos(area.minX(), targetPos.getY(), area.minZ()), requireEnergy)) {
                    return BreakResult.NOT_ENOUGH_ENERGY;
                }
            }
            if (flagMinX && flagMaxZ) {
                if (removeFluidAtEdge(targetWorld, new BlockPos(area.minX(), targetPos.getY(), area.maxZ()), requireEnergy)) {
                    return BreakResult.NOT_ENOUGH_ENERGY;
                }
            }
            if (flagMaxX && flagMinZ) {
                if (removeFluidAtEdge(targetWorld, new BlockPos(area.maxX(), targetPos.getY(), area.minZ()), requireEnergy)) {
                    return BreakResult.NOT_ENOUGH_ENERGY;
                }
            }
            if (flagMaxX && flagMaxZ) {
                if (removeFluidAtEdge(targetWorld, new BlockPos(area.maxX(), targetPos.getY(), area.maxZ()), requireEnergy)) {
                    return BreakResult.NOT_ENOUGH_ENERGY;
                }
            }
        }

        // Fluid at target pos
        var fluidState = targetWorld.getFluidState(targetPos);
        if (!fluidState.isEmpty() && blockState.getBlock() instanceof FluidDrainable fluidBlock) {
            if (requireEnergy && !useEnergy(Constants.BREAK_BLOCK_FLUID, Reason.REMOVE_FLUID, unbreakingLevel())) {
                return BreakResult.NOT_ENOUGH_ENERGY;
            }
            var bucketItem = fluidBlock.tryDrainFluid(targetWorld, targetPos, blockState);
            this.storage.addFluid(bucketItem);
            blockState = targetWorld.getBlockState(targetPos); // Update reference, because fluid state is changed.
        }

        // Break block
        var hardness = fluidState.isEmpty() ? blockState.getHardness(targetWorld, targetPos) : 5;
        if (requireEnergy && !useEnergy(Constants.getBreakEnergy(hardness), Reason.BREAK_BLOCK, unbreakingLevel())) {
            return BreakResult.NOT_ENOUGH_ENERGY;
        }
        var drops = Block.getDroppedStacks(blockState, targetWorld, targetPos, targetWorld.getBlockEntity(targetPos), null, getPickaxe());
        drops.forEach(this.storage::addItem);
        targetWorld.setBlockState(targetPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
        var sound = blockState.getSoundGroup();
        if (requireEnergy)
            targetWorld.playSound(null, targetPos, sound.getBreakSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 4F, sound.getPitch() * 0.8F);

        return BreakResult.SUCCESS;
    }

    /**
     * @return {@code true} if quarry failed to remove fluid due to energy shortage.
     */
    private boolean removeFluidAtEdge(ServerWorld world, BlockPos pos, boolean requireEnergy) {
        var state = world.getBlockState(pos);
        var fluidState = world.getFluidState(pos);
        if (!fluidState.isEmpty() && state.getBlock() instanceof FluidDrainable fluidBlock) {
            if (requireEnergy && !useEnergy(Constants.BREAK_BLOCK_FLUID, Reason.REMOVE_FLUID, unbreakingLevel())) {
                return true;
            }
            var bucketItem = fluidBlock.tryDrainFluid(world, pos, state);
            this.storage.addFluid(bucketItem);
            if (world.isAir(pos)) {
                world.setBlockState(pos, QuarryPlus.ModObjects.BLOCK_FRAME.getDammingState());
            }
        }
        return false;
    }

    public void setEnchantments(Map<Enchantment, Integer> enchantments) {
        this.enchantments = RESTRICTION.filterMap(enchantments).entrySet().stream()
            .map(EnchantmentLevel::new)
            .toList();
    }

    public void setTileDataFromItem(@Nullable NbtCompound tileData) {
        if (tileData == null) return;
        setBedrockRemove(tileData.getBoolean("bedrockRemove"));
    }

    public NbtCompound getTileDataForItem() {
        var tag = new NbtCompound();
        if (bedrockRemove) tag.putBoolean("bedrockRemove", true);
        return tag;
    }

    private ItemStack getPickaxe() {
        var stack = new ItemStack(Items.NETHERITE_PICKAXE);
        enchantments.forEach(e -> stack.addEnchantment(e.enchantment(), e.level()));
        return stack;
    }

    double headSpeed() {
        final int defaultSpeed = -7;
        return enchantments.stream()
            .filter(e -> e.enchantment() == Enchantments.EFFICIENCY)
            .mapToInt(EnchantmentLevel::level)
            .mapToDouble(l -> Math.pow(2, defaultSpeed + l * (1 - defaultSpeed) / 5d))
            .findFirst()
            .orElse(Math.pow(2, defaultSpeed));
    }

    int unbreakingLevel() {
        return enchantments.stream().filter(e -> e.enchantment() == Enchantments.UNBREAKING)
            .mapToInt(EnchantmentLevel::level).findFirst().orElse(0);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canBreak(World targetWorld, BlockPos targetPos, BlockState state) {
        var unbreakable = state.getHardness(targetWorld, targetPos) < 0;
        if (unbreakable && bedrockRemove && state.getBlock() == Blocks.BEDROCK) {
            if (targetWorld.getRegistryKey().equals(World.NETHER)) {
                return (0 < targetPos.getY() && targetPos.getY() < 5) || (122 < targetPos.getY() && targetPos.getY() < 127);
            }
            return 0 < targetPos.getY() && targetPos.getY() < 5;
        }
        return !unbreakable;
    }

    @Override
    public List<? extends Text> getDebugLogs() {
        return Stream.of(
            "%sArea:%s %s".formatted(Formatting.GREEN, Formatting.RESET, area),
            "%sTarget:%s %s".formatted(Formatting.GREEN, Formatting.RESET, target),
            "%sState:%s %s".formatted(Formatting.GREEN, Formatting.RESET, state),
            "%sRemoveBedrock:%s %s".formatted(Formatting.GREEN, Formatting.RESET, bedrockRemove),
            "%sHead:%s (%f, %f, %f)".formatted(Formatting.GREEN, Formatting.RESET, headX, headY, headZ),
            "%sEnergy:%s %f FE (%d)".formatted(Formatting.GREEN, Formatting.RESET, getEnergy() / (double) PowerTile.ONE_FE, getEnergy())
        ).map(LiteralText::new).toList();
    }

    @Override
    public MachineStorage getStorage() {
        return this.storage;
    }

    @Override
    public List<EnchantmentLevel> getEnchantments() {
        return Collections.unmodifiableList(enchantments);
    }
}

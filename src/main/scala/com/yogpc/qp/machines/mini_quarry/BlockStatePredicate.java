package com.yogpc.qp.machines.mini_quarry;

import java.util.Locale;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.yogpc.qp.QuarryPlus;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.apache.logging.log4j.Logger;

interface BlockStatePredicate {
    boolean test(BlockState state, BlockGetter level, BlockPos pos);

    static BlockStatePredicate air() {
        return Air.INSTANCE;
    }

    static BlockStatePredicate fluid() {
        return Fluid.INSTANCE;
    }

    static BlockStatePredicate name(ResourceLocation location) {
        return new Name(location);
    }

    static BlockStatePredicate tag(ResourceLocation location) {
        return new Tag(location);
    }

    static BlockStatePredicate predicateString(String location) {
        return new VanillaBlockPredicate(location);
    }

    /**
     * Create predicate from nbt tag. Tag must contain "type" to specify type of the predicate.
     * Also, it should contain property for each predicate, such as block name and tag name.
     */
    static BlockStatePredicate fromTag(CompoundTag tag) {
        var type = tag.getString("type");
        return switch (type) {
            case "air" -> air();
            case "fluid" -> fluid();
            case "name" -> name(new ResourceLocation(tag.getString("location")));
            case "tag" -> tag(new ResourceLocation(tag.getString("location")));
            case "vanilla" -> predicateString(tag.getString("predicate"));
            default -> throw new IllegalArgumentException("invalid type name: %s, got from %s".formatted(type, tag));
        };
    }

    CompoundTag toTag();

    final class Air implements BlockStatePredicate {
        private static final Air INSTANCE = new Air();

        private Air() {
        }

        @Override
        public boolean test(BlockState state, BlockGetter level, BlockPos pos) {
            return state.isAir();
        }

        @Override
        public CompoundTag toTag() {
            var tag = new CompoundTag();
            tag.putString("type", toString().toLowerCase(Locale.ROOT));
            return tag;
        }

        @Override
        public String toString() {
            return "Air";
        }
    }

    final class Fluid implements BlockStatePredicate {
        private static final Fluid INSTANCE = new Fluid();

        private Fluid() {
        }

        @Override
        public boolean test(BlockState state, BlockGetter level, BlockPos pos) {
            return !state.getFluidState().isEmpty();
        }

        @Override
        public CompoundTag toTag() {
            var tag = new CompoundTag();
            tag.putString("type", toString().toLowerCase(Locale.ROOT));
            return tag;
        }

        @Override
        public String toString() {
            return "Fluid";
        }
    }

    final class Name implements BlockStatePredicate {
        private final ResourceLocation location;

        private Name(ResourceLocation location) {
            this.location = location;
        }

        @Override
        public boolean test(BlockState state, BlockGetter level, BlockPos pos) {
            return location.equals(state.getBlock().getRegistryName());
        }

        @Override
        public CompoundTag toTag() {
            var tag = new CompoundTag();
            tag.putString("type", "name");
            tag.putString("location", location.toString());
            return tag;
        }

        @Override
        public String toString() {
            return "Name{" +
                "location=" + location +
                '}';
        }
    }

    final class Tag implements BlockStatePredicate {
        private final net.minecraft.tags.Tag<Block> tag;
        private final ResourceLocation location;

        private Tag(ResourceLocation tagName) {
            this.location = tagName;
            this.tag = BlockTags.getAllTags().getTag(tagName);
        }

        @Override
        public boolean test(BlockState state, BlockGetter level, BlockPos pos) {
            return tag.contains(state.getBlock());
        }

        @Override
        public CompoundTag toTag() {
            var tag = new CompoundTag();
            tag.putString("type", "tag");
            tag.putString("location", location.toString());
            return tag;
        }

        @Override
        public String toString() {
            return "Tag{" + location + "}";
        }
    }

    final class VanillaBlockPredicate implements BlockStatePredicate {
        private static final Logger LOGGER = QuarryPlus.getLogger(VanillaBlockPredicate.class);
        private final String blockPredicate;
        @Nullable
        private BlockPredicateArgument.Result argument;

        public VanillaBlockPredicate(String blockPredicate) {
            this.blockPredicate = blockPredicate;
            try {
                argument = BlockPredicateArgument.blockPredicate().parse(new StringReader(blockPredicate));
            } catch (CommandSyntaxException e) {
                LOGGER.warn("Caught invalid BlockState predicate.", e);
                argument = null;
            }
        }

        @Override
        public boolean test(BlockState state, BlockGetter blockGetter, BlockPos pos) {
            if (argument != null && (blockGetter instanceof Level level)) {
                try {
                    return argument.create(level.getTagManager())
                        .test(new BlockInWorld(level, pos, true));
                } catch (CommandSyntaxException e) {
                    LOGGER.warn("Caught error in creating predicate.", e);
                    return false;
                }
            } else {
                return false;
            }
        }

        @Override
        public CompoundTag toTag() {
            var tag = new CompoundTag();
            tag.putString("type", "vanilla");
            tag.putString("predicate", blockPredicate);
            return tag;
        }

        @Override
        public String toString() {
            return "VanillaBlockPredicate{" +
                "blockPredicate='" + blockPredicate + '\'' +
                "valid=" + (argument != null) +
                '}';
        }
    }
}

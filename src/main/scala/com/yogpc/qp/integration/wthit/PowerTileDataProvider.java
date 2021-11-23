package com.yogpc.qp.integration.wthit;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.PowerTile;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

final class PowerTileDataProvider implements IServerDataProvider<PowerTile>, IBlockComponentProvider {
    /**
     * In this method, I will send these data.
     * <ul>
     *     <li>Current Energy</li>
     *     <li>Max Energy</li>
     * </ul>
     */
    @Override
    public void appendServerData(CompoundTag data, ServerPlayer player, Level world, PowerTile powerTile) {
        data.putLong("currentEnergy", powerTile.getEnergy());
        data.putLong("maxEnergy", powerTile.getMaxEnergy());
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        var tag = accessor.getServerData();
        long actualCurrentEnergy = tag.contains("currentEnergy", Tag.TAG_LONG) ? tag.getLong("currentEnergy") : 0;
        long currentEnergy = QuarryPlus.config.common.debug ? actualCurrentEnergy : Math.max(0L, actualCurrentEnergy);
        long maxEnergy = tag.contains("maxEnergy", Tag.TAG_LONG) ? tag.getLong("maxEnergy") : 0;
        String percent = String.format("Energy: %.1f%%", 100d * currentEnergy / maxEnergy);
        String energy = String.format("%d / %d FE", currentEnergy / PowerTile.ONE_FE, maxEnergy / PowerTile.ONE_FE);
        tooltip.add(new TextComponent(percent));
        tooltip.add(new TextComponent(energy));
    }
}

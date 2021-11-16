package com.yogpc.qp;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;

@Config(name = QuarryPlus.modID)
public class QuarryConfig implements ConfigData {
    private static class Constant {
        @ConfigEntry.Gui.Excluded
        private static final String COMMON_CATEGORY = "default";
        @ConfigEntry.Gui.Excluded
        private static final String COMMON_POWER_CATEGORY = "common.power";
    }

    @ConfigEntry.Gui.CollapsibleObject
    public Common common = new Common();
    @ConfigEntry.Gui.CollapsibleObject
    public Power power = new Power();

    public static class Common {
        @ConfigEntry.Category(Constant.COMMON_CATEGORY)
        public boolean debug = FabricLauncherBase.getLauncher() == null || FabricLoader.getInstance().isDevelopmentEnvironment();
        @ConfigEntry.Category(Constant.COMMON_CATEGORY)
        @ConfigEntry.BoundedDiscrete(min = -128, max = 512)
        public int netherTop = FabricLauncherBase.getLauncher() == null || FabricLoader.getInstance().isDevelopmentEnvironment() ? 128 : 127;
        @ConfigEntry.Category(Constant.COMMON_CATEGORY)
        @ConfigEntry.Gui.RequiresRestart
        public boolean noEnergy = false;
        @ConfigEntry.Category(Constant.COMMON_CATEGORY)
        @ConfigEntry.Gui.RequiresRestart
        public boolean convertDeepslateOres = false;
    }

    public static class Power {
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        @ConfigEntry.Gui.RequiresRestart
        public double rebornEnergyConversionCoefficient = 1d / 16d;
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        @ConfigEntry.Gui.RequiresRestart
        public double fastTransferEnergyConversionCoefficient = 1d / 16d;

        // Quarry Energy Config
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        @ConfigEntry.Gui.RequiresRestart
        public double quarryEnergyCapacity = 10000d;
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        public double quarryEnergyMakeFrame = 15d;
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        public double quarryEnergyBreakBlock = 10d;
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        public double quarryEnergyRemoveFluid = quarryEnergyBreakBlock * 5;
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        public double quarryEnergyMoveHead = 0.5d;

        // Advanced Pump
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        @ConfigEntry.Gui.RequiresRestart
        public double advPumpEnergyCapacity = 1000d;
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        public double advPumpEnergyRemoveFluid = 20d;

        // Chunk Destroyer Energy Config
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        @ConfigEntry.Gui.RequiresRestart
        public double advQuarryEnergyCapacity = 50000d;
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        public double advQuarryEnergyMakeFrame = 15d;
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        public double advQuarryEnergyBreakBlock = 10d;
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        public double advQuarryEnergyRemoveFluid = advQuarryEnergyBreakBlock * 5;
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        public double advQuarryEnergyMoveHead = 0.5d;
    }
}

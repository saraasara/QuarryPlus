package com.yogpc.qp.machines;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.yogpc.qp.utils.ManualOrder;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public record EnchantmentLevel(Enchantment enchantment, int level) {
    public EnchantmentLevel(Map.Entry<Enchantment, Integer> entry) {
        this(entry.getKey(), entry.getValue());
    }

    public EnchantmentLevel(Identifier enchantmentID, int level) {
        this(Registry.ENCHANTMENT.get(enchantmentID), level);
    }

    @Nullable
    public Identifier enchantmentID() {
        return Registry.ENCHANTMENT.getId(enchantment());
    }

    public interface HasEnchantments {
        List<EnchantmentLevel> getEnchantments();
    }

    public static final Comparator<EnchantmentLevel> COMPARATOR =
        Comparator.comparingInt((EnchantmentLevel e) -> Registry.ENCHANTMENT.getRawId(e.enchantment()))
            .thenComparing(EnchantmentLevel::level);
    public static final Comparator<EnchantmentLevel> QUARRY_ENCHANTMENT_COMPARATOR =
        Comparator.comparing(EnchantmentLevel::enchantment,
            ManualOrder.builder(Comparator.comparingInt(Registry.ENCHANTMENT::getRawId))
                .add(Enchantments.EFFICIENCY)
                .add(Enchantments.UNBREAKING)
                .add(Enchantments.FORTUNE)
                .add(Enchantments.SILK_TOUCH)
                .build()
        ).thenComparing(EnchantmentLevel::level);
}

package com.yogpc.qp.machines.module;

import java.util.List;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.ItemConverter;
import com.yogpc.qp.machines.ItemKey;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public final class FilterModule implements QuarryModule {
    @VisibleForTesting
    final List<ItemKey> itemKeys;

    public FilterModule(List<ItemKey> itemKeys) {
        this.itemKeys = itemKeys;
    }

    public FilterModule(@Nullable ListTag tag) {
        this(getFromTag(tag));
    }

    public static List<ItemKey> getFromTag(@Nullable ListTag tag) {
        if (tag == null || tag.isEmpty()) return List.of();
        return tag.stream()
            .mapMulti(MapMulti.cast(CompoundTag.class))
            .map(ItemKey::fromNbt)
            .distinct()
            .toList();
    }

    @Override
    public ResourceLocation moduleId() {
        return Holder.ITEM_FILTER_MODULE.getRegistryName();
    }

    public ItemConverter createConverter() {
        return ItemConverter.voidConverter(this.itemKeys);
    }
}

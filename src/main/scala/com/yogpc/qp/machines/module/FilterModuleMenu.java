package com.yogpc.qp.machines.module;

import java.util.Optional;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class FilterModuleMenu extends AbstractContainerMenu {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + FilterModuleItem.NAME;
    private final ModuleContainer container;

    public FilterModuleMenu(int pContainerId, Player player, ItemStack filterModuleItem) {
        super(Holder.FILTER_MODULE_MENU_TYPE, pContainerId);
        this.container = new ModuleContainer(filterModuleItem);
        final int rows = 2;
        int i = (rows - 4) * 18;

        final int oneBox = 18;
        // Filter item inventory
        for (int j = 0; j < rows; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(container, k + j * 9, 8 + k * oneBox, 18 + j * oneBox));
            }
        }

        // Player
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(player.getInventory(), j1 + l * 9 + 9, 8 + j1 * oneBox, 103 + l * oneBox + i));
            }
        }
        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(player.getInventory(), i1, 8 + i1 * oneBox, 161 + i));
        }

    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        this.container.stopOpen(pPlayer);
    }

    private static class ModuleContainer extends SimpleContainer {
        private final ItemStack filterModuleItem;

        public ModuleContainer(ItemStack filterModuleItem) {
            super(18);
            this.filterModuleItem = filterModuleItem;
            FilterModule.getFromTag(Optional.ofNullable(filterModuleItem.getTag())
                    .map(t -> t.getList(FilterModuleItem.KEY_ITEMS, Tag.TAG_COMPOUND)).orElse(null))
                .stream()
                .map(itemKey -> itemKey.toStack(1))
                .forEach(this::addItem);
        }

        @Override
        public final int getMaxStackSize() {
            return 1;
        }

        @Override
        public void stopOpen(Player pPlayer) {
            super.stopOpen(pPlayer);
            if (this.isEmpty()) {
                filterModuleItem.removeTagKey(FilterModuleItem.KEY_ITEMS);
            } else {
                filterModuleItem.addTagElement(FilterModuleItem.KEY_ITEMS, FilterModule.getFromItems(this.removeAllItems()));
            }
        }
    }
}

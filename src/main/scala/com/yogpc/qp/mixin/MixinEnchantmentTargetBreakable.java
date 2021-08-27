package com.yogpc.qp.mixin;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.enchantment.EnchantmentTarget$2")
// EnchantmentTarget.BREAKABLE
public class MixinEnchantmentTargetBreakable {
    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = {"isAcceptableItem", "method_8177"}, at = @At("HEAD"), cancellable = true)
    public void addQuarry(Item item, CallbackInfoReturnable<Boolean> cir) {
        if (item == QuarryPlus.ModObjects.BLOCK_QUARRY.blockItem ||
            item == QuarryPlus.ModObjects.BLOCK_ADV_PUMP.blockItem) {
            cir.setReturnValue(true);
        }
    }
}

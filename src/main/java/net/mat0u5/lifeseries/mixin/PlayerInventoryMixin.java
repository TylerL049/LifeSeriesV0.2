package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.utils.other.OtherUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.mat0u5.lifeseries.Main.currentSeason;

@Mixin(value = PlayerInventory.class, priority = 1)
public abstract class PlayerInventoryMixin {

    @Inject(method = "markDirty", at = @At("TAIL"))
    private void onMarkDirty(CallbackInfo info) {
        ls$onUpdatedInventory();
    }

    @Inject(method = "offer", at = @At("TAIL"))
    private void onOffer(ItemStack stack, boolean notifiesClient, CallbackInfo info) {
        ls$onUpdatedInventory();
    }

    @Inject(method = "insertStack(Lnet/minecraft/item/ItemStack;)Z", at = @At("RETURN"))
    private void onInsertStack(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (cir != null && cir.getReturnValue()) {
            ls$onUpdatedInventory();
        }
    }

    @Inject(method = "dropSelectedItem", at = @At("RETURN"))
    private void onDropSelectedItem(boolean entireStack, CallbackInfoReturnable<ItemStack> cir) {
        if (!cir.getReturnValue().isEmpty()) {
            ls$onUpdatedInventory();
        }
    }

    @Inject(method = "removeStack(II)Lnet/minecraft/item/ItemStack;", at = @At("RETURN"))
    private void onRemoveStack(int slot, int amount, CallbackInfoReturnable<ItemStack> cir) {
        if (!cir.getReturnValue().isEmpty()) {
            ls$onUpdatedInventory();
        }
    }

    @Inject(method = "setStack", at = @At("TAIL"))
    private void onSetStack(int slot, ItemStack stack, CallbackInfo info) {
        ls$onUpdatedInventory();
    }

    @Unique
    private boolean ls$processing = false;
    @Unique
    private int ls$skippedCalls = 0;

    @Unique
    private void ls$onUpdatedInventory() {
        if (!Main.isLogicalSide()) return;
        if (ls$processing) {
            ls$skippedCalls++;
            return;
        }
        ls$processing = true;
        PlayerInventory inventory = (PlayerInventory) (Object) this;
        PlayerEntity player = inventory.player;
        try {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                currentSeason.onUpdatedInventory(serverPlayer);
            }
        }
        finally {
            ls$processing = false;
            //if (ls$skippedCalls != 0) OtherUtils.log(player.getNameForScoreboard()+" skipped " + ls$skippedCalls + " inventory updates.");
            ls$skippedCalls = 0;
        }
    }
}

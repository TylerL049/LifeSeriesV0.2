package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.Main;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.mat0u5.lifeseries.Main.seasonConfig;

@Mixin(value = SpawnEggItem.class, priority = 1)
public abstract class SpawnEggItemMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void preventSpawnerModification(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (!Main.isLogicalSide()) return;
        if (context.getPlayer() instanceof ServerPlayerEntity) {
            if (seasonConfig.SPAWN_EGG_ALLOW_ON_SPAWNER.get(seasonConfig)) return;
            Block block = context.getWorld().getBlockState(context.getBlockPos()).getBlock();
            if (block != Blocks.SPAWNER) return;
            if (context.getPlayer() == null) return;
            if (context.getPlayer().isCreative() && seasonConfig.CREATIVE_IGNORE_BLACKLIST.get(seasonConfig)) return;
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}

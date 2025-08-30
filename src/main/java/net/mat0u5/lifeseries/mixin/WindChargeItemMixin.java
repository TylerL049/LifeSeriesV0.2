package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.seasons.season.Seasons;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.superpowers.Superpowers;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.superpowers.SuperpowersWildcard;
import net.mat0u5.lifeseries.utils.other.TaskScheduler;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.WindChargeItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.mat0u5.lifeseries.Main.currentSeason;
//? if <= 1.21 {
import net.minecraft.item.ItemStack;
import net.minecraft.util.TypedActionResult;
//?}
//? if >= 1.21.2
/*import net.minecraft.util.ActionResult;*/

@Mixin(value = WindChargeItem.class, priority = 1)
public class WindChargeItemMixin {
    @Inject(method = "use", at = @At("RETURN"))
    //? if <= 1.21 {
    public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
    //?} else
    /*public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {*/
        if (!Main.isLogicalSide()) return;
        if (user instanceof ServerPlayerEntity player) {
            if (currentSeason.getSeason() != Seasons.WILD_LIFE) return;
            if (!SuperpowersWildcard.hasActivatedPower(player, Superpowers.WIND_CHARGE)) return;

            TaskScheduler.scheduleTask(1, () -> {
                player.getInventory().insertStack(Items.WIND_CHARGE.getDefaultStack());
                player.getInventory().markDirty();
                PlayerUtils.updatePlayerInventory(player);
            });
        }
    }
}

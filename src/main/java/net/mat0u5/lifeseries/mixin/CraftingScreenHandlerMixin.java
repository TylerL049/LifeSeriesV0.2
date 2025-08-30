package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.utils.world.ItemStackUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if <= 1.21
import net.minecraft.world.World;
//? if >= 1.21.2
/*import net.minecraft.server.world.ServerWorld;*/

@Mixin(value = CraftingScreenHandler.class, priority = 1)
public class CraftingScreenHandlerMixin {
    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    //? if <= 1.21 {
    private static void blockPreviewIfNoCraftingItemPresent(ScreenHandler handler, World world, PlayerEntity player,
            RecipeInputInventory craftingInventory, CraftingResultInventory resultInventory, RecipeEntry<CraftingRecipe> recipe, CallbackInfo ci) {
    //?} else {
    /*private static void blockPreviewIfNoCraftingItemPresent(ScreenHandler handler, ServerWorld world, PlayerEntity player,
            RecipeInputInventory craftingInventory, CraftingResultInventory resultInventory, RecipeEntry<CraftingRecipe> recipe, CallbackInfo ci) {
    *///?}
        if (!Main.isLogicalSide()) return;

        for (int i = 0; i < craftingInventory.size(); i++) {
            ItemStack stack = craftingInventory.getStack(i);
            if (ItemStackUtils.hasCustomComponentEntry(stack, "NoCrafting") ||
                    ItemStackUtils.hasCustomComponentEntry(stack, "NoModifications")) {
                resultInventory.setStack(0, ItemStack.EMPTY);
                ci.cancel();
                return;
            }
        }
    }
}

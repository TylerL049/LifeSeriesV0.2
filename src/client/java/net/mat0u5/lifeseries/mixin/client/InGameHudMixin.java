package net.mat0u5.lifeseries.mixin.client;

import net.mat0u5.lifeseries.MainClient;
import net.mat0u5.lifeseries.seasons.season.Seasons;
import net.mat0u5.lifeseries.utils.ClientUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
//? if > 1.21 && <= 1.21.5 {
/*import java.util.function.Function;
import net.minecraft.client.render.RenderLayer;
*///?}
//? if >= 1.21.6
/*import com.mojang.blaze3d.pipeline.RenderPipeline;*/

@Mixin(value = InGameHud.class, priority = 1)
public class InGameHudMixin {
    @Unique
    private static final List<String> ls$allowedColors = List.of(
            "aqua","black","blue","dark_aqua","dark_blue","dark_gray","dark_green",
            "dark_purple","dark_red","gold","gray","green","light_purple","white","yellow", "red"
    );
    @Unique
    private static final List<String> ls$allowedHearts = List.of(
            "hud/heart/full", "hud/heart/full_blinking", "hud/heart/half", "hud/heart/half_blinking",
            "hud/heart/hardcore_full", "hud/heart/hardcore_full_blinking", "hud/heart/hardcore_half", "hud/heart/hardcore_half_blinking"
    );

    //? if <= 1.21 {
    @Redirect(method = "drawHeart", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
    private void customHearts(DrawContext instance, Identifier identifier, int x, int y, int u, int v) {
    //?} else if <= 1.21.5 {
    /*@Redirect(method = "drawHeart", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V"))
    private void customHearts(DrawContext instance, Function<Identifier, RenderLayer> renderLayers, Identifier identifier, int x, int y, int u, int v) {
    *///?} else {
    /*@Redirect(method = "drawHeart", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V"))
    private void customHearts(DrawContext instance, RenderPipeline renderPipeline, Identifier identifier, int x, int y, int u, int v) {
    *///?}

        String texturePath = identifier.getPath();
        Team playerTeam = ClientUtils.getPlayerTeam();
        if (!MainClient.COLORED_HEARTS || MainClient.clientCurrentSeason == Seasons.SECRET_LIFE ||
                playerTeam == null || playerTeam.getColor() == null ||
                !ls$allowedColors.contains(playerTeam.getColor().getName().toLowerCase()) ||
                !ls$allowedHearts.contains(texturePath)) {
            //? if <= 1.21 {
            instance.drawGuiTexture(identifier, x, y, u, v);
             //?} else if <= 1.21.5 {
            /*instance.drawGuiTexture(renderLayers, identifier, x, y, u, v);
            *///?} else {
            /*instance.drawGuiTexture(renderPipeline, identifier, x, y, u, v);
            *///?}
            return;
        }

        String color = playerTeam.getColor().getName().toLowerCase();

        String heartType = texturePath.replaceFirst("hud/heart/", "");

        if (!heartType.startsWith("hardcore_")) {
            if (MainClient.COLORED_HEARTS_HARDCORE_ALL_LIVES || (playerTeam.getName().equals("lives_1") & MainClient.COLORED_HEARTS_HARDCORE_LAST_LIFE)) {
                heartType = "hardcore_"+heartType;
            }
        }
        Identifier customHeart = Identifier.of("lifeseries", "textures/gui/hearts/"+color+"_"+heartType+".png");
        //? if <= 1.21 {
        instance.drawTexture(customHeart, x, y, 100, u, v, u, v, u, v);
        //?} else if <= 1.21.5 {
        /*instance.drawTexture(renderLayers, customHeart, x, y, u, v, u, v, u, v);
        *///?} else {
        /*instance.drawTexture(renderPipeline, customHeart, x, y, u, v, u, v, u, v);
        *///?}
    }
}

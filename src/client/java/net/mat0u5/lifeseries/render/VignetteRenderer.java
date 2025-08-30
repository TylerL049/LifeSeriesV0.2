package net.mat0u5.lifeseries.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
//? if <= 1.21 {
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
//?}
//? if >= 1.21.2 {
/*import net.minecraft.util.math.ColorHelper;
*///?}
//? if >= 1.21.2 && <= 1.21.5
/*import net.minecraft.client.render.RenderLayer;*/
//? if >= 1.21.6
/*import net.minecraft.client.gl.RenderPipelines;*/

public class VignetteRenderer {
    private static final Identifier VIGNETTE_TEXTURE = Identifier.ofVanilla("textures/misc/vignette.png");
    private static float vignetteDarkness = 0.0F;
    private static long vignetteEnd = 0;

    public static void renderVignette(DrawContext context) {
        if (System.currentTimeMillis() >= vignetteEnd && vignetteEnd != -1) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        float darkness = MathHelper.clamp(vignetteDarkness, 0.0F, 1.0F);
        if (darkness == 0) return;


        //? if <= 1.21 {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO
        );
        context.setShaderColor(darkness, darkness, darkness, 1.0F);
        context.drawTexture(VIGNETTE_TEXTURE, 0, 0, -90, 0.0F, 0.0F,
                context.getScaledWindowWidth(), context.getScaledWindowHeight(),
                context.getScaledWindowWidth(), context.getScaledWindowHeight()
        );

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        //?} else if <= 1.21.5 {
        /*int color = ColorHelper.fromFloats(1.0F, darkness, darkness, darkness);
        context.drawTexture(RenderLayer::getVignette, VIGNETTE_TEXTURE, 0, 0, 0.0F, 0.0F,
                context.getScaledWindowWidth(), context.getScaledWindowHeight(), context.getScaledWindowWidth(), context.getScaledWindowHeight(), color);
        *///?} else {
        /*int color = ColorHelper.fromFloats(1.0F, darkness, darkness, darkness);
        context.drawTexture(RenderPipelines.VIGNETTE, VIGNETTE_TEXTURE, 0, 0, 0.0F, 0.0F,
                context.getScaledWindowWidth(), context.getScaledWindowHeight(), context.getScaledWindowWidth(), context.getScaledWindowHeight(), color);
        *///?}
    }

    // Call this method to show the vignette for a certain duration
    public static void showVignetteFor(float darkness, long durationMillis) {
        vignetteDarkness = MathHelper.clamp(darkness, 0.0F, 1.0F);
        if (durationMillis == -1) {
            vignetteEnd = -1;
        }
        else {
            vignetteEnd = System.currentTimeMillis() + durationMillis;
        }
    }
}

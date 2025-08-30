package net.mat0u5.lifeseries.render;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class ClientRenderer {
    public static boolean isGameFullyFrozen = false;
    public static void onInitialize() {
        HudRenderCallback.EVENT.register(ClientRenderer::renderText);
    }

    private static void renderText(DrawContext context, RenderTickCounter renderTickCounter) {
        TextHud.renderText(context);
        VignetteRenderer.renderVignette(context);
    }
}

package net.mat0u5.lifeseries.gui;

import net.mat0u5.lifeseries.render.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public abstract class DefaultSmallScreen extends DefaultScreen {
    private static final Identifier BACKGROUND_TEXTURE = Identifier.of("lifeseries","textures/gui/gui_small.png");

    protected DefaultSmallScreen(Text name, float scaleX, float scaleY) {
        super(name, scaleX, scaleY);
        this.BG_WIDTH = (int) (148 * scaleX);
        this.BG_HEIGHT = (int) (67 * scaleY);
        this.BG_WIDTH_UNSCALED = 148;
        this.BG_HEIGHT_UNSCALED = 67;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        calculateCoordinates();
    }

    public DefaultSmallScreen(Text name) {
        super(name);
        this.BG_WIDTH = 148;
        this.BG_HEIGHT = 67;
        this.BG_WIDTH_UNSCALED = 148;
        this.BG_HEIGHT_UNSCALED = 67;
        calculateCoordinates();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY) {
        //? if <= 1.21 {
        if (!isScaled()) {
            RenderUtils.drawTexture(context, BACKGROUND_TEXTURE, startX, startY, 0, 0, BG_WIDTH, BG_HEIGHT);
        }
        else {
            RenderUtils.drawTextureScaled(context, BACKGROUND_TEXTURE, startX, startY, 0, 0, BG_WIDTH_UNSCALED, BG_HEIGHT_UNSCALED, scaleX, scaleY);
        }
        //?} else {
        /*if (!isScaled()) {
        RenderUtils.drawTexture(context, BACKGROUND_TEXTURE, startX, startY, 0, 0, BG_WIDTH, BG_HEIGHT, 256, 256);
        }
        else {
        RenderUtils.drawTextureScaled(context, BACKGROUND_TEXTURE, startX, startY, 0, 0, BG_WIDTH_UNSCALED, BG_HEIGHT_UNSCALED, 256, 256, scaleX, scaleY);
        }
        *///?}
        if (allowCloseButton()) renderClose(context, mouseX, mouseY);
    }

    public abstract void render(DrawContext context, int mouseX, int mouseY);
}
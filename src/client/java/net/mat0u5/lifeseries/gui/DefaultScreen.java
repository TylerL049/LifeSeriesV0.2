package net.mat0u5.lifeseries.gui;

import net.mat0u5.lifeseries.render.RenderUtils;
import net.mat0u5.lifeseries.utils.TextColors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public abstract class DefaultScreen extends Screen {
    private static final Identifier BACKGROUND_TEXTURE_LEFT = Identifier.of("lifeseries","textures/gui/gui_left.png");
    private static final Identifier BACKGROUND_TEXTURE_RIGHT = Identifier.of("lifeseries","textures/gui/gui_right.png");
    private static final Identifier TEXTURE_CLOSE = Identifier.of("lifeseries","textures/gui/close.png");
    private static final Identifier TEXTURE_SELECTED = Identifier.of("lifeseries","textures/gui/selected.png");

    protected int BG_WIDTH;
    protected int BG_HEIGHT;
    protected int BG_WIDTH_UNSCALED;
    protected int BG_HEIGHT_UNSCALED;
    protected float scaleX = 1;
    protected float scaleY = 1;
    protected static final int DEFAULT_TEXT_COLOR = TextColors.DEFAULT;

    protected DefaultScreen(Text name, float scaleX, float scaleY) {
        super(name);
        this.BG_WIDTH = (int) (320.0 * scaleX);
        this.BG_HEIGHT = (int) (180.0 * scaleY);
        this.BG_WIDTH_UNSCALED = 320;
        this.BG_HEIGHT_UNSCALED = 180;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        calculateCoordinates();
    }

    public DefaultScreen(Text name) {
        super(name);
        this.BG_WIDTH = 320;
        this.BG_HEIGHT = 180;
        this.BG_WIDTH_UNSCALED = 320;
        this.BG_HEIGHT_UNSCALED = 180;
        calculateCoordinates();
    }


    protected int startX;
    protected int centerX;
    protected int endX;
    protected int backgroundWidth;

    protected int startY;
    protected int centerY;
    protected int endY;
    protected int backgroundHeight;

    public void calculateCoordinates() {
        startX = (this.width - BG_WIDTH) / 2;
        endX = startX + BG_WIDTH;
        centerX = (startX + endX) / 2;
        backgroundWidth = endX - startX;

        startY = (this.height - BG_HEIGHT) / 2;
        endY = startY + BG_HEIGHT;
        centerY = (startY + endY) / 2;
        backgroundHeight = endY - startY;
    }

    public boolean isScaled() {
        return scaleX != 1 || scaleY != 1;
    }

    public boolean allowCloseButton() {
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && allowCloseButton()) { // Left-click
            if (isInCloseRegion((int)mouseX, (int)mouseY)) {
                if (this.client != null) this.client.setScreen(null);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean isInCloseRegion(int x, int y) {
        double width = textRenderer.getWidth(Text.of("✖"));
        double middleX = endX - 4 - (width/2);
        double height = textRenderer.fontHeight;
        double middleY = startY + 4 + (height/2);
        return Math.abs(x-middleX) <= (width/2) && Math.abs(y-middleY) <= (height/2);
    }

    @Override
    protected void init() {
        calculateCoordinates();
        super.init();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    public void renderBackground(DrawContext context, int mouseX, int mouseY) {
        //? if <= 1.21 {
        if (!isScaled()) {
            RenderUtils.drawTexture(context, BACKGROUND_TEXTURE_LEFT, startX, startY, 0, 0, BG_WIDTH/2, BG_HEIGHT);
            RenderUtils.drawTexture(context, BACKGROUND_TEXTURE_RIGHT, startX+BG_WIDTH/2, startY, 0, 0, BG_WIDTH/2, BG_HEIGHT);
        }
        else {
            RenderUtils.drawTextureScaled(context, BACKGROUND_TEXTURE_LEFT, startX, startY, 0, 0, BG_WIDTH_UNSCALED/2, BG_HEIGHT_UNSCALED, scaleX, scaleY);
            RenderUtils.drawTextureScaled(context, BACKGROUND_TEXTURE_RIGHT, startX+BG_WIDTH/2, startY, 0, 0, BG_WIDTH_UNSCALED/2, BG_HEIGHT_UNSCALED, scaleX, scaleY);
        }
        //?} else {
        /*if (!isScaled()) {
            RenderUtils.drawTexture(context, BACKGROUND_TEXTURE_LEFT, startX, startY, 0, 0, BG_WIDTH/2, BG_HEIGHT, 256, 256);
            RenderUtils.drawTexture(context, BACKGROUND_TEXTURE_RIGHT, startX+BG_WIDTH/2, startY, 0, 0, BG_WIDTH/2, BG_HEIGHT, 256, 256);
        }
        else {
            RenderUtils.drawTextureScaled(context, BACKGROUND_TEXTURE_LEFT, startX, startY, 0, 0, BG_WIDTH_UNSCALED/2, BG_HEIGHT_UNSCALED, 256, 256, scaleX, scaleY);
            RenderUtils.drawTextureScaled(context, BACKGROUND_TEXTURE_RIGHT, startX+BG_WIDTH/2, startY, 0, 0, BG_WIDTH_UNSCALED/2, BG_HEIGHT_UNSCALED, 256, 256, scaleX, scaleY);
        }
        *///?}
        if (allowCloseButton()) renderClose(context, mouseX, mouseY);
    }

    public void renderClose(DrawContext context, int mouseX, int mouseY) {
        if (isInCloseRegion(mouseX, mouseY)) RenderUtils.drawTextRight(context, textRenderer, Text.of("§l✖"), endX - 4, startY + 4);
        else RenderUtils.drawTextRight(context, textRenderer, Text.of("✖"), endX - 4, startY + 4);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        this.renderBackground(context, mouseX, mouseY);
        this.render(context, mouseX, mouseY);
        super.render(context, mouseX, mouseY, delta);
    }

    public abstract void render(DrawContext context, int mouseX, int mouseY);
}
package net.mat0u5.lifeseries.gui.seasons;

import net.mat0u5.lifeseries.gui.DefaultSmallScreen;
import net.mat0u5.lifeseries.network.NetworkHandlerClient;
import net.mat0u5.lifeseries.render.RenderUtils;
import net.mat0u5.lifeseries.seasons.season.Seasons;
import net.mat0u5.lifeseries.utils.TextColors;
import net.mat0u5.lifeseries.utils.enums.PacketNames;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;
//? if >= 1.21.2 && <= 1.21.5
/*import net.minecraft.client.render.RenderLayer;*/
//? if >= 1.21.6
/*import net.minecraft.client.gl.RenderPipelines;*/

public class ChooseExtraSeasonScreen extends DefaultSmallScreen {

    private static final Identifier TEXTURE_SELECTED = Identifier.of("lifeseries","textures/gui/selected.png");
    private static final Identifier TEXTURE_SIMPLELIFE = Identifier.of("lifeseries","textures/gui/simplelife.png");
    private static final Identifier TEXTURE_REALLIFE = Identifier.of("lifeseries","textures/gui/reallife.png");

    public static boolean hasSelectedBefore = false;

    public ChooseExtraSeasonScreen(boolean hasSelectedBefore) {
        super(Text.literal("Choose April Season Screen"), 1.3f, 1.6f);
        this.hasSelectedBefore = hasSelectedBefore;
    }

    public int getRegion(int x, int y) {
        // X
        int startX = (this.width - BG_WIDTH) / 2;
        int endX = startX + BG_WIDTH;
        int centerX = (startX + endX) / 2;


        // Y
        int startY = (this.height - BG_HEIGHT) / 2;

        if (Math.abs(y - (startY + 60)) < 25) {
            if (Math.abs(x - (centerX - 30)) < 25) return 2;
            if (Math.abs(x - (centerX + 30)) < 25) return 3;
        }


        Text goBack = Text.of("Go Back");
        int textWidth = textRenderer.getWidth(goBack);
        int textHeight = textRenderer.fontHeight;

        Rectangle rect = new Rectangle(startX+9, endY-11-textHeight, textWidth+1, textHeight+1);
        if (x >= rect.x && x <= rect.x + rect.width && y >= rect.y && y <= rect.y + rect.height) {
            return 1;
        }

        return 0;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left-click
            int region = getRegion((int) mouseX, (int) mouseY);
            if (region == 1 && this.client != null) {
                this.client.setScreen(new ChooseSeasonScreen(hasSelectedBefore));
            }
            else if (region != 0) {
                if (hasSelectedBefore && this.client != null) {
                    if (region == 2) this.client.setScreen(new ConfirmSeasonAnswerScreen(this, Seasons.REAL_LIFE.getName()));
                    if (region == 3) this.client.setScreen(new ConfirmSeasonAnswerScreen(this, Seasons.SIMPLE_LIFE.getName()));
                }
                else {
                    if (region == 2) NetworkHandlerClient.sendStringPacket(PacketNames.SET_SEASON, Seasons.REAL_LIFE.getName());
                    if (region == 3) NetworkHandlerClient.sendStringPacket(PacketNames.SET_SEASON, Seasons.SIMPLE_LIFE.getName());
                    if (this.client != null) this.client.setScreen(null);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY) {
        int region = getRegion(mouseX, mouseY);

        // Background + images
        //? if <= 1.21 {

        if (region != 0) {
            if (region == 2) context.drawTexture(TEXTURE_SELECTED,centerX-25-30, startY + 35, 0, 0, 50, 50);
            if (region == 3) context.drawTexture(TEXTURE_SELECTED,centerX-25+30, startY + 35, 0, 0, 50, 50);
        }

        context.getMatrices().push();
        context.getMatrices().scale(0.2f, 0.2f, 1.0f);

        context.drawTexture(TEXTURE_REALLIFE, (centerX-25-30) * 5, (startY + 35) * 5, 0, 0, 256, 256);
        context.drawTexture(TEXTURE_SIMPLELIFE, (centerX-25+30) * 5, (startY + 35) * 5, 0, 0, 256, 256);

        context.getMatrices().pop();
        //?} else if <= 1.21.5 {
        /*if (region != 0) {
            if (region == 2) context.drawTexture(RenderLayer::getGuiTextured, TEXTURE_SELECTED, centerX-25-30, startY + 35, 0, 0, 50, 50, 64, 64);
            if (region == 3) context.drawTexture(RenderLayer::getGuiTextured, TEXTURE_SELECTED, centerX-25+30, startY + 35, 0, 0, 50, 50, 64, 64);
        }

        context.getMatrices().push();
        context.getMatrices().scale(0.2f, 0.2f, 1.0f);

        context.drawTexture(RenderLayer::getGuiTextured, TEXTURE_REALLIFE, (centerX-25-30) * 5, (startY + 35) * 5, 0, 0, 256, 256, 256, 256);
        context.drawTexture(RenderLayer::getGuiTextured, TEXTURE_SIMPLELIFE, (centerX-25+30) * 5, (startY + 35) * 5, 0, 0, 256, 256, 256, 256);

        context.getMatrices().pop();
        *///?} else {
        /*if (region != 0) {
            if (region == 2) context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE_SELECTED, centerX-25-30, startY + 35, 0, 0, 50, 50, 64, 64);
            if (region == 3) context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE_SELECTED, centerX-25+30, startY + 35, 0, 0, 50, 50, 64, 64);
        }

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(0.2f, 0.2f);

        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE_REALLIFE, (centerX-25-30) * 5, (startY + 35) * 5, 0, 0, 256, 256, 256, 256);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE_SIMPLELIFE, (centerX-25+30) * 5, (startY + 35) * 5, 0, 0, 256, 256, 256, 256);

        context.getMatrices().popMatrix();
        *///?}

        String prompt = "Select the season you want to play.";
        RenderUtils.drawTextCenter(context, this.textRenderer, Text.of(prompt), centerX, startY + 20);

        Text goBack = Text.of("Go Back");
        int textWidth = textRenderer.getWidth(goBack);
        int textHeight = textRenderer.fontHeight;

        Rectangle rect = new Rectangle(startX+9, endY-11-textHeight, textWidth+1, textHeight+1);

        context.fill(rect.x - 1, rect.y - 1, rect.x + rect.width + 1, rect.y, DEFAULT_TEXT_COLOR); // top border
        context.fill(rect.x - 1, rect.y + rect.height, rect.x + rect.width + 2, rect.y + rect.height + 2, DEFAULT_TEXT_COLOR); // bottom
        context.fill(rect.x - 1, rect.y, rect.x, rect.y + rect.height, DEFAULT_TEXT_COLOR); // left
        context.fill(rect.x + rect.width, rect.y-1, rect.x + rect.width + 2, rect.y + rect.height, DEFAULT_TEXT_COLOR); // right

        if (region == 1) {
            RenderUtils.drawTextLeft(context, this.textRenderer, TextColors.WHITE, goBack, rect.x+1, rect.y+1);
        }
        else {
            RenderUtils.drawTextLeft(context, this.textRenderer, DEFAULT_TEXT_COLOR, goBack, rect.x+1, rect.y+1);
        }

    }
}

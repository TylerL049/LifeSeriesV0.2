package net.mat0u5.lifeseries.gui.seasons;

import net.mat0u5.lifeseries.gui.DefaultSmallScreen;
import net.mat0u5.lifeseries.network.NetworkHandlerClient;
import net.mat0u5.lifeseries.render.RenderUtils;
import net.mat0u5.lifeseries.utils.enums.PacketNames;
import net.mat0u5.lifeseries.utils.other.TextUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ConfirmSeasonAnswerScreen extends DefaultSmallScreen {
    private final Screen parent;
    private final String seasonName;

    public ConfirmSeasonAnswerScreen(Screen parent, String seasonName) {
        super(Text.literal("Confirm Answer"), 2.2f, 1.6f);
        this.parent = parent;
        this.seasonName = seasonName;
    }

    @Override
    public boolean allowCloseButton() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        int startX = (this.width - BG_WIDTH) / 2;
        int startY = (this.height - BG_HEIGHT) / 2;
        int fifth2 = startX + (BG_WIDTH / 5)*2;
        int fifth3 = startX + (BG_WIDTH / 5)*3;

        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Confirm"), btn -> {
                            if (this.client != null) this.client.setScreen(null);
                            NetworkHandlerClient.sendStringPacket(PacketNames.SET_SEASON, seasonName);
                        })
                        .position(fifth2 - 40, startY + BG_HEIGHT - 35)
                        .size(60, 20)
                        .build()
        );

        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Cancel"), btn -> {
                            if (this.client != null) this.client.setScreen(parent);
                        })
                        .position(fifth3 - 20, startY + BG_HEIGHT - 35)
                        .size(60, 20)
                        .build()
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY) {
        Text prompt1 = Text.of("WARNING: you have already selected a season.");
        Text prompt2 = Text.of("Changing it might cause some saved data to be lost (lives, ...).");
        Text prompt3 = TextUtils.formatPlain("Change the season to {}?", seasonName);
        RenderUtils.drawTextCenter(context, textRenderer, prompt1, centerX, startY + 15);
        RenderUtils.drawTextCenter(context, textRenderer, prompt2, centerX, startY + 20 + textRenderer.fontHeight);
        RenderUtils.drawTextCenter(context, textRenderer, prompt3, centerX, startY + 35 + textRenderer.fontHeight*2);
    }
}
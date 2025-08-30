package net.mat0u5.lifeseries.gui.other;

import net.mat0u5.lifeseries.gui.DefaultScreen;
import net.mat0u5.lifeseries.render.RenderUtils;
import net.mat0u5.lifeseries.utils.TextColors;
import net.mat0u5.lifeseries.utils.other.TextUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class UpdateInfoScreen extends DefaultScreen {
    private String versionName;
    private String description;

    public UpdateInfoScreen(String versionName, String description) {
        super(Text.of("New Life Series Update"), 1.3f, 1.3f);
        this.versionName = versionName;
        this.description = description.replace("\r","");
    }

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Join Discord").withColor(TextColors.PASTEL_WHITE),btn -> {
                            Util.getOperatingSystem().open("https://discord.gg/QWJxfb4zQZ");
                        })
                        .position(startX + 8, endY - 28)
                        .size(80, 20)
                        .build()
        );
        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Full Changelog").withColor(TextColors.PASTEL_WHITE), btn -> {
                            Util.getOperatingSystem().open("https://github.com/Mat0u5/LifeSeries/blob/main/docs/changelogs/"+versionName+".md");
                        })
                        .position(endX - 80 - 8, endY - 28)
                        .size(80, 20)
                        .build()
        );
        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Download on Modrinth"), btn -> {
                            if (this.client != null) this.client.setScreen(null);
                            Util.getOperatingSystem().open("https://modrinth.com/mod/life-series"); //Same as having a text with a click event, but that doesnt work in GUIs
                        })
                        .position(centerX - 85, endY - 28)
                        .size(170, 20)
                        .build()
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY) {
        RenderUtils.drawTextCenter(context, this.textRenderer, Text.of("§0§nA new Life Series mod update is available!"), centerX, startY + 10);
        RenderUtils.drawTextLeft(context, this.textRenderer, TextUtils.formatLoosely("§0§nChangelog in version §l{}§0:",versionName), startX + 10, startY + 25 + textRenderer.fontHeight);
        RenderUtils.drawTextLeftWrapLines(context, this.textRenderer, DEFAULT_TEXT_COLOR, Text.of(description), startX + 10, startY + 30 + textRenderer.fontHeight*2, backgroundWidth-20, 5);
    }
}
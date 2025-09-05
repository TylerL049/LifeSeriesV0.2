package net.mat0u5.lifeseries.gui.other;

import net.mat0u5.lifeseries.gui.DefaultScreen;
import net.mat0u5.lifeseries.network.NetworkHandlerClient;
import net.mat0u5.lifeseries.render.RenderUtils;
import net.mat0u5.lifeseries.utils.enums.PacketNames;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ChooseWildcardScreen extends DefaultScreen {

    public ChooseWildcardScreen() {
        super(Text.literal("Choose Wildcard Screen"));
    }

    @Override
    protected void init() {
        super.init();

        int oneThirdX = startX + BG_WIDTH / 3;
        int twoThirdX = startX + (BG_WIDTH / 3) * 2;

        /*
            Left column
         */
        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Size Shifting"), btn -> selectWildcard("size_shifting"))
                        .position(oneThirdX - 40, startY + 45)
                        .size(80, 20)
                        .build()
        );

        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Hunger"), btn -> selectWildcard("hunger"))
                        .position(oneThirdX - 40, startY + 75)
                        .size(80, 20)
                        .build()
        );

        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Snails"), btn -> selectWildcard("snails"))
                        .position(oneThirdX - 40, startY + 105)
                        .size(80, 20)
                        .build()
        );

        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Time Dilation"), btn -> selectWildcard("time_dilation"))
                        .position(oneThirdX - 40, startY + 135)
                        .size(80, 20)
                        .build()
        );

        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Player Swap"), btn -> selectWildcard("player_swap"))
                        .position(oneThirdX - 40, startY + 165)
                        .size(80, 20)
                        .build()
        );

        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("Floor is Lava"), btn -> selectWildcard("floor_is_lava"))
                    .position(oneThirdX - 40, startY + 195)
                    .size(80, 20)
                    .build()
    );

        /*
            Right column
         */
        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Trivia"), btn -> selectWildcard("trivia"))
                        .position(twoThirdX - 40, startY + 45)
                        .size(80, 20)
                        .build()
        );

        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Mob Swap"), btn -> selectWildcard("mob_swap"))
                        .position(twoThirdX - 40, startY + 75)
                        .size(80, 20)
                        .build()
        );

        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Superpowers"), btn -> selectWildcard("superpowers"))
                        .position(twoThirdX - 40, startY + 105)
                        .size(80, 20)
                        .build()
        );

        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Callback"), btn -> selectWildcard("callback"))
                        .position(twoThirdX - 40, startY + 135)
                        .size(80, 20)
                        .build()
        );

        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Gravity Manipulation"), btn -> selectWildcard("gravity_manipulation"))
                        .position(twoThirdX - 40, startY + 165)
                        .size(80, 20)
                        .build()
        );

        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Hot Potato"), btn -> selectWildcard("hot_potato"))
                        .position(twoThirdX - 40, startY + 195)
                        .size(80, 20)
                        .build()
        );
    }

    private void selectWildcard(String wildcardName) {
        if (this.client != null) this.client.setScreen(null);
        NetworkHandlerClient.sendStringPacket(PacketNames.SELECTED_WILDCARD, wildcardName);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY) {
        String prompt = "Select the Wildcard for this session.";
        RenderUtils.drawTextCenter(context, this.textRenderer, Text.of(prompt), centerX, startY + 20);
    }
}
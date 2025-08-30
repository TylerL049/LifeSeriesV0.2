package net.mat0u5.lifeseries.gui.trivia;

import net.mat0u5.lifeseries.features.Trivia;
import net.mat0u5.lifeseries.gui.DefaultSmallScreen;
import net.mat0u5.lifeseries.render.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ConfirmQuizAnswerScreen extends DefaultSmallScreen {
    private final QuizScreen parent;
    private final int answerIndex;

    public ConfirmQuizAnswerScreen(QuizScreen parent, int answerIndex) {
        super(Text.literal("Confirm Answer"));
        this.parent = parent;
        this.answerIndex = answerIndex;
    }

    @Override
    public boolean allowCloseButton() {
        return false;
    }

    @Override
    protected void init() {
        super.init();

        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Confirm"), btn -> {
                            if (this.client != null) this.client.setScreen(null);
                            Trivia.sendAnswer(answerIndex);
                        })
                        .position(startX + 8, endY - 28)
                        .size(60, 20)
                        .build()
        );

        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Cancel"), btn -> {
                            if (this.client != null) this.client.setScreen(parent);
                        })
                        .position(endX  - 68, endY - 28)
                        .size(60, 20)
                        .build()
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY) {
        RenderUtils.drawTextCenter(context, textRenderer, Text.of("Submit answer?"), centerX, startY + 20);
    }
}
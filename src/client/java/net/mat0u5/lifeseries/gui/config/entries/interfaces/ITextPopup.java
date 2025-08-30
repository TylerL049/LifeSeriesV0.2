package net.mat0u5.lifeseries.gui.config.entries.interfaces;

import net.mat0u5.lifeseries.utils.TextColors;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public interface ITextPopup extends IPopup {
    TextRenderer getTextRenderer();
    Text getPopupText();
    default int getPopupWidth() {
        return getTextRenderer().getWidth(getPopupText())+1;
    }

    default int getPopupHeight() {
        return getTextRenderer().fontHeight;
    }

    default void renderContent(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta) {
        TextRenderer textRenderer = getTextRenderer();
        Text popupText = getPopupText();
        if (popupText == null) return;
        context.drawText(textRenderer, popupText, x+1, y+1, TextColors.LIGHT_GRAY, false);
    }
}

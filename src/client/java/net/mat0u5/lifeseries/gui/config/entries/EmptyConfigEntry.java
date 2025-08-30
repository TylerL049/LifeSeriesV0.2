package net.mat0u5.lifeseries.gui.config.entries;

import net.minecraft.client.gui.DrawContext;

public abstract class EmptyConfigEntry extends ConfigEntry {
    public EmptyConfigEntry(String fieldName, String displayName, String description) {
        super(fieldName, displayName, description);
    }

    @Override
    protected void renderEntry(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, boolean hovered, float tickDelta) {
    }

    @Override
    protected boolean mouseClickedEntry(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    protected boolean keyPressedEntry(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    protected boolean charTypedEntry(char chr, int modifiers) {
        return false;
    }

    @Override
    protected void resetToDefault() {

    }

    @Override
    public boolean canReset() {
        return false;
    }

    @Override
    public boolean hasError() {
        return false;
    }

    @Override
    protected boolean hasResetButton() {
        return false;
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public String getValueAsString() {
        return "";
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public String getDefaultValueAsString() {
        return "";
    }

    @Override
    public Object getStartingValue() {
        return null;
    }

    @Override
    public String getStartingValueAsString() {
        return "";
    }

    @Override
    public void setValue(Object value) {
    }

    @Override
    public boolean modified() {
        return false;
    }
}

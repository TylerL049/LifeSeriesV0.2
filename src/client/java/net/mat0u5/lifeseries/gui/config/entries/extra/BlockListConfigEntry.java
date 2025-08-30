package net.mat0u5.lifeseries.gui.config.entries.extra;

import net.mat0u5.lifeseries.gui.config.entries.StringListPopupConfigEntry;
import net.mat0u5.lifeseries.utils.enums.ConfigTypes;
import net.mat0u5.lifeseries.utils.other.TextUtils;
import net.minecraft.block.Block;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class BlockListConfigEntry extends StringListPopupConfigEntry<Block> {
    public BlockListConfigEntry(String fieldName, String displayName, String description, String value, String defaultValue) {
        super(fieldName, displayName, description, value, defaultValue);
        reloadEntriesRaw(value);
    }

    @Override
    protected void reloadEntries(List<String> items) {
        if (entries != null) {
            entries.clear();
        }

        List<Block> newList = new ArrayList<>();
        boolean errors = false;

        for (String blockId : items) {
            if (blockId.isEmpty()) continue;
            if (!blockId.contains(":")) blockId = "minecraft:" + blockId;

            try {
                Identifier id = Identifier.of(blockId);
                RegistryKey<Block> key = RegistryKey.of(Registries.BLOCK.getKey(), id);

                Block block = Registries.BLOCK.get(key);
                if (block != null) {
                    newList.add(block);
                } else {
                    setError(TextUtils.formatString("Invalid block: '{}'", blockId));
                    errors = true;
                }
            } catch (Exception e) {
                setError(TextUtils.formatString("Error parsing block ID: '{}'", blockId));
                errors = true;
            }
        }

        entries = newList;
        if (!errors) {
            clearError();
        }
    }

    @Override
    protected void renderListEntry(DrawContext context, Block block, int x, int y, int mouseX, int mouseY, float tickDelta) {
        context.drawItem(block.asItem().getDefaultStack(), x, y);
    }

    @Override
    public boolean hasCustomErrors() {
        return true;
    }

    @Override
    public ConfigTypes getValueType() {
        return ConfigTypes.BLOCK_LIST;
    }
}

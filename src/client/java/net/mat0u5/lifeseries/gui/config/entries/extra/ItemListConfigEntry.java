package net.mat0u5.lifeseries.gui.config.entries.extra;

import net.mat0u5.lifeseries.gui.config.entries.StringListPopupConfigEntry;
import net.mat0u5.lifeseries.utils.enums.ConfigTypes;
import net.mat0u5.lifeseries.utils.other.TextUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ItemListConfigEntry extends StringListPopupConfigEntry<Item> {

    public ItemListConfigEntry(String fieldName, String displayName, String description, String value, String defaultValue) {
        super(fieldName, displayName, description, value, defaultValue);
        reloadEntriesRaw(value);
    }

    @Override
    protected void reloadEntries(List<String> items) {
        if (entries != null) {
            entries.clear();
        }

        List<Item> newList = new ArrayList<>();
        boolean errors = false;

        for (String itemId : items) {
            if (itemId.isEmpty()) continue;
            if (!itemId.contains(":")) itemId = "minecraft:" + itemId;

            try {
                Identifier id = Identifier.of(itemId);
                RegistryKey<Item> key = RegistryKey.of(Registries.ITEM.getKey(), id);

                Item item = Registries.ITEM.get(key);
                if (item != null) {
                    newList.add(item);
                } else {
                    setError(TextUtils.formatString("Invalid item: '{}'", itemId));
                    errors = true;
                }
            } catch (Exception e) {
                setError(TextUtils.formatString("Error parsing item ID: '{}'", itemId));
                errors = true;
            }
        }
        entries = newList;
        if (!errors) {
            clearError();
        }
    }

    @Override
    protected void renderListEntry(DrawContext context, Item item, int x, int y, int mouseX, int mouseY, float tickDelta) {
        context.drawItem(item.getDefaultStack(), x, y);
    }

    @Override
    public boolean hasCustomErrors() {
        return true;
    }

    @Override
    public ConfigTypes getValueType() {
        return ConfigTypes.ITEM_LIST;
    }
}

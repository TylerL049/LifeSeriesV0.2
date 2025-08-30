package net.mat0u5.lifeseries.gui.config.entries.extra;

import net.mat0u5.lifeseries.gui.config.entries.main.StringConfigEntry;
import net.mat0u5.lifeseries.utils.enums.ConfigTypes;
import net.mat0u5.lifeseries.utils.other.TextUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnchantListConfigEntry extends StringConfigEntry {
    public EnchantListConfigEntry(String fieldName, String displayName, String description, String value, String defaultValue) {
        super(fieldName, displayName, description, value, defaultValue);
    }

    @Override
    protected void onTextChanged(String text) {
        super.onTextChanged(text);
        reloadEntriesRaw(text);
    }

    protected void reloadEntriesRaw(String text) {
        String raw = text;
        raw = raw.replaceAll("\\[","").replaceAll("]","").replaceAll(" ", "");
        List<String> items = new ArrayList<>(Arrays.asList(raw.split(",")));
        reloadEntries(items);
    }
    protected void reloadEntries(List<String> items) {
        if (MinecraftClient.getInstance().world == null) return;

        List<RegistryKey<Enchantment>> newList = new ArrayList<>();
        boolean errors = false;

        Registry<Enchantment> enchantmentRegistry = MinecraftClient.getInstance().world.getRegistryManager()

        //? if <=1.21 {
        .get(RegistryKey.ofRegistry(Identifier.of("minecraft", "enchantment")));
        //?} else
        /*.getOrThrow(RegistryKey.ofRegistry(Identifier.of("minecraft", "enchantment")));*/


        for (String enchantmentId : items) {
            if (enchantmentId.isEmpty()) continue;
            if (!enchantmentId.contains(":")) enchantmentId = "minecraft:" + enchantmentId;

            try {
                Identifier id = Identifier.of(enchantmentId);
                Enchantment enchantment = enchantmentRegistry.get(id);

                if (enchantment != null) {
                    newList.add(enchantmentRegistry.getKey(enchantment).orElseThrow());
                } else {
                    setError(TextUtils.formatString("Invalid enchantment: '{}'", enchantmentId));
                    errors = true;
                }
            } catch (Exception e) {
                setError(TextUtils.formatString("Error parsing enchantment ID: '{}'", enchantmentId));
                errors = true;
            }
        }

        if (!errors) {
            clearError();
        }
    }

    @Override
    public boolean hasCustomErrors() {
        return true;
    }

    @Override
    public ConfigTypes getValueType() {
        return ConfigTypes.ENCHANT_LIST;
    }
}

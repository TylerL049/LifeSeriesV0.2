package net.mat0u5.lifeseries.gui.config.entries.extra;

import com.mojang.blaze3d.systems.RenderSystem;
import net.mat0u5.lifeseries.gui.config.entries.StringListPopupConfigEntry;
import net.mat0u5.lifeseries.utils.enums.ConfigTypes;
import net.mat0u5.lifeseries.utils.other.TextUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

//? if <= 1.21.5
import net.minecraft.client.texture.StatusEffectSpriteManager;

//? if >= 1.21.2 && <= 1.21.5
/*import net.minecraft.client.render.RenderLayer;*/

//? if >= 1.21.2
/*import net.minecraft.util.math.ColorHelper;*/

//? if >= 1.21.6
/*import net.minecraft.client.gl.RenderPipelines;*/

public class EffectListConfigEntry extends StringListPopupConfigEntry<RegistryEntry<StatusEffect>> {
    private static final Identifier EFFECT_BACKGROUND_TEXTURE = Identifier.ofVanilla("hud/effect_background");

    public EffectListConfigEntry(String fieldName, String displayName, String description, String value, String defaultValue) {
        super(fieldName, displayName, description, value, defaultValue, 5, 24, 2);
        reloadEntriesRaw(value);
    }

    @Override
    protected void reloadEntries(List<String> items) {
        if (MinecraftClient.getInstance().world == null) return;
        if (entries != null) {
            entries.clear();
        }

        List<RegistryEntry<StatusEffect>> newList = new ArrayList<>();
        boolean errors = false;

        Registry<StatusEffect> effectsRegistry = MinecraftClient.getInstance().world.getRegistryManager()
        //? if <=1.21 {
        .get(RegistryKey.ofRegistry(Identifier.of("minecraft", "mob_effect")));
        //?} else
        /*.getOrThrow(RegistryKey.ofRegistry(Identifier.of("minecraft", "mob_effect")));*/

        for (String potionId : items) {
            if (potionId.isEmpty()) continue;
            if (!potionId.contains(":")) potionId = "minecraft:" + potionId;

            try {
                Identifier id = Identifier.of(potionId);
                StatusEffect enchantment = effectsRegistry.get(id);

                if (enchantment != null) {
                    newList.add(effectsRegistry.getEntry(enchantment));
                } else {
                    setError(TextUtils.formatString("Invalid effect: '{}'", potionId));
                    errors = true;
                }
            } catch (Exception e) {
                setError(TextUtils.formatString("Error parsing effect ID: '{}'", potionId));
                errors = true;
            }
        }

        entries = newList;
        if (!errors) {
            clearError();
        }
    }

    @Override
    protected void renderListEntry(DrawContext context, RegistryEntry<StatusEffect> effectType, int x, int y, int mouseX, int mouseY, float tickDelta) {
        //? if <= 1.21 {
        StatusEffectSpriteManager statusEffectSpriteManager = MinecraftClient.getInstance().getStatusEffectSpriteManager();
        RenderSystem.enableBlend();

        context.drawGuiTexture(EFFECT_BACKGROUND_TEXTURE, x, y, 24, 24);
        Sprite sprite = statusEffectSpriteManager.getSprite(effectType);
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        context.drawSprite(x + 3, y + 3, 0, 18, 18, sprite);
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderSystem.disableBlend();
        //?} else if <= 1.21.5 {
        /*StatusEffectSpriteManager statusEffectSpriteManager = MinecraftClient.getInstance().getStatusEffectSpriteManager();
        context.drawGuiTexture(RenderLayer::getGuiTextured, EFFECT_BACKGROUND_TEXTURE, x, y, 24, 24);
        Sprite sprite = statusEffectSpriteManager.getSprite(effectType);
        context.drawSpriteStretched(RenderLayer::getGuiTextured, sprite, x + 3, y + 3, 18, 18, ColorHelper.getWhite(1.0f));
        *///?} else {
        /*context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, EFFECT_BACKGROUND_TEXTURE, x, y, 24, 24);
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, InGameHud.getEffectTexture(effectType), x + 3, y + 3, 18, 18, ColorHelper.getWhite(1.0f));
        *///?}
    }

    @Override
    public boolean hasCustomErrors() {
        return true;
    }

    @Override
    public ConfigTypes getValueType() {
        return ConfigTypes.EFFECT_LIST;
    }
}

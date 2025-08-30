package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.network.NetworkHandlerServer;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.WildcardManager;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.seasons.session.SessionTranscript;
import net.mat0u5.lifeseries.utils.other.OtherUtils;
import net.mat0u5.lifeseries.utils.other.TaskScheduler;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import static net.mat0u5.lifeseries.Main.currentSession;

//? if <= 1.21 {
import net.minecraft.component.ComponentMapImpl;
import java.util.Optional;
//?}

//? if >= 1.21.2 {
/*import net.minecraft.component.MergedComponentMap;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.item.consume.UseAction;
*///?}

public class Hunger extends Wildcard {
    private static final Random rnd = new Random();
    public static int SWITCH_DELAY = 36000;
    public static int shuffleVersion = 0;
    private static boolean shuffledBefore = false;
    private static int lastVersion = -1;
    private static long ticks = 0;

    public static int HUNGER_EFFECT_LEVEL = 3;

    private static final List<RegistryEntry<StatusEffect>> effects = List.of(
            StatusEffects.SPEED,
            StatusEffects.SLOWNESS,
            StatusEffects.HASTE,
            StatusEffects.MINING_FATIGUE,
            StatusEffects.STRENGTH,
            StatusEffects.INSTANT_HEALTH,
            StatusEffects.INSTANT_DAMAGE,
            StatusEffects.JUMP_BOOST,
            StatusEffects.NAUSEA,
            StatusEffects.REGENERATION,
            StatusEffects.RESISTANCE,
            StatusEffects.FIRE_RESISTANCE,
            StatusEffects.WATER_BREATHING,
            StatusEffects.INVISIBILITY,
            StatusEffects.BLINDNESS,
            StatusEffects.NIGHT_VISION,
            StatusEffects.WEAKNESS,
            StatusEffects.POISON,
            StatusEffects.WITHER,
            StatusEffects.HEALTH_BOOST,
            StatusEffects.ABSORPTION,
            StatusEffects.SATURATION,
            StatusEffects.GLOWING,
            StatusEffects.LEVITATION,
            StatusEffects.LUCK,
            StatusEffects.UNLUCK,
            StatusEffects.SLOW_FALLING,
            StatusEffects.CONDUIT_POWER,
            StatusEffects.DOLPHINS_GRACE,
            StatusEffects.HERO_OF_THE_VILLAGE,
            StatusEffects.DARKNESS,
            StatusEffects.WIND_CHARGED,
            StatusEffects.WEAVING,
            StatusEffects.OOZING,
            StatusEffects.INFESTED
    );

    private static final List<RegistryEntry<StatusEffect>> levelLimit = List.of(
            StatusEffects.STRENGTH,
            StatusEffects.INSTANT_HEALTH,
            StatusEffects.INSTANT_DAMAGE,
            StatusEffects.REGENERATION,
            StatusEffects.RESISTANCE,
            StatusEffects.WITHER,
            StatusEffects.ABSORPTION,
            StatusEffects.SATURATION
    );

    private static final List<RegistryEntry<StatusEffect>> durationLimit = List.of(
            StatusEffects.INSTANT_HEALTH,
            StatusEffects.INSTANT_DAMAGE,
            StatusEffects.SATURATION
    );

    public static final List<Item> commonItems = Arrays.asList(
            Items.DIRT, Items.STONE, Items.COBBLESTONE, Items.GRAVEL, Items.SAND, Items.NETHERRACK,
            Items.OAK_LOG, Items.SPRUCE_LOG, Items.BIRCH_LOG, Items.JUNGLE_LOG, Items.ACACIA_LOG, Items.DARK_OAK_LOG, Items.MANGROVE_LOG, Items.CHERRY_LOG, Items.CRIMSON_STEM, Items.WARPED_STEM,
            Items.OAK_LEAVES, Items.SPRUCE_LEAVES, Items.BIRCH_LEAVES, Items.JUNGLE_LEAVES, Items.ACACIA_LEAVES, Items.DARK_OAK_LEAVES, Items.MANGROVE_LEAVES, Items.CHERRY_LEAVES, Items.NETHER_WART_BLOCK, Items.WARPED_WART_BLOCK,
            Items.OAK_PLANKS, Items.SPRUCE_PLANKS, Items.BIRCH_PLANKS, Items.JUNGLE_PLANKS, Items.ACACIA_PLANKS, Items.DARK_OAK_PLANKS, Items.MANGROVE_PLANKS, Items.CHERRY_PLANKS, Items.CRIMSON_HYPHAE, Items.WARPED_HYPHAE,
            Items.OAK_BUTTON, Items.SPRUCE_BUTTON, Items.BIRCH_BUTTON, Items.JUNGLE_BUTTON, Items.ACACIA_BUTTON, Items.DARK_OAK_BUTTON, Items.MANGROVE_BUTTON, Items.CHERRY_BUTTON, Items.CRIMSON_BUTTON, Items.WARPED_BUTTON,
            Items.PINK_PETALS, Items.IRON_NUGGET, Items.GOLD_NUGGET, Items.STICK, Items.STRING, Items.BONE_MEAL,
            Items.GRASS_BLOCK, Items.COARSE_DIRT, Items.SNOW_BLOCK, Items.DEEPSLATE, Items.CALCITE, Items.TUFF,
            Items.ANDESITE, Items.DIORITE, Items.GRANITE, Items.BASALT, Items.BLACKSTONE, Items.END_STONE,
            Items.SOUL_SAND, Items.SOUL_SOIL, Items.CRIMSON_NYLIUM, Items.WARPED_NYLIUM, Items.CACTUS, Items.SEA_PICKLE,
            Items.KELP, Items.DRIED_KELP_BLOCK
    );

    @Override
    public Wildcards getType() {
        return Wildcards.HUNGER;
    }

    @Override
    public void tick() {
        if (currentSession.sessionLength == null || currentSession.sessionLength - currentSession.passedTime > 6000) {
            int currentVersion = (int) Math.floor(currentSession.passedTime / (SWITCH_DELAY));
            if (lastVersion != currentVersion) {
                lastVersion = currentVersion;
                newFoodRules();
            }
        }
        ticks++;
        if (ticks % 20 == 0) {
            for (ServerPlayerEntity player : PlayerUtils.getAllFunctioningPlayers()) {
                if (!player.hasStatusEffect(StatusEffects.HUNGER)) {
                    addHunger(player);
                }
            }
        }
    }

    @Override
    public void deactivate() {
        shuffledBefore = false;
        TaskScheduler.scheduleTask(1, OtherUtils::reloadServerNoUpdate);
        TaskScheduler.scheduleTask(10, Hunger::updateInventories);
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            player.removeStatusEffect(StatusEffects.HUNGER);
        }
        super.deactivate();
    }
    @Override
    public void activate() {
        TaskScheduler.scheduleTask(100, () -> {
            shuffleVersion = rnd.nextInt(0,100);
            shuffledBefore = false;
            lastVersion = -1;
            super.activate();
        });
    }

    public void newFoodRules() {
        List<ServerPlayerEntity> players = PlayerUtils.getAllFunctioningPlayers();
        SessionTranscript.newHungerRule();
        if (shuffledBefore) {
            PlayerUtils.playSoundToPlayers(players, SoundEvents.BLOCK_NOTE_BLOCK_PLING.value());
            PlayerUtils.sendTitleWithSubtitleToPlayers(players, Text.empty(), Text.of("§7Food is about to be randomised..."), 0, 140, 0);
            TaskScheduler.scheduleTask(40, WildcardManager::showDots);
            TaskScheduler.scheduleTask(140, () -> {
                addHunger();
                updateInventories();
                PlayerUtils.playSoundToPlayers(players, SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, 0.2f, 1);
                shuffleVersion++;
            });
        }
        else {
            TaskScheduler.scheduleTask(10, Hunger::updateInventories);
            shuffleVersion++;
        }
        shuffledBefore = true;
        addHunger();
        TaskScheduler.scheduleTask(1, OtherUtils::reloadServerNoUpdate);
        NetworkHandlerServer.sendUpdatePackets();
    }

    public static void updateInventories() {
        PlayerUtils.getAllFunctioningPlayers().forEach(Hunger::updateInventory);
    }

    public static void updateInventory(ServerPlayerEntity player) {
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;

            stack.set(DataComponentTypes.FOOD, stack.getDefaultComponents().get(DataComponentTypes.FOOD));
            //? if >= 1.21.2 {
            /*stack.set(DataComponentTypes.CONSUMABLE, stack.getDefaultComponents().get(DataComponentTypes.CONSUMABLE));
            *///?}

            ComponentChanges changes = stack.getComponentChanges();
            ItemStack newItem = new ItemStack(stack.getItem(), stack.getCount());
            newItem.applyChanges(changes);
            inventory.setStack(i, newItem);
        }

        PlayerUtils.updatePlayerInventory(player);
    }

    public static void addHunger() {
        PlayerUtils.getAllFunctioningPlayers().forEach(Hunger::addHunger);
    }
    public static void addHunger(ServerPlayerEntity player) {
        if (player == null) return;
        if (player.isSpectator()) return;
        if (HUNGER_EFFECT_LEVEL <= 0) return;
        StatusEffectInstance statusEffectInstance = new StatusEffectInstance(StatusEffects.HUNGER, -1, HUNGER_EFFECT_LEVEL-1, false, false, false);
        player.addStatusEffect(statusEffectInstance);
    }

    public static void onUseItem(ServerPlayerEntity player) {
        if (!player.hasStatusEffect(StatusEffects.HUNGER) && WildcardManager.isActiveWildcard(Wildcards.HUNGER)){
            addHunger(player);
        }
    }

    public static final List<Item> bannedFoodItems = List.of(
            Items.AIR, Items.ENDER_PEARL, Items.WIND_CHARGE, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE
    );

    //? if <= 1.21 {
    public static void defaultFoodComponents(Item item, ComponentMapImpl components) {
        if (item == null) return;
        if (bannedFoodItems.contains(item)) return;
        components.set(DataComponentTypes.FOOD, new FoodComponent(0, 0, false, 1.6f, Optional.empty(), List.of()));
    }
    //?} else {
    /*public static void defaultFoodComponents(Item item, MergedComponentMap components) {
        if (item == null) return;
        if (bannedFoodItems.contains(item)) return;
        components.set(DataComponentTypes.CONSUMABLE,
                new ConsumableComponent(ConsumableComponent.DEFAULT_CONSUME_SECONDS, UseAction.EAT, SoundEvents.ENTITY_GENERIC_EAT, true, List.of())
        );
        components.set(DataComponentTypes.FOOD, new FoodComponent(0, 0, false));
    }
    *///?}

    public static void finishUsing(Item item, ComponentMap normalComponents, LivingEntity entity) {
        if (!(entity instanceof ServerPlayerEntity player)) return;
        if (item == null) return;
        if (bannedFoodItems.contains(item)) return;

        int nutrition = 0;
        int saturation = 0;
        StatusEffectInstance effect = null;

        if (normalComponents.contains(DataComponentTypes.FOOD)) {
            effect = new StatusEffectInstance(StatusEffects.HUNGER, 3600, 7, false, false, false);
        }
        else {
            //Random effect
            int hash = getHash(item);
            if ((hash % 13) % 3 != 0) {
                int amplifier = hash % 5; // 0 -> 4
                int duration = ((3 + hash) % 18) * 20; // 1 -> 20 seconds
                RegistryEntry<StatusEffect> registryEntryEffect = effects.get(hash % effects.size());
                if (levelLimit.contains(registryEntryEffect) || commonItems.contains(item)) {
                    amplifier = 0;
                }
                if (durationLimit.contains(registryEntryEffect)) {
                    duration = 1;
                }
                effect = new StatusEffectInstance(registryEntryEffect, duration, amplifier);
            }

            // Random nutrition and saturation
            if (!commonItems.contains(item)) {
                nutrition = hash % 19 - 10; // -10 -> 8
                saturation = hash % 12 - 7; // -7 -> 4
                if (nutrition < 0) nutrition = 0;
                if (saturation < 0) saturation = 0;
                if (saturation > nutrition) saturation = nutrition;
            }
        }

        player.getHungerManager().add(nutrition, saturation);
        if (effect != null) {
            player.addStatusEffect(effect);
        }
    }

    private static int getHash(Item item) {
        String itemId = Registries.ITEM.getId(item).toString();
        return Math.abs((itemId.hashCode() + shuffleVersion) * 31);
    }
}
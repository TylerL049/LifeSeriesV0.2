package net.mat0u5.lifeseries.seasons.blacklist;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.seasons.season.doublelife.DoubleLife;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.superpowers.Superpowers;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.superpowers.SuperpowersWildcard;
import net.mat0u5.lifeseries.utils.other.OtherUtils;
import net.mat0u5.lifeseries.utils.other.TaskScheduler;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.mat0u5.lifeseries.utils.world.ItemStackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.mat0u5.lifeseries.Main.seasonConfig;
import static net.mat0u5.lifeseries.Main.server;

public class Blacklist {
    public List<Identifier> loadedListItemIdentifier;
    private List<Item> loadedListItem;
    private List<Block> loadedListBlock;
    private List<RegistryKey<Enchantment>> loadedListEnchants;
    private List<RegistryKey<Enchantment>> loadedBannedEnchants;

    private List<RegistryEntry<StatusEffect>> loadedBannedEffects;
    
    public boolean CREATIVE_IGNORE_BLACKLIST = true;

    public List<String> loadItemBlacklist() {
        if (seasonConfig == null) return new ArrayList<>();
        String raw = seasonConfig.BLACKLIST_ITEMS.get(seasonConfig);
        raw = raw.replaceAll("\\[","").replaceAll("]","").replaceAll(" ", "");
        if (raw.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(raw.split(",")));
    }

    public List<String> loadBlockBlacklist() {
        if (seasonConfig == null) return new ArrayList<>();
        String raw = seasonConfig.BLACKLIST_BLOCKS.get(seasonConfig);
        raw = raw.replaceAll("\\[","").replaceAll("]","").replaceAll(" ", "");
        if (raw.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(raw.split(",")));
    }

    public List<String> loadClampedEnchants() {
        if (seasonConfig == null) return new ArrayList<>();
        String raw = seasonConfig.BLACKLIST_CLAMPED_ENCHANTS.get(seasonConfig);
        raw = raw.replaceAll("\\[","").replaceAll("]","").replaceAll(" ", "");
        if (raw.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(raw.split(",")));
    }

    public List<String> loadBlacklistedEnchants() {
        if (seasonConfig == null) return new ArrayList<>();
        String raw = seasonConfig.BLACKLIST_BANNED_ENCHANTS.get(seasonConfig);
        raw = raw.replaceAll("\\[","").replaceAll("]","").replaceAll(" ", "");
        if (raw.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(raw.split(",")));
    }

    public List<String> loadBannedPotions() {
        if (seasonConfig == null) return new ArrayList<>();
        String raw = seasonConfig.BLACKLIST_BANNED_POTION_EFFECTS.get(seasonConfig);
        raw = raw.replaceAll("\\[","").replaceAll("]","").replaceAll(" ", "");
        if (raw.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(raw.split(",")));
    }

    public List<Item> getItemBlacklist() {
        if (loadedListItem != null) return loadedListItem;
        List<Item> newList = new ArrayList<>();
        List<Identifier> newListIdentifier = new ArrayList<>();

        if (seasonConfig != null) {
            if (!seasonConfig.SPAWNER_RECIPE.get(seasonConfig)) {
                newListIdentifier.add(Identifier.of("lifeseries", "spawner_recipe"));
            }
        }

        for (String itemId : loadItemBlacklist()) {
            if (!itemId.contains(":")) itemId = "minecraft:" + itemId;

            try {
                Identifier id = Identifier.of(itemId);
                RegistryKey<Item> key = RegistryKey.of(Registries.ITEM.getKey(), id);

                // Check if the block exists in the registry
                Item item = Registries.ITEM.get(key);
                if (item != null) {
                    newListIdentifier.add(id);
                    newList.add(item);
                } else {
                    OtherUtils.throwError("[CONFIG] Invalid item: " + itemId);
                }
            } catch (Exception e) {
                OtherUtils.throwError("[CONFIG] Error parsing item ID: " + itemId);
            }
        }

        loadedListItem = newList;
        loadedListItemIdentifier = newListIdentifier;
        return newList;
    }

    public List<Block> getBlockBlacklist() {
        if (loadedListBlock != null) return loadedListBlock;
        List<Block> newList = new ArrayList<>();

        for (String blockId : loadBlockBlacklist()) {
            if (!blockId.contains(":")) blockId = "minecraft:" + blockId;

            try {
                Identifier id = Identifier.of(blockId);
                RegistryKey<Block> key = RegistryKey.of(Registries.BLOCK.getKey(), id);

                // Check if the block exists in the registry
                Block block = Registries.BLOCK.get(key);
                if (block != null) {
                    newList.add(block);
                } else {
                    OtherUtils.throwError("[CONFIG] Invalid block: " + blockId);
                }
            } catch (Exception e) {
                OtherUtils.throwError("[CONFIG] Error parsing block ID: " + blockId);
            }
        }

        loadedListBlock = newList;
        return newList;
    }

    public List<RegistryKey<Enchantment>> getClampedEnchants() {
        if (server == null) return new ArrayList<>();

        if (loadedListEnchants != null) return loadedListEnchants;
        List<RegistryKey<Enchantment>> newList = new ArrayList<>();

        Registry<Enchantment> enchantmentRegistry = server.getRegistryManager()

                //? if <=1.21 {
                .get(RegistryKey.ofRegistry(Identifier.of("minecraft", "enchantment")));
                 //?} else
                /*.getOrThrow(RegistryKey.ofRegistry(Identifier.of("minecraft", "enchantment")));*/


        for (String enchantmentId : loadClampedEnchants()) {
            if (!enchantmentId.contains(":")) enchantmentId = "minecraft:" + enchantmentId;

            try {
                Identifier id = Identifier.of(enchantmentId);
                Enchantment enchantment = enchantmentRegistry.get(id);

                if (enchantment != null) {
                    newList.add(enchantmentRegistry.getKey(enchantment).orElseThrow());
                } else {
                    OtherUtils.throwError("[CONFIG] Invalid enchantment: " + enchantmentId);
                }
            } catch (Exception e) {
                OtherUtils.throwError("[CONFIG] Error parsing enchantment ID: " + enchantmentId);
            }
        }

        loadedListEnchants = newList;
        return newList;
    }

    public List<RegistryKey<Enchantment>> getBannedEnchants() {
        if (server == null) return new ArrayList<>();

        if (loadedBannedEnchants != null) return loadedBannedEnchants;
        List<RegistryKey<Enchantment>> newList = new ArrayList<>();

        Registry<Enchantment> enchantmentRegistry = server.getRegistryManager()

                //? if <=1.21 {
                .get(RegistryKey.ofRegistry(Identifier.of("minecraft", "enchantment")));
        //?} else
        /*.getOrThrow(RegistryKey.ofRegistry(Identifier.of("minecraft", "enchantment")));*/


        for (String enchantmentId : loadBlacklistedEnchants()) {
            if (!enchantmentId.contains(":")) enchantmentId = "minecraft:" + enchantmentId;

            try {
                Identifier id = Identifier.of(enchantmentId);
                Enchantment enchantment = enchantmentRegistry.get(id);

                if (enchantment != null) {
                    newList.add(enchantmentRegistry.getKey(enchantment).orElseThrow());
                } else {
                    OtherUtils.throwError("[CONFIG] Invalid enchantment: " + enchantmentId);
                }
            } catch (Exception e) {
                OtherUtils.throwError("[CONFIG] Error parsing enchantment ID: " + enchantmentId);
            }
        }

        loadedBannedEnchants = newList;
        return newList;
    }

    public List<RegistryEntry<StatusEffect>> getBannedEffects() {
        if (server == null) return new ArrayList<>();

        if (loadedBannedEffects != null) return loadedBannedEffects;
        List<RegistryEntry<StatusEffect>> newList = new ArrayList<>();

        Registry<StatusEffect> effectsRegistry = server.getRegistryManager()
        //? if <=1.21 {
        .get(RegistryKey.ofRegistry(Identifier.of("minecraft", "mob_effect")));
        //?} else
        /*.getOrThrow(RegistryKey.ofRegistry(Identifier.of("minecraft", "mob_effect")));*/

        for (String potionId : loadBannedPotions()) {
            if (!potionId.contains(":")) potionId = "minecraft:" + potionId;

            try {
                Identifier id = Identifier.of(potionId);
                StatusEffect enchantment = effectsRegistry.get(id);

                if (enchantment != null) {
                    newList.add(effectsRegistry.getEntry(enchantment));
                } else {
                    OtherUtils.throwError("[CONFIG] Invalid effect: " + potionId);
                }
            } catch (Exception e) {
                OtherUtils.throwError("[CONFIG] Error parsing effect ID: " + potionId);
            }
        }

        loadedBannedEffects = newList;
        return newList;
    }

    public void reloadBlacklist() {
        if (Main.server == null) return;

        CREATIVE_IGNORE_BLACKLIST = seasonConfig.CREATIVE_IGNORE_BLACKLIST.get(seasonConfig);
        
        loadedListItem = null;
        loadedListBlock = null;
        loadedListEnchants = null;
        loadedBannedEnchants = null;
        loadedBannedEffects = null;
        getItemBlacklist();
        getBlockBlacklist();
        getClampedEnchants();
        getBannedEnchants();
        getBannedEffects();
    }

    public ActionResult onBlockUse(ServerPlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (player.isCreative() && CREATIVE_IGNORE_BLACKLIST) return ActionResult.PASS;
        processItemStack(player, player.getStackInHand(hand));
        BlockPos blockPos = hitResult.getBlockPos();
        BlockState block = world.getBlockState(blockPos);
        if (block.isAir()) return ActionResult.PASS;
        if (getBlockBlacklist().contains(block.getBlock())) {
            world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    public ActionResult onBlockAttack(ServerPlayerEntity player, World world, BlockPos pos) {
        if (player.isCreative() && CREATIVE_IGNORE_BLACKLIST) return ActionResult.PASS;
        if (world.isClient()) return ActionResult.PASS;
        BlockState block = world.getBlockState(pos);
        if (block.isAir()) return ActionResult.PASS;
        if (getBlockBlacklist().contains(block.getBlock())) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    public void onCollision(ServerPlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (player.isCreative() && CREATIVE_IGNORE_BLACKLIST) return;
        processItemStack(player, stack);
    }

    public void onInventoryUpdated(ServerPlayerEntity player) {
        if (Main.server == null) return;
        PlayerInventory inventory = player.getInventory();
        if (player.isCreative() && CREATIVE_IGNORE_BLACKLIST) return;
        for (int i = 0; i < inventory.size(); i++) {
            processItemStack(player, inventory.getStack(i));
        }
        PlayerUtils.updatePlayerInventory(player);
    }

    public boolean isBlacklistedItemSimple(ItemStack itemStack) {
        return getItemBlacklist().contains(itemStack.getItem());
    }

    public boolean isBlacklistedItem(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (getItemBlacklist().contains(item)) return true;
        if (item != Items.POTION && item != Items.LINGERING_POTION && item != Items.SPLASH_POTION) return false;

        PotionContentsComponent potions = itemStack.getComponents().get(DataComponentTypes.POTION_CONTENTS);
        if (potions == null) return false;
        for (StatusEffectInstance effect : potions.getEffects()) {
            if (getBannedEffects().contains(effect.getEffectType())) return true;
        }
        return false;
    }

    public void processItemStack(ServerPlayerEntity player, ItemStack itemStack) {
        if (player.isCreative() && CREATIVE_IGNORE_BLACKLIST) return;
        if (itemStack.isEmpty()) return;
        if (itemStack.getItem() == Items.AIR) return;
        if (isBlacklistedItem(itemStack) && !ItemStackUtils.hasCustomComponentEntry(itemStack, "IgnoreBlacklist")) {
            itemStack.setCount(0);
            player.getInventory().updateItems();
            return;
        }

        if (ItemStackUtils.hasCustomComponentEntry(itemStack, "FromSuperpower")) {
            boolean remove = true;
            if (ItemStackUtils.hasCustomComponentEntry(itemStack, "WindChargeSuperpower")) {
                if (SuperpowersWildcard.hasActivatedPower(player, Superpowers.WIND_CHARGE)) {
                    remove = false;
                }
            }
            //? if >= 1.21.2 {
            /*if (ItemStackUtils.hasCustomComponentEntry(itemStack, "FlightSuperpower")) {
                if (SuperpowersWildcard.hasActivePower(player, Superpowers.FLIGHT)) {
                    remove = false;
                }
            }
            *///?}
            if (remove) {
                itemStack.setCount(0);
                player.getInventory().updateItems();
                return;
            }
            return;
        }
        ItemEnchantmentsComponent enchants = itemStack.getComponents().get(DataComponentTypes.ENCHANTMENTS);
        ItemEnchantmentsComponent enchantsStored = itemStack.getComponents().get(DataComponentTypes.STORED_ENCHANTMENTS);
        if (enchants != null) {
            itemStack.set(DataComponentTypes.ENCHANTMENTS, clampAndBlacklistEnchantments(enchants));
        }
        if (enchantsStored != null) {
            itemStack.set(DataComponentTypes.STORED_ENCHANTMENTS, clampAndBlacklistEnchantments(enchantsStored));
        }
    }

    public ItemEnchantmentsComponent clampAndBlacklistEnchantments(ItemEnchantmentsComponent enchants) {
        ItemEnchantmentsComponent afterBlacklist = blacklistEnchantments(enchants);
        clampEnchantments(afterBlacklist);
        return afterBlacklist;
    }

    public ItemEnchantmentsComponent blacklistEnchantments(ItemEnchantmentsComponent enchants) {
        if (enchants.isEmpty()) return enchants;
        List<RegistryKey<Enchantment>> banned = getBannedEnchants();
        if (banned.isEmpty()) return enchants;
        List<it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<RegistryEntry<Enchantment>>> toRemove = new ArrayList<>();
        for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<RegistryEntry<Enchantment>> enchant : enchants.getEnchantmentEntries()) {
            Optional<RegistryKey<Enchantment>> enchantRegistry = enchant.getKey().getKey();
            if (enchantRegistry.isEmpty()) continue;
            if (banned.contains(enchantRegistry.get())) {
                toRemove.add(enchant);
            }
        }
        if (toRemove.isEmpty()) return enchants;
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);

        for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<RegistryEntry<Enchantment>> enchant : enchants.getEnchantmentEntries()) {
            if (toRemove.contains(enchant)) continue;
            builder.add(enchant.getKey(), enchant.getIntValue());
        }

        return builder.build();
    }

    public void clampEnchantments(ItemEnchantmentsComponent enchants) {
        List<RegistryKey<Enchantment>> clamp = getClampedEnchants();
        for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<RegistryEntry<Enchantment>> enchant : enchants.getEnchantmentEntries()) {
            Optional<RegistryKey<Enchantment>> enchantRegistry = enchant.getKey().getKey();
            if (enchantRegistry.isEmpty()) continue;
            if (clamp.contains(enchantRegistry.get())) {
                enchant.setValue(1);
            }
        }
    }
}
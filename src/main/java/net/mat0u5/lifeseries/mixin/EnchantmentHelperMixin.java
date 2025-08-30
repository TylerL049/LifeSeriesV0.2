package net.mat0u5.lifeseries.mixin;

import com.google.common.collect.Lists;
import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.superpowers.Superpowers;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.superpowers.SuperpowersWildcard;
import net.mat0u5.lifeseries.utils.world.ItemStackUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static net.mat0u5.lifeseries.Main.blacklist;
import static net.mat0u5.lifeseries.Main.seasonConfig;
//? if >= 1.21.2
/*import net.mat0u5.lifeseries.utils.player.PlayerUtils;*/

@Mixin(value = EnchantmentHelper.class, priority = 1)
public class EnchantmentHelperMixin {
    @Inject(method = "getPossibleEntries", at = @At("HEAD"), cancellable = true)
    private static void getPossibleEntries(int level, ItemStack stack, Stream<RegistryEntry<Enchantment>> possibleEnchantments, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        if (!Main.isLogicalSide()) return;
        if (Main.server == null) return;

        if (ItemStackUtils.hasCustomComponentEntry(stack, "NoEnchants") || ItemStackUtils.hasCustomComponentEntry(stack, "NoModifications")) {
            cir.setReturnValue(Lists.<EnchantmentLevelEntry>newArrayList());
            return;
        }

        if (seasonConfig.CUSTOM_ENCHANTER_ALGORITHM.get(seasonConfig)) {
            ls$customEnchantmentTableAlgorithm(level, stack, possibleEnchantments, cir);
        }
        else {
            ls$blacklistEnchantments(level, stack, possibleEnchantments, cir);
        }
    }

    @Unique
    private static void ls$blacklistEnchantments(int level, ItemStack stack, Stream<RegistryEntry<Enchantment>> possibleEnchantments, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        List<EnchantmentLevelEntry> list = Lists.<EnchantmentLevelEntry>newArrayList();
        boolean bl = stack.isOf(Items.BOOK);
        possibleEnchantments.filter(enchantment -> ((Enchantment)enchantment.value()).isPrimaryItem(stack) || bl).forEach(enchantmentx -> {
            Enchantment enchantment = (Enchantment)enchantmentx.value();
            Optional<RegistryKey<Enchantment>> enchantRegistryKey = enchantmentx.getKey();
            boolean isRegistryPresent = enchantRegistryKey.isPresent();
            if (isRegistryPresent && !blacklist.getBannedEnchants().contains(enchantRegistryKey.get())) {
                for (int j = enchantment.getMaxLevel(); j >= enchantment.getMinLevel(); j--) {
                    if (level >= enchantment.getMinPower(j) && level <= enchantment.getMaxPower(j)) {
                        if (isRegistryPresent && blacklist.getClampedEnchants().contains(enchantRegistryKey.get())) {
                            list.add(new EnchantmentLevelEntry(enchantmentx, 1));
                        }
                        else {
                            list.add(new EnchantmentLevelEntry(enchantmentx, j));
                        }
                        break;
                    }
                }
            }
        });
        cir.setReturnValue(list);
    }

    @Unique
    private static void ls$customEnchantmentTableAlgorithm(int level, ItemStack stack, Stream<RegistryEntry<Enchantment>> possibleEnchantments, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        List<EnchantmentLevelEntry> list = new ArrayList<>();
        boolean bl = stack.isOf(Items.BOOK);
        possibleEnchantments.filter(enchantment -> ((Enchantment)enchantment.value()).isPrimaryItem(stack) || bl).forEach(enchantmentx -> {
            Enchantment enchantment = (Enchantment)enchantmentx.value();
            Optional<RegistryKey<Enchantment>> enchantRegistryKey = enchantmentx.getKey();
            if (enchantRegistryKey.isPresent() && !blacklist.getBannedEnchants().contains(enchantRegistryKey.get())) {
                if (blacklist.getClampedEnchants().contains(enchantRegistryKey.get())) {
                    list.add(new EnchantmentLevelEntry(enchantmentx, 1));
                }
                else {
                    for (int j = enchantment.getMaxLevel(); j >= enchantment.getMinLevel(); j--) {
                        if (j == 1) {
                            if (enchantment.getMaxLevel() <= 3 || level < 4) {
                                list.add(new EnchantmentLevelEntry(enchantmentx, j));
                            }
                        }
                        else if (j == 2 && level > 4 && enchantment.getMaxLevel() > 3) {
                            list.add(new EnchantmentLevelEntry(enchantmentx, j));
                        }
                        else if (j == 2 && level > 6 && enchantment.getMaxLevel() >= 3) {
                            list.add(new EnchantmentLevelEntry(enchantmentx, j));
                        }
                        else if (j == 3 && level > 6 && enchantment.getMaxLevel() > 3) {
                            list.add(new EnchantmentLevelEntry(enchantmentx, j));
                        }
                    }
                }
            }
        });
        cir.setReturnValue(list);
    }

    @Inject(
            method = "onTargetDamaged(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;)V", at = @At("HEAD")
    )
    private static void onTargetDamaged(ServerWorld world, Entity victimEntity, DamageSource damageSource, CallbackInfo ci) {
        if (!Main.isLogicalSide()) return;
        if (!(victimEntity instanceof ServerPlayerEntity victim)) return;
        if (damageSource == null) return;
        if (damageSource.getAttacker() == null) return;
        if (!SuperpowersWildcard.hasActivatedPower(victim, Superpowers.SUPER_PUNCH)) return;
        //? if <= 1.21 {
        damageSource.getAttacker().damage(victim.getDamageSources().thorns(victim), 1F);
        //?} else {
        /*damageSource.getAttacker().damage(PlayerUtils.getServerWorld(victim), victim.getDamageSources().thorns(victim), 1F);
         *///?}
    }
}

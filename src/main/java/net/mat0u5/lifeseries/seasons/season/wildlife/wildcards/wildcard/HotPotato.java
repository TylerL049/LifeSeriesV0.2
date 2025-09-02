package net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard;


import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.mat0u5.lifeseries.seasons.other.WatcherManager;
import net.mat0u5.lifeseries.utils.other.TaskScheduler;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;


import java.util.List;
import java.util.Random;
import java.util.UUID;


import static net.mat0u5.lifeseries.Main.livesManager;


public class HotPotato extends Wildcard {


private ServerPlayerEntity potatoHolder;
private ServerPlayerEntity lastHolder;
private boolean active;
private boolean potatoAssigned;
private UUID potatoUuid;


private static final int DELAY_TICKS = 200;
private static final int FUSE_DURATION = 13000;
private static final String NBT_KEY = "HotPotatoUUID";


public HotPotato() {
this.active = false;
this.potatoAssigned = false;
}


@Override
public Wildcards getType() {
return Wildcards.HOT_POTATO;
}


@Override
public void activate() {
this.active = true;
this.potatoAssigned = false;
this.potatoUuid = UUID.randomUUID();
TaskScheduler.scheduleTask(DELAY_TICKS, this::assignPotatoToRandomPlayer);
}


private void assignPotatoToRandomPlayer() {
List<ServerPlayerEntity> candidates = livesManager.getAlivePlayers();
candidates.removeIf(WatcherManager::isWatcher);
if (candidates.isEmpty()) {
reset();
return;
}
potatoHolder = candidates.get(new Random().nextInt(candidates.size()));
givePotato(potatoHolder);
potatoAssigned = true;
PlayerUtils.sendTitle(
potatoHolder,
Text.literal("You have the Hot Potato!").formatted(Formatting.RED),
20, 40, 20
);
TaskScheduler.scheduleTask(FUSE_DURATION, this::explode);
}


public void passTo(ServerPlayerEntity nextPlayer) {
if (!active || !potatoAssigned || nextPlayer == null || nextPlayer == potatoHolder) return;
lastHolder = potatoHolder;
potatoHolder = nextPlayer;
removePotato(lastHolder);
givePotato(potatoHolder);
PlayerUtils.sendTitle(
potatoHolder,
Text.literal("You have the Hot Potato!").formatted(Formatting.RED),
20, 40, 20
);
}
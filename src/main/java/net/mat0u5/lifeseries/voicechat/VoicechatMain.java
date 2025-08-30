package net.mat0u5.lifeseries.voicechat;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.entity.triviabot.TriviaBot;
import net.mat0u5.lifeseries.seasons.season.Seasons;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.WildcardManager;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.superpowers.Superpowers;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.superpowers.SuperpowersWildcard;
import net.mat0u5.lifeseries.seasons.season.wildlife.wildcards.wildcard.superpowers.superpower.Listening;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.mat0u5.lifeseries.voicechat.soundeffects.RadioEffect;
import net.mat0u5.lifeseries.voicechat.soundeffects.RoboticVoice;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

import static net.mat0u5.lifeseries.Main.currentSeason;

public class VoicechatMain implements VoicechatPlugin {

    private OpusEncoder encoder;
    private OpusDecoder decoder;

    @Override
    public String getPluginId() {
        return "lifeseries";
    }

    @Override
    public void initialize(VoicechatApi api) {
        Main.LOGGER.info("Life Series Voice Chat plugin initialized!");
        this.encoder = api.createEncoder();
        this.decoder = api.createDecoder();
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onAudioPacket);
    }

    private void onAudioPacket(MicrophonePacketEvent event) {
        roboticVoice(event);
        listeningPower(event);
    }

    private void roboticVoice(MicrophonePacketEvent event) {
        if (currentSeason.getSeason() != Seasons.WILD_LIFE) {
            return;
        }
        if (!WildcardManager.isActiveWildcard(Wildcards.TRIVIA)) {
            return;
        }
        try {
            VoicechatConnection connection = event.getSenderConnection();
            if (connection == null) return;
            UUID senderUUID = connection.getPlayer().getUuid();
            if (!TriviaBot.cursedRoboticVoicePlayers.contains(senderUUID)) {
                return;
            }

            byte[] opusData = event.getPacket().getOpusEncodedData();
            byte[] processedOpusData = processOpusAudioForRobot(senderUUID, opusData);

            event.getPacket().setOpusEncodedData(processedOpusData);
        } catch (Exception e) {
            Main.LOGGER.error("Error processing audio", e);
        }
    }

    private void listeningPower(MicrophonePacketEvent event) {
        if (currentSeason.getSeason() != Seasons.WILD_LIFE) {
            return;
        }
        if (Listening.listeningPlayers.isEmpty()) {
            return;
        }

        MicrophonePacket voicePacket = event.getPacket();
        VoicechatConnection connection = event.getSenderConnection();
        if (connection == null) {
            return;
        }

        UUID senderUUID = connection.getPlayer().getUuid();
        Position senderPosition = connection.getPlayer().getPosition();

        VoicechatServerApi api = event.getVoicechat();
        for (UUID uuid : Listening.listeningPlayers) {
            if (uuid == senderUUID) {
                continue;
            }
            VoicechatConnection playerConnection = api.getConnectionOf(uuid);
            ServerPlayerEntity player = PlayerUtils.getPlayer(uuid);

            if (playerConnection == null || player == null) {
                continue;
            }
            Vec3d lookingAt = null;
            if (SuperpowersWildcard.hasActivatedPower(player, Superpowers.LISTENING)) {
                if (SuperpowersWildcard.getSuperpowerInstance(player) instanceof Listening listeningPower) {
                    lookingAt = listeningPower.lookingAt;
                }
            }

            if (lookingAt == null || api.getBroadcastRange() == 0) {
                continue;
            }

            Vec3d senderPos = new Vec3d(senderPosition.getX(), senderPosition.getY(), senderPosition.getZ());
            double distanceFromSound = senderPos.distanceTo(lookingAt);
            double maxDistance = Math.min(api.getBroadcastRange(), Listening.MAX_RANGE);
            if (distanceFromSound > maxDistance) {
                continue;
            }
            double scaled = api.getBroadcastRange()/Listening.MAX_RANGE;
            if ((distanceFromSound*scaled) > player.getPos().distanceTo(senderPos)) {
                continue;
            }

            byte[] processedAudio = processOpusAudioForRadio(voicePacket.getOpusEncodedData().clone());
            LocationalSoundPacket processedPacket = voicePacket.locationalSoundPacketBuilder()
                    .position(api.createPosition(player.getX(), player.getY() + (distanceFromSound*scaled), player.getZ()))
                    .distance((float)api.getBroadcastRange())
                    .opusEncodedData(processedAudio)
                    .build();
            api.sendLocationalSoundPacketTo(playerConnection, processedPacket);
        }
    }

    private byte[] processOpusAudioForRadio(byte[] opusData) {
        try {
            short[] pcmData = decoder.decode(opusData);
            if (pcmData == null) {
                Main.LOGGER.warn("Failed to decode Opus data");
                return opusData;
            }

            short[] processedPcm = RadioEffect.applyEffect(pcmData);

            return encoder.encode(processedPcm);
        } catch (Exception e) {
            Main.LOGGER.error("Error processing Opus audio", e);
            return opusData;
        }
    }

    private byte[] processOpusAudioForRobot(UUID uuid, byte[] opusData) {
        try {
            short[] pcmData = decoder.decode(opusData);
            if (pcmData == null) {
                Main.LOGGER.warn("Failed to decode Opus data");
                return opusData;
            }

            short[] processedPcm = RoboticVoice.applyEffect(uuid, pcmData);

            return encoder.encode(processedPcm);
        } catch (Exception e) {
            Main.LOGGER.error("Error processing Opus audio", e);
            return opusData;
        }
    }
}
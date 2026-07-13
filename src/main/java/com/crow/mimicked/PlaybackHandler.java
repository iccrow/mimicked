package com.crow.mimicked;

import com.crow.mimicked.audio.AudioConverter;
import com.crow.mimicked.audio.AudioFileManager;
import com.crow.mimicked.audio.fx.SFX;
import com.crow.mimicked.audio.fx.SFXConfig;
import com.mojang.logging.LogUtils;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.ClientEntityAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.EntityAudioChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@EventBusSubscriber(modid = Mimicked.MODID)
public class PlaybackHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ConcurrentMap<UUID, UUID> speaking = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) throws IOException, UnsupportedAudioFileException {
        Player tickingPlayer = event.getEntity();

        if (EffectiveSide.get().isClient() && ModNetwork.isServerSidePresent() && Config.PREFER_SERVER_SIDE.get())
            return;
        if (EffectiveSide.get().isClient() && event.getEntity() != Minecraft.getInstance().player)
            return;

        boolean overdrive = checkForOverdrive(tickingPlayer, Plugin.api.getVoiceChatDistance() / 2.0);
        double sparsity = overdrive ? Config.OVERDRIVE_SPARSITY.get() : Config.SPARSITY.get();

        if (Math.random() > 1.0 / (20 * 60 * sparsity))
            return;

        if (!AudioFileManager.hasClips())
            return;

        Entity theChosenOne = chooseHostEntity(tickingPlayer, Plugin.api.getVoiceChatDistance() / 2.0, overdrive);
        if (theChosenOne == null) {
            if (Config.DEBUG.get())
                LOGGER.info("No host entity found");
            return;
        }

        if (speaking.getOrDefault(theChosenOne.getUUID(), null) != null) {
            if (Config.DEBUG.get())
                LOGGER.info("Skipping mimic event because the chosen host is already speaking");

            return;
        }

        for (int i = 0; i < 5; i++) {
            UUID mimicked = AudioFileManager.getRandomPlayer();

            if (mimicked.equals(tickingPlayer.getUUID()) && Config.DISABLE_SELF.get())
                continue;

            File file = AudioFileManager.getRandomAudioFile(mimicked);
            if (file == null)
                continue;

            short[] samples = AudioFileManager.readFromAudioFile(file);
            if (samples.length < 100)
                return;

            double sfxChance = overdrive ? SFXConfig.SFX_OVERDRIVE_CHANCE.get() : SFXConfig.SFX_CHANCE.get();
            double sfxOverlapChange = overdrive ? SFXConfig.SFX_OVERDRIVE_OVERLAP_CHANCE.get() : SFXConfig.SFX_OVERLAP_CHANCE.get();
            int maxOverlaps = overdrive ? SFXConfig.SFX_OVERDRIVE_MAX_OVERLAP.get() : SFXConfig.SFX_MAX_OVERLAP.get();
            float[] floats = AudioConverter.toFloat(samples);
            boolean appliedSFX = false;
            for (int j = 0; j < maxOverlaps; j++) {
                if (j == 0 && Math.random() > sfxChance || j > 0 && Math.random() > sfxOverlapChange)
                    break;
                try {
                    SFX sfx = SFX.random();
                    if (sfx == null)
                        break;

                    if (Config.DEBUG.get())
                        LOGGER.info("Applying SFX #{}: {}", j + 1, sfx.getId());

                    floats = sfx.apply(floats);
                    appliedSFX = true;
                } catch (Exception | Error e) {
                    if (Config.DEBUG.get()) {
                        LOGGER.info("Failed to apply SFX: {}", e.getMessage());
                    }
                }
            }

            if (appliedSFX)
                samples = AudioConverter.toShort(floats);

            if (EffectiveSide.get().isClient()) {
                ClientEntityAudioChannel channel = Plugin.capi.createEntityAudioChannel(theChosenOne.getUUID(), Plugin.capi.fromEntity(theChosenOne));
                if (channel == null)
                    return;

                if (Config.DEBUG.get())
                    LOGGER.info("Playing audio to client");

                channel.play(samples);
            } else {
                EntityAudioChannel channel = Plugin.sapi.createEntityAudioChannel(theChosenOne.getUUID(), Plugin.sapi.fromEntity(theChosenOne));
                if (channel == null)
                    return;

                AudioPlayer player = Plugin.sapi.createAudioPlayer(channel, Plugin.sapi.createEncoder(), samples);

                if (Config.DEBUG.get())
                    LOGGER.info("Playing audio to server");

                speaking.put(theChosenOne.getUUID(), mimicked);
                player.startPlaying();

                player.setOnStopped(() -> speaking.remove(theChosenOne.getUUID()));

            }

            if (!overdrive && Math.random() < Config.DELETION_CHANCE.get()) {

                if (Config.DEBUG.get())
                    LOGGER.info("Deleting audio file");

                file.delete();
            }

            return;
        }
    }

    private static boolean checkForOverdrive(Player player, double radius) {
        AABB box = new AABB(
                player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                player.getX() + radius, player.getY() + radius, player.getZ() + radius
        );

        List<Entity> entities = player.level().getEntities(player, box, e -> !(e instanceof Player) && e instanceof LivingEntity);

        if (entities.isEmpty()) return false;

        List<Entity> candidates;
        if (Config.OVERDRIVE_HOSTS.get().isEmpty())
            return false;
        else
            candidates = entities.stream().filter(e -> {
                for (String s : Config.OVERDRIVE_HOSTS.get()) {
                    if (
                            EntityType.getKey(e.getType()).toString().equals(s) || (
                                    s.startsWith("@") && EntityType.getKey(e.getType()).getNamespace().equals(s.substring(1))
                            )
                    ) return true;
                }
                return false;
            }).toList();

        return !candidates.isEmpty();
    }

    private static Entity chooseHostEntity(Player player, double radius, boolean overdrive) {
        AABB box = new AABB(
                player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                player.getX() + radius, player.getY() + radius, player.getZ() + radius
        );

        List<Entity> entities = player.level().getEntities(player, box, e -> !(e instanceof Player) && e instanceof LivingEntity);

        if (entities.isEmpty()) return null;

        Random rand = new Random();

        List<Entity> candidates;
        List<? extends String> whitelist = overdrive ? Config.OVERDRIVE_HOSTS.get() : Config.HOST_WHITELIST.get();
        if (whitelist.isEmpty())
            candidates = entities.stream().filter(e -> (e instanceof Monster monster) && !monster.isNoAi()).toList();
        else
            candidates = entities.stream().filter(e -> {
                for (String s : whitelist) {
                    if (
                            EntityType.getKey(e.getType()).toString().equals(s) || (
                                    s.startsWith("@") && EntityType.getKey(e.getType()).getNamespace().equals(s.substring(1))
                            )
                    ) return true;
                }
                return false;
            }).toList();

        if (candidates.isEmpty())
            if (whitelist.isEmpty())
                return entities.get(rand.nextInt(entities.size()));
            else
                return null;

        return candidates.get(rand.nextInt(candidates.size()));
    }

    public static boolean isEntityMimickingPlayer(UUID entity, UUID player) {
        return speaking.containsKey(entity) && speaking.get(entity).equals(player);
    }
}

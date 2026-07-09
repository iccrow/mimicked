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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Mod.EventBusSubscriber(modid = Mimicked.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlaybackHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ConcurrentMap<UUID, Boolean> speaking = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) throws IOException, UnsupportedAudioFileException {
        if (event.phase != TickEvent.Phase.END)
            return;
        if (event.side.isClient() && ModNetwork.isServerSidePresent() && Config.PREFER_SERVER_SIDE.get())
            return;
        if (event.side.isClient() && event.player != Minecraft.getInstance().player)
            return;

        if (Math.random() > 1.0 / (20 * 60 * Config.SPARSITY.get()))
            return;

        if (!AudioFileManager.hasClips())
            return;

        Entity theChosenOne = chooseHostEntity(event.player, Plugin.capi.getVoiceChatDistance()/2.0);
        if (theChosenOne == null) {
            if (Config.DEBUG.get())
                LOGGER.info("No host entity found");
            return;
        }

        for (int i = 0; i < 5; i++) {
            UUID mimicked = AudioFileManager.getRandomPlayer();

            if (mimicked.equals(event.player.getUUID()) && Config.DISABLE_SELF.get())
                continue;

            File file = AudioFileManager.getRandomAudioFile(mimicked);
            if (file == null)
                continue;

            short[] samples = AudioFileManager.readFromAudioFile(file);
            if (samples.length < 100)
                return;

            if (Math.random() < SFXConfig.SFX_CHANCE.get()) {
                float[] floats = AudioConverter.toFloat(samples);
                SFX sfx = SFX.random();
                if (sfx != null) {
                    if (Config.DEBUG.get())
                        LOGGER.info("Applying SFX: {}", sfx.getId());

                    floats = sfx.apply(floats);

                    if (Math.random() < SFXConfig.SFX_OVERLAP_CHANCE.get()) {
                        SFX sfx2 = SFX.random();
                        if (sfx2 != null) {
                            if (Config.DEBUG.get())
                                LOGGER.info("Applying Overlapping SFX: {}", sfx.getId());

                            floats = sfx2.apply(floats);
                            samples = AudioConverter.toShort(floats);
                        }
                    } else {
                        samples = AudioConverter.toShort(floats);
                    }
                }
            }

            if (event.side.isClient()) {
                ClientEntityAudioChannel channel = Plugin.capi.createEntityAudioChannel(theChosenOne.getUUID(), Plugin.capi.fromEntity(theChosenOne));
                if (channel == null)
                    return;

                if (Config.DEBUG.get())
                    LOGGER.info("Playing audio to client");

                channel.play(samples);
            } else {
                EntityAudioChannel channel = Plugin.sapi.createEntityAudioChannel(theChosenOne.getUUID(), Plugin.capi.fromEntity(theChosenOne));
                if (channel == null)
                    return;

                AudioPlayer player = Plugin.sapi.createAudioPlayer(channel, Plugin.sapi.createEncoder(), samples);
                if (!speaking.getOrDefault(mimicked, false)) {

                    if (Config.DEBUG.get())
                        LOGGER.info("Playing audio to server");

                    speaking.put(mimicked, true);
                    player.startPlaying();

                    player.setOnStopped(() -> {
                        speaking.put(mimicked, false);
                    });
                } else {
                    if (Config.DEBUG.get())
                        LOGGER.info("Skipping audio to server because another mimic clip of the same player is currently playing");
                }

            }

            if (Math.random() < Config.DELETION_CHANCE.get()) {

                if (Config.DEBUG.get())
                    LOGGER.info("Deleting audio file");

                file.delete();
            }

            return;
        }
    }

    public static Entity chooseHostEntity(Player player, double radius) {
        AABB box = new AABB(
                player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                player.getX() + radius, player.getY() + radius, player.getZ() + radius
        );

        List<Entity> entities = player.level().getEntities(player, box, e -> !(e instanceof Player) && e instanceof LivingEntity);

        if (entities.isEmpty()) return null;

        Random rand = new Random();

        List<Entity> candidates;
        if (Config.HOST_WHITELIST.get().isEmpty())
            candidates = entities.stream().filter(e -> (e instanceof Monster monster) && !monster.isNoAi()).toList();
        else
            candidates = entities.stream().filter(e -> Config.HOST_WHITELIST.get().contains(EntityType.getKey(e.getType()).toString())).toList();

        if (candidates.isEmpty())
            if (Config.HOST_WHITELIST.get().isEmpty())
                return entities.get(rand.nextInt(entities.size()));
            else
                return null;

        return candidates.get(rand.nextInt(candidates.size()));
    }
}

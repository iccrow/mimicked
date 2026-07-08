package com.crow.mimicked;

import com.crow.mimicked.common.PCMStorage;
import com.crow.mimicked.common.audio.AudioFileManager;
import com.crow.mimicked.common.audio.SelectionFilters;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.sound.sampled.*;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Mimicked.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PacketHandler {

    private static final Map<UUID, PCMStorage> packets = new HashMap<>();

    public static void receivePacket(ClientReceiveSoundEvent.EntitySound e) {
        UUID uuid = e.getEntityId();
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null &&
                        !mc.getConnection().getOnlinePlayerIds().contains(uuid))
            return;

        PCMStorage storage = packets.computeIfAbsent(uuid, k -> new PCMStorage(48000*Config.MAX_CLIP_LENGTH.get()));
        short[] audio = e.getRawAudio();
        storage.appendPacket(audio);
    }

    public static void receivePacket(ClientSoundEvent e) {
        if (Minecraft.getInstance().player == null || Config.DISABLE_SELF.get())
            return;

        UUID uuid = Minecraft.getInstance().player.getUUID();
        PCMStorage storage = packets.computeIfAbsent(uuid, k -> new PCMStorage(48000*Config.MAX_CLIP_LENGTH.get()));
        short[] audio = e.getRawAudio();
        storage.appendPacket(audio);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, PCMStorage>> it = packets.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, PCMStorage> entry = it.next();
            if (now - entry.getValue().getLastUpdateTime() < Config.TIMEOUT.get())
                continue;

            UUID uuid = entry.getKey();
            PCMStorage storage = entry.getValue();
            it.remove();

            File eDir = FMLPaths.GAMEDIR.get()
                    .resolve("mimicked")
                    .resolve(uuid.toString())
                    .toFile();
            eDir.mkdirs();

            File[] files = eDir.listFiles();
            int count = files == null ? 0 : files.length;

            File file = eDir.toPath().resolve(now + ".wav")
                    .toFile();

            if (!Mimicked.valids.contains(uuid))
                Mimicked.valids.add(uuid);

            double seconds = storage.getDuration();

            if (seconds < 1 ||
                    SelectionFilters.isMostlySilent(storage) ||
                    Math.random() < Config.RANDOMNESS.get())
                continue;

            if (
                    count >= Config.MAX_CLIP_STORAGE.get() &&
                            Math.random() > Config.REPLACEMENT_CHANCE.get()
            ) AudioFileManager.removeRandomClip(eDir);
            else if (count >= Config.MAX_CLIP_STORAGE.get()) continue;

            AudioFileManager.saveWav(storage, file);
        }
    }
}

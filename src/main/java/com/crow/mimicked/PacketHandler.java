package com.crow.mimicked;

import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.sound.sampled.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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

            File file = FMLPaths.GAMEDIR.get()
                    .resolve("mimicked")
                    .resolve(uuid.toString())
                    .toFile();
            file.mkdirs();

            File[] files = file.listFiles();
            int count = files == null ? 0 : files.length;

            file = file.toPath().resolve(now + ".wav")
                    .toFile();

            if (!Mimicked.valids.contains(uuid))
                Mimicked.valids.add(uuid);

            double seconds = storage.getDuration();

            if (
                    (
                        count >= Config.MAX_CLIP_STORAGE.get() &&
                                Math.random() > Config.REPLACEMENT_CHANCE.get()
                    ) ||
                    seconds < 1 ||
                    isMostlySilent(storage) ||
                    Math.random() < Config.RANDOMNESS.get())
                continue;

            saveWav(storage, file);
        }
    }

    private static boolean isMostlySilent(PCMStorage storage) {
        short[] audio = storage.getAllSamples();
        long sum = 0;
        int count = 0;

        for (short s : audio) {
            sum += (long) s * s;
            if (s > Config.AMPLITUDE_THRESHOLD.get())
                count++;
        }

        double mean = sum / (double) audio.length;
        double rms = Math.sqrt(mean);

        double ratio = (double) count / audio.length;

        return rms < Config.RMS_THRESHOLD.get() || ratio < Config.LOUD_THRESHOLD.get();
    }

    private static void saveWav(PCMStorage storage, File file) {
        try {
            AudioFormat format = new AudioFormat(
                    48000,
                    16,
                    1,
                    true,
                    false
            );

            byte[] audio = storage.getByteData();
            storage.clear();

            ByteArrayInputStream bais = new ByteArrayInputStream(audio);

            AudioInputStream ais = new AudioInputStream(
                    bais,
                    format,
                    audio.length / 2
            );

            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
        } catch (IOException ignored) {}
    }
}

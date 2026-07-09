package com.crow.mimicked.audio;

import com.crow.mimicked.Config;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class AudioFileManager {

    public static ConcurrentMap<UUID, PCMStorage> packets = new ConcurrentHashMap<>();
    private static List<UUID> valids = new ArrayList<>();
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        File file = FMLPaths.GAMEDIR.get().resolve("mimicked").toFile();
        if (!file.exists())
            file.mkdirs();

        File[] files = file.listFiles();
        if (files == null)
            return;

        valids = Arrays.stream(files).filter(File::isDirectory).map(f -> UUID.fromString(f.getName())).collect(Collectors.toList());
    }

    public static PCMStorage getPCMStorage(UUID uuid) {
        return packets.computeIfAbsent(uuid, k -> new PCMStorage((int) (48000*Config.MAX_CLIP_LENGTH.get())));
    }

    public static List<UUID> checkTimeouts() {
        List<UUID> removed = new ArrayList<>();
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, PCMStorage>> it = packets.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, PCMStorage> entry = it.next();
            if (now - entry.getValue().getLastUpdateTime() < Config.TIMEOUT.get())
                continue;

            UUID uuid = entry.getKey();
            PCMStorage storage = entry.getValue();
            it.remove();
            removed.add(uuid);

            File eDir = FMLPaths.GAMEDIR.get()
                    .resolve("mimicked")
                    .resolve(uuid.toString())
                    .toFile();
            eDir.mkdirs();

            File[] files = eDir.listFiles();
            int count = files == null ? 0 : files.length;

            File file = eDir.toPath().resolve(now + ".wav")
                    .toFile();

            if (!valids.contains(uuid))
                valids.add(uuid);

            double seconds = storage.getDuration();

            if (seconds < 1 ||
                    SelectionFilters.isMostlySilent(storage) ||
                    Math.random() > Config.RECORDING_CHANCE.get())
                continue;

            if (
                    count >= Config.MAX_CLIP_STORAGE.get() &&
                            Math.random() > Config.REPLACEMENT_CHANCE.get()
            ) AudioFileManager.removeRandomClip(eDir);
            else if (count >= Config.MAX_CLIP_STORAGE.get()) continue;

            if (Config.DEBUG.get())
                LOGGER.info("Saving audio clip to {}", file.getAbsolutePath());

            AudioFileManager.saveWav(storage, file);
        }

        return removed;
    }

    public static void removeRandomClip(File eDir) {
        File[] files = eDir.listFiles();
        if (files == null) return;

        File file = files[(int) (files.length * Math.random())];
        file.delete();
    }

    public static void saveWav(PCMStorage storage, File file) {
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

    public static boolean hasClips() {
        return !valids.isEmpty();
    }

    public static UUID getRandomPlayer() {
        return valids.get((int) (Math.random() * valids.size()));
    }

    public static File getRandomAudioFile(UUID player) {
        File dir = FMLPaths.GAMEDIR.get().resolve("mimicked").resolve(player.toString()).toFile();
        File[] files = dir.listFiles();
        if (files == null) return null;
        files = Arrays.stream(files).filter(f -> f.getName().endsWith(".wav")).toArray(File[]::new);
        if (files.length == 0) return null;
        return files[(int) (files.length * Math.random())];
    }

    public static short[] readFromAudioFile(File file) throws UnsupportedAudioFileException, IOException {
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(file)) {
            AudioFormat format = ais.getFormat();

            if (format.getSampleSizeInBits() != 16)
                throw new UnsupportedAudioFileException("Unsupported sample size: " + format.getSampleSizeInBits());
            if (format.isBigEndian())
                throw new UnsupportedAudioFileException("Unsupported byte order: " + format.isBigEndian());

            byte[] bytes = ais.readAllBytes();

            ShortBuffer shortBuffer = ByteBuffer
                    .wrap(bytes)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .asShortBuffer();

            short[] samples = new short[shortBuffer.remaining()];
            shortBuffer.get(samples);

            return samples;
        }
    }
}

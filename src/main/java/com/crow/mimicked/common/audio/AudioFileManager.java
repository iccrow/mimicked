package com.crow.mimicked.common.audio;

import com.crow.mimicked.common.PCMStorage;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class AudioFileManager {

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
}

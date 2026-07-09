package com.crow.mimicked.audio;

import com.crow.mimicked.Config;

public class SelectionFilters {

    public static boolean isMostlySilent(PCMStorage storage) {
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
}

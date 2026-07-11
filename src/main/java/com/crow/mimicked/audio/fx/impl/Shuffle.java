package com.crow.mimicked.audio.fx.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Shuffle {

    public static float[] byChunk(float[] samples, double fxRatio, double concentration) {
        float[] shuffled = new float[samples.length];
        System.arraycopy(samples, 0, shuffled, 0, samples.length);

        int shuffledSamples = (int) (samples.length * fxRatio);
        int segments = (int) Math.max(Math.round(shuffledSamples * concentration / 24_000), 1);
        int segmentSize = shuffledSamples / segments;
        int windowSize = samples.length / segments;

        List<Integer> src = new ArrayList<>();
        List<Integer> dest = new ArrayList<>();
        for (int i = 0; i < segments; i++) {
            src.add(i);
            dest.add(i);
        }
        Collections.shuffle(src);
        Collections.shuffle(dest);

        int[] segmentIndices = new int[segments];
        for (int i = 0; i < segments; i++) {
            segmentIndices[i] = (int) (Math.random() * (windowSize - segmentSize)) + i * windowSize;
        }

        for (int i = 0; i < segments; i++) {
            int srcIndex = segmentIndices[src.get(i)];
            int destIndex = segmentIndices[dest.get(i)];
            System.arraycopy(samples, srcIndex, shuffled, destIndex, segmentSize);
        }

        return shuffled;
    }

    public static float[] bySampleWithinChunk(float[] samples, double fxRatio, double concentration) {
        float[] shuffled = new float[samples.length];
        System.arraycopy(samples, 0, shuffled, 0, samples.length);

        int shuffledSamples = (int) (samples.length * fxRatio);
        int segments = (int) Math.max(Math.round(shuffledSamples * concentration) / 24_000, 2);
        int segmentSize = shuffledSamples / segments;
        int windowSize = samples.length / segments;

        int[] segmentIndices = new int[segments];
        for (int i = 0; i < segments; i++) {
            segmentIndices[i] = (int) (Math.random() * (windowSize - segmentSize)) + i * windowSize;
        }

        for (int segmentIndex : segmentIndices) {
            List<Float> wrapped = new ArrayList<>(segmentSize);
            for (int i = 0; i < segmentSize; i++) {
                wrapped.add(shuffled[segmentIndex + i]);
            }
            Collections.shuffle(wrapped);
            for (int i = 0; i < segmentSize; i++) {
                shuffled[segmentIndex + i] = wrapped.get(i);
            }
        }

        return shuffled;
    }

    public static float[] bySample(float[] samples, double fxRatio) {
        float[] shuffled = new float[samples.length];
        System.arraycopy(samples, 0, shuffled, 0, samples.length);

        List<Integer> src = new ArrayList<>();
        List<Integer> dest = new ArrayList<>();
        for (int i = 0; i < samples.length; i++) {
            src.add(i);
            dest.add(i);
        }
        Collections.shuffle(src);
        Collections.shuffle(dest);

        int shuffledSamples = (int) (samples.length * fxRatio);
        for (int i = 0; i < shuffledSamples; i++) {
            shuffled[dest.get(i)] = samples[src.get(i)];
        }

        return shuffled;
    }
}

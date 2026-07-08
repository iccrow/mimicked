package com.crow.mimicked.common.sfx;

import java.util.Arrays;

public class Reversal {

    public static float[] full(float[] samples) {
        float[] reversed = new float[samples.length];

        for (int i = 0; i < samples.length; i++) {
            reversed[i] = samples[samples.length - i - 1];
        }

        return reversed;
    }

    public static float[] fractured(float[] samples, double fxRatio, double concentration) {
        float[] reversed = Arrays.copyOf(samples, samples.length);

        int reversedSamples = (int) (samples.length * fxRatio);
        int segments = (int) Math.max(reversedSamples * (1-concentration) / 24_000, 1);
        int segmentSize = reversedSamples / segments;
        int windowSize = samples.length / segments;

        int[] segmentIndices = new int[segments];

        for (int i = 0; i < segments; i++) {
            segmentIndices[i] = (int) (Math.random() * (windowSize - segmentSize)) + i * windowSize;
        }

        for (int segmentIndex : segmentIndices) {
            float[] segment = Arrays.copyOfRange(samples, segmentIndex, segmentIndex + segmentSize);

            for (int i = 0; i < segmentSize; i++) {
                reversed[i + segmentIndex] = segment[segmentSize - i - 1];
            }
        }

        return reversed;
    }
}

package com.crow.mimicked.audio.fx.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Stutter {

    public static float[] stutter(float[] samples, double stutterDuration, double stutterCooldown, int repeats) {
        Float[] wrapped = new Float[samples.length];
        for (int i = 0; i < samples.length; i++) {
            wrapped[i] = samples[i];
        }
        ArrayList<Float> stuttered = new ArrayList<>(
                List.of(wrapped)
        );

        int segments = (int) (samples.length / 48000.0 / (stutterCooldown + stutterDuration * repeats)) + 1;
        int stutterSamples = (int) (stutterDuration * segments * 48000);
        int segmentSize = stutterSamples / segments;
        int windowSize = samples.length / segments;

        int[] segmentIndices = new int[segments];
        for (int i = 0; i < segments; i++) {
            segmentIndices[i] = (int) (Math.random() * (windowSize - segmentSize)) + i * windowSize;
        }

        for (int i = 0; i < segmentIndices.length; i++) {
            for (int j = 0; j < repeats; j++) {
                Float[] segment = Arrays.copyOfRange(wrapped, segmentIndices[i], segmentIndices[i] + segmentSize);
                stuttered.addAll(i * segmentSize * repeats + j * segmentSize + segmentIndices[i], List.of(segment));
            }
        }

        float[] stutteredSamples = new float[stuttered.size()];
        for (int i = 0; i < stuttered.size(); i++) {
            stutteredSamples[i] = stuttered.get(i);
        }

        return stutteredSamples;
    }
}

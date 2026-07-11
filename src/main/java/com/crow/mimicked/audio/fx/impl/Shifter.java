package com.crow.mimicked.audio.fx.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Shifter {

    public static float[] tempo(float[] samples, double shift) {
        int newLength = (int) (samples.length / Math.max(1 + shift, 1e-5));
        float[] shifted = new float[Math.max(samples.length, newLength) + 1024];
        System.arraycopy(samples, 0, shifted, 0, samples.length);
        Sonic.changeFloatSpeed(shifted, samples.length, (float) (1 + shift), 1, 1, 1, false, 48000, 1);

        float[] reduced = new float[newLength - 1024];
        System.arraycopy(shifted, 0, reduced, 0, newLength - 1024);
        shifted = reduced;
        return shifted;
    }

    public static float[] speed(float[] samples, double shift) {
        int newLength = (int) (samples.length / Math.max(1 + shift, 1e-5));
        float[] shifted = new float[Math.max(samples.length, newLength) + 1024];
        System.arraycopy(samples, 0, shifted, 0, samples.length);
        Sonic.changeFloatSpeed(shifted, samples.length, 1, 1, (float) (1 + shift), 1, false, 48000, 1);

        float[] reduced = new float[newLength - 1024];
        System.arraycopy(shifted, 0, reduced, 0, newLength - 1024);
        shifted = reduced;
        return shifted;
    }

    public static float[] pitch(float[] samples, double shift) {
        float[] shifted = new float[samples.length + 1024];
        System.arraycopy(samples, 0, shifted, 0, samples.length);
        Sonic.changeFloatSpeed(shifted, samples.length, 1, (float) (1 + shift), 1, 1, false, 48000, 1);
        return shifted;
    }

    public static float[] fracturedTempo(float[] samples, double fxRatio, double concentration, double variance) {
        Float[] wrapped = new Float[samples.length];
        for (int i = 0; i < samples.length; i++) {
            wrapped[i] = samples[i];
        }
        List<Float> fractured = new ArrayList<>(List.of(wrapped));

        int shiftedSamples = (int) (samples.length * fxRatio);
        int segments = (int) Math.max(Math.round(shiftedSamples * concentration / 24_000), 1);
        int segmentSize = shiftedSamples / segments;
        int windowSize = samples.length / segments;

        int[] segmentIndices = new int[segments];

        for (int i = 0; i < segments; i++) {
            segmentIndices[i] = (int) (Math.random() * (windowSize - segmentSize)) + i * windowSize;
        }

        int added = 0;

        for (int segmentIndex : segmentIndices) {
            float[] segment = Arrays.copyOfRange(samples, segmentIndex, segmentIndex + segmentSize);
            double shift = (Math.random() - 0.5) * 2 * variance;
            if (shift < 0)
                shift = -1 / (-1 + shift) - 1;
            float[] shifted = tempo(segment, shift);
            wrapped = new Float[shifted.length];
            for (int i = 0; i < shifted.length; i++) {
                wrapped[i] = shifted[i];
            }
            fractured.subList(segmentIndex + added, segmentIndex + added + segmentSize).clear();
            fractured.addAll(segmentIndex + added, List.of(wrapped));
            added += wrapped.length - segmentSize;
        }

        float[] out = new float[fractured.size()];
        for (int i = 0; i < fractured.size(); i++) {
            out[i] = fractured.get(i);
        }

        return out;
    }

    public static float[] fracturedPitch(float[] samples, double fxRatio, double concentration, double variance) {
        float[] fractured = Arrays.copyOf(samples, samples.length);

        int shiftedSamples = (int) (samples.length * fxRatio);
        int segments = (int) Math.max(Math.round(shiftedSamples * concentration / 24_000), 1);
        int segmentSize = shiftedSamples / segments;
        int windowSize = samples.length / segments;

        int[] segmentIndices = new int[segments];

        for (int i = 0; i < segments; i++) {
            segmentIndices[i] = (int) (Math.random() * (windowSize - segmentSize)) + i * windowSize;
        }

        for (int segmentIndex : segmentIndices) {
            float[] segment = Arrays.copyOfRange(samples, segmentIndex, segmentIndex + segmentSize);
            double shift = (Math.random() - 0.5) * 2 * variance;
            if (shift < 0)
                shift = -1 / (-1 + shift) - 1;
            float[] shifted = pitch(segment, shift);
            System.arraycopy(shifted, 0, fractured, segmentIndex, segmentSize);
        }

        return fractured;
    }

//    public static float[] phaser(float[] samples, double shift) {
//        return samples;
//    }
}

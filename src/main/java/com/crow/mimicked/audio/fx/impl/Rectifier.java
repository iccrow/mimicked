package com.crow.mimicked.audio.fx.impl;

public class Rectifier {

    public static float[] full(float[] samples) {
        float[] rectified = new float[samples.length];
        for (int i = 0; i < samples.length; i++) {
            rectified[i] = Math.abs(samples[i]);
        }
        return rectified;
    }

    public static float[] half(float[] samples) {
        float[] rectified = new float[samples.length];
        for (int i = 0; i < samples.length; i++) {
            rectified[i] = Math.max(samples[i], 0);
        }
        return rectified;
    }
}

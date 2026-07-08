package com.crow.mimicked.common.sfx;

import java.util.Arrays;

public class Shifter {

    private static final int BLOCK_SIZE = 256;

    public static float[] tempo(float[] samples, double shift) {
        shift = 1 / Math.max(1 + shift, 1e-5);
        float[] mainBuffer = new float[(int) Math.ceil(samples.length / (float) BLOCK_SIZE) * BLOCK_SIZE + 3 * BLOCK_SIZE];
        System.arraycopy(samples, 0, mainBuffer, 256, samples.length);

        float[] hann = new float[BLOCK_SIZE];
        for (int i = 0; i < hann.length; i++) {
            hann[i] = (float) Math.pow(Math.sin((Math.PI * i) / BLOCK_SIZE), 2);
        }

        int outputSize = (int) (samples.length * shift) + BLOCK_SIZE;
        float[] output = new float[outputSize];
        int outputWinPos = 0;

        int s = 0;
        int delta = 0;
        int tolerance = BLOCK_SIZE / 2;
        int windowIdx = 1;
        while (s < samples.length) {
            for (int i = 0; i < BLOCK_SIZE; i++) {
                output[outputWinPos + i] += hann[i] * mainBuffer[s + delta + i];
            }

            float[] frameAdj = new float[BLOCK_SIZE];
            float[] frameNext = new float[BLOCK_SIZE * 2];
            for (int i = 0; i < BLOCK_SIZE; i++) {
                frameAdj[i] = mainBuffer[s + delta + i + BLOCK_SIZE / 2];
            }

            s += (int) (BLOCK_SIZE / shift / 2);
            for (int i = 0; i < BLOCK_SIZE * 2; i++) {
                if (s - tolerance + i < 0)
                    frameNext[i] = 0;
                else
                    frameNext[i] = mainBuffer[s - tolerance + i];
            }

            float[] CCResult = new float[BLOCK_SIZE * 3 - 1];
            float[] nextRev = new float[BLOCK_SIZE * 2];
            for (int i = 0; i < BLOCK_SIZE * 2; i++) {
                nextRev[i] = frameNext[BLOCK_SIZE * 2 - i - 1];
            }

            for (int i = 0; i < BLOCK_SIZE * 3 - 1; i++) {
                int adjStart = Math.max(0, i - BLOCK_SIZE * 2 + 1);
                int adjEnd = Math.min(i + 1, BLOCK_SIZE);
                int nextStart = Math.min(i, BLOCK_SIZE * 2 - 1);
                for (int j = adjStart; j < adjEnd; j++) {
                    CCResult[i] += frameAdj[j] * nextRev[nextStart--];
                }
            }

            float max = 0;
            int maxIndex = 0;
            for (int i = 0; i < BLOCK_SIZE * 2; i++) {
                if (CCResult[i] > max) {
                    max = CCResult[i];
                    maxIndex = i - BLOCK_SIZE;
                }
            }

            delta = tolerance - maxIndex;
            outputWinPos = (windowIdx++) * BLOCK_SIZE / 2;
        }

        return output;
    }

    private static float hermite(float x0, float x1, float x2, float x3, float mu) {
        float mu2 = mu * mu;
        float a0 = 3 * x1 - 3 * x2 + x3 - x0;
        float a1 = 2 * x0 - 5 * x1 + 4 * x2 - x3;
        float a2 = x2 - x0;
        float a3 = 2 * x1;

        return (a0 * mu * mu2 + a1 * mu2 + a2 * mu + a3) / 2;
    }

    public static float[] resample(float[] samples, int sampleCount) {
        float t = 0;
        float tMod = 0;
        int tFloor;
        float[] resampled = new float[sampleCount];

        for (int i = 0; i < sampleCount - BLOCK_SIZE; i++) {
            tFloor = (int) t;
            if (t < 1)
                resampled[i] = hermite(0, samples[0], samples[1], samples[2], tMod);
            else {
                tMod = t % 1;
                resampled[i] = hermite(samples[tFloor - 1], samples[tFloor], samples[tFloor + 1], samples[tFloor + 2], tMod);
            }
            t += (float) samples.length / sampleCount;
        }

        return resampled;
    }

    public static float[] speed(float[] samples, double shift) {
        return resample(samples, (int) (samples.length * Math.max(1 + shift, 0)));
    }

    public static float[] pitch(float[] samples, double shift) {
        return resample(tempo(samples, -shift), samples.length + 2 * BLOCK_SIZE);
    }

//    public static float[] phaser(float[] samples, double shift) {
//        return samples;
//    }
}

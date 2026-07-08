package com.crow.mimicked.common.audio;

public class AudioConverter {

    public static float[] toFloat(short[] samples) {
        float[] floats = new float[samples.length];
        for (int i = 0; i < samples.length; i++) {
            floats[i] = samples[i] / 32767.0f;
        }
        return floats;
    }

    public static short[] toShort(float[] samples) {
        short[] shorts = new short[samples.length];
        for (int i = 0; i < samples.length; i++) {
            shorts[i] = (short) (samples[i] * 32767);
        }
        return shorts;
    }

    public static byte[] rawBytes(short[] samples) {
        byte[] bytes = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            bytes[i * 2] = (byte) (samples[i] & 0xFF);
            bytes[i * 2 + 1] = (byte) ((samples[i] >> 8) & 0xFF);
        }
        return bytes;
    }
}

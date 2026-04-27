package com.crow.mimicked;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class PCMStorage {
    private final ByteBuffer byteBuffer;
    private final ShortBuffer pcmBuffer;

    private long lastUpdateTime;
    private int samples = 0;

    public PCMStorage(int capacitySamples) {
        // Allocate enough space for all PCM samples (short = 2 bytes)
        byteBuffer = ByteBuffer
                .allocate(capacitySamples * 2)
                .order(ByteOrder.LITTLE_ENDIAN);
        pcmBuffer = byteBuffer.asShortBuffer();

        lastUpdateTime = System.currentTimeMillis();
    }

    public synchronized void appendPacket(short[] packet) {
        if (pcmBuffer.remaining() < packet.length)
            return;

        pcmBuffer.put(packet);
        lastUpdateTime = System.currentTimeMillis();
        samples += packet.length;
    }

    public synchronized short[] getAllSamples() {
        int size = pcmBuffer.position(); // number of samples written
        short[] data = new short[size];

        pcmBuffer.rewind(); // reset read position to 0
        pcmBuffer.get(data); // read all written samples

        return data;
    }

    public synchronized byte[] getByteData() {
        int size = pcmBuffer.position() * 2; // number of samples written
        byte[] data = new byte[size];

        byteBuffer.rewind(); // reset read position to 0
        byteBuffer.get(data); // read all written samples

        return data;
    }

    public synchronized long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public double getDuration() {
        return samples / 48000.0;
    }

    public synchronized void clear() {
        pcmBuffer.clear(); // resets buffer for reuse
        byteBuffer.clear();
    }
}
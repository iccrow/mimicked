package com.crow.mimicked.audio.fx;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public interface SFX {

    public static final List<Pair<Supplier<Double>, Supplier<SFX>>> SFX_LIST = new ArrayList<>();

    public static SFX random() {
        double totalWeight = 0;
        for (Pair<Supplier<Double>, Supplier<SFX>> pair : SFX_LIST) {
            totalWeight += pair.getLeft().get();
        }
        double random = Math.random() * totalWeight;
        for (Pair<Supplier<Double>, Supplier<SFX>> pair : SFX_LIST) {
            random -= pair.getLeft().get();
            if (random <= 0) {
                return pair.getRight().get();
            }
        }

        if (!SFX_LIST.isEmpty())
            return SFX_LIST.get(0).getRight().get();
        else
            return null;
    }

    String getId();
    float[] apply(float[] samples);
}

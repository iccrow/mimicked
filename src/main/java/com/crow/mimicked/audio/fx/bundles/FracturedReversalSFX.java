package com.crow.mimicked.audio.fx.bundles;

import com.crow.mimicked.audio.fx.SFX;
import com.crow.mimicked.audio.fx.SFXConfig;
import com.crow.mimicked.audio.fx.impl.Reversal;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class FracturedReversalSFX implements SFX {

    private final double fxRatio;
    private final double concentration;

    public FracturedReversalSFX() {
        this.fxRatio = Math.random() * (
                SFXConfig.FRACTURED_REVERSAL_SFX.RATIO_MAX.get() - SFXConfig.FRACTURED_REVERSAL_SFX.RATIO_MIN.get()
        ) + SFXConfig.FRACTURED_REVERSAL_SFX.RATIO_MIN.get();
        this.concentration = Math.random() * (
                SFXConfig.FRACTURED_REVERSAL_SFX.CONCENTRATION_MAX.get() - SFXConfig.FRACTURED_REVERSAL_SFX.CONCENTRATION_MIN.get()
        ) + SFXConfig.FRACTURED_REVERSAL_SFX.CONCENTRATION_MIN.get();
    }

    @Override
    public String getId() {
        return "fractured_reversal";
    }

    @Override
    public float[] apply(float[] samples) {
        return Reversal.fractured(samples, this.fxRatio, this.concentration);
    }

    public static class Config {

        public final ModConfigSpec.DoubleValue WEIGHT;

        public final ModConfigSpec.DoubleValue RATIO_MIN;
        public final ModConfigSpec.DoubleValue RATIO_MAX;

        public final ModConfigSpec.DoubleValue CONCENTRATION_MIN;
        public final ModConfigSpec.DoubleValue CONCENTRATION_MAX;

        public Config(ModConfigSpec.Builder builder) {
            builder.comment("Fractured Reversal takes chunks of the input audio and reverses them.")
                    .push("fractured_reversal");

            this.WEIGHT = builder
                    .comment("How heavily the fractured audio reversal sfx should be weighted compared to the normal chance.")
                    .defineInRange("weight", 1.0, 0.0, Double.MAX_VALUE);

            builder.comment("The ratio of reversed audio to unreversed audio.")
                    .push("ratio");
            this.RATIO_MIN = builder
                    .defineInRange("min", 0.3, 0.0, 1.0);

            this.RATIO_MAX = builder
                    .defineInRange("max", 0.4, 0.0, 1.0);
            builder.pop();

            builder.comment("The conversion rate of number of chunks processed per half-second of reversed audio.")
                    .push("concentration");
            this.CONCENTRATION_MIN = builder
                    .defineInRange("min", 0.5, 0.0, 24_000.0);

            this.CONCENTRATION_MAX = builder
                    .defineInRange("max", 0.7, 0.0, 24_000.0);
            builder.pop()
                    .pop();

            SFX.SFX_LIST.add(
                    Pair.of(
                            WEIGHT,
                            FracturedReversalSFX::new
                    )
            );
        }
    }
}

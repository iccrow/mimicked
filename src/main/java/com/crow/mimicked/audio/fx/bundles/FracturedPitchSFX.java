package com.crow.mimicked.audio.fx.bundles;

import com.crow.mimicked.audio.fx.SFX;
import com.crow.mimicked.audio.fx.SFXConfig;
import com.crow.mimicked.audio.fx.impl.Shifter;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class FracturedPitchSFX implements SFX {

    private final double fxRatio;
    private final double concentration;
    private final double variance;

    public FracturedPitchSFX() {
        this.fxRatio = Math.random() * (
                SFXConfig.FRACTURED_PITCH_SFX.RATIO_MAX.get() - SFXConfig.FRACTURED_PITCH_SFX.RATIO_MIN.get()
        ) + SFXConfig.FRACTURED_PITCH_SFX.RATIO_MIN.get();
        this.concentration = Math.random() * (
                SFXConfig.FRACTURED_PITCH_SFX.CONCENTRATION_MAX.get() - SFXConfig.FRACTURED_PITCH_SFX.CONCENTRATION_MIN.get()
        ) + SFXConfig.FRACTURED_PITCH_SFX.CONCENTRATION_MIN.get();
        this.variance = Math.random() * (
                SFXConfig.FRACTURED_PITCH_SFX.VARIANCE_MAX.get() - SFXConfig.FRACTURED_PITCH_SFX.VARIANCE_MIN.get()
        ) + SFXConfig.FRACTURED_PITCH_SFX.VARIANCE_MIN.get();
    }

    @Override
    public String getId() {
        return "fractured_pitch";
    }

    @Override
    public float[] apply(float[] samples) {
        return Shifter.fracturedPitch(samples, this.fxRatio, this.concentration, this.variance);
    }

    public static class Config {

        public final ForgeConfigSpec.DoubleValue WEIGHT;

        public final ForgeConfigSpec.DoubleValue RATIO_MIN;
        public final ForgeConfigSpec.DoubleValue RATIO_MAX;

        public final ForgeConfigSpec.DoubleValue CONCENTRATION_MIN;
        public final ForgeConfigSpec.DoubleValue CONCENTRATION_MAX;

        public final ForgeConfigSpec.DoubleValue VARIANCE_MIN;
        public final ForgeConfigSpec.DoubleValue VARIANCE_MAX;

        public Config(ForgeConfigSpec.Builder builder) {
            builder.comment("Fractured Pitch takes chunks of the input audio and shifts their pitch.")
                    .push("fractured_pitch");

            this.WEIGHT = builder
                    .comment("How heavily the fractured audio pitch sfx should be weighted compared to the normal chance.")
                    .defineInRange("weight", 1.0, 0.0, Double.MAX_VALUE);

            builder.comment("The ratio of pitch-shifted audio to pitch-normal audio.")
                    .push("ratio");
            this.RATIO_MIN = builder
                    .defineInRange("min", 0.3, 0.0, 1.0);

            this.RATIO_MAX = builder
                    .defineInRange("max", 0.4, 0.0, 1.0);
            builder.pop();

            builder.comment("The conversion rate of number of chunks processed per half-second of pitch-shifted audio.")
                    .push("concentration");
            this.CONCENTRATION_MIN = builder
                    .defineInRange("min", 0.5, 0.0, 24_000.0);

            this.CONCENTRATION_MAX = builder
                    .defineInRange("max", 0.7, 0.0, 24_000.0);
            builder.pop();

            builder.comment("The maximum variance of pitch the fractured audio sfx.")
                    .push("variance");
            this.VARIANCE_MIN = builder
                    .defineInRange("min", 0.3, 0.0, Double.MAX_VALUE);
            this.VARIANCE_MAX = builder
                    .defineInRange("max", 0.4, 0.0, Double.MAX_VALUE);
            builder.pop()
                    .pop();

            SFX.SFX_LIST.add(
                    Pair.of(
                            WEIGHT,
                            FracturedPitchSFX::new
                    )
            );
        }
    }
}

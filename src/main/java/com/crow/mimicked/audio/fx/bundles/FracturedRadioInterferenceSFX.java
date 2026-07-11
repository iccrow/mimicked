package com.crow.mimicked.audio.fx.bundles;

import com.crow.mimicked.audio.fx.SFX;
import com.crow.mimicked.audio.fx.SFXConfig;
import com.crow.mimicked.audio.fx.impl.Shuffle;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class FracturedRadioInterferenceSFX implements SFX {

    private final double fxRatio;
    private final double concentration;

    public FracturedRadioInterferenceSFX() {
        this.fxRatio = Math.random() * (
                SFXConfig.FRACTURED_RADIO_INTERFERENCE_SFX.RATIO_MAX.get() - SFXConfig.FRACTURED_RADIO_INTERFERENCE_SFX.RATIO_MIN.get()
        ) + SFXConfig.FRACTURED_RADIO_INTERFERENCE_SFX.RATIO_MIN.get();
        this.concentration = Math.random() * (
                SFXConfig.FRACTURED_RADIO_INTERFERENCE_SFX.CONCENTRATION_MAX.get() - SFXConfig.FRACTURED_RADIO_INTERFERENCE_SFX.CONCENTRATION_MIN.get()
        ) + SFXConfig.FRACTURED_RADIO_INTERFERENCE_SFX.CONCENTRATION_MIN.get();
    }

    @Override
    public String getId() {
        return "fractured_radio_interference";
    }

    @Override
    public float[] apply(float[] samples) {
        return Shuffle.bySampleWithinChunk(samples, this.fxRatio, this.concentration);
    }

    public static class Config {

        public final ForgeConfigSpec.DoubleValue WEIGHT;

        public final ForgeConfigSpec.DoubleValue RATIO_MIN;
        public final ForgeConfigSpec.DoubleValue RATIO_MAX;

        public final ForgeConfigSpec.DoubleValue CONCENTRATION_MIN;
        public final ForgeConfigSpec.DoubleValue CONCENTRATION_MAX;

        public Config(ForgeConfigSpec.Builder builder) {
            builder.comment("Fractured Radio Interference takes chunks of the input audio and converts them to static. This effectively sounds like radio interference for portions of the audio.")
                    .push("fractured_radio_interference");

            this.WEIGHT = builder
                    .comment("How heavily the fractured radio interference sfx should be weighted compared to the normal chance.")
                    .defineInRange("weight", 1.0, 0.0, Double.MAX_VALUE);

            builder.comment("The ratio of static audio to non-static audio.")
                    .push("ratio");
            this.RATIO_MIN = builder
                    .defineInRange("min", 0.3, 0.0, 1.0);

            this.RATIO_MAX = builder
                    .defineInRange("max", 0.4, 0.0, 1.0);
            builder.pop();

            builder.comment("The conversion rate of number of chunks processed per half-second of shuffled audio.")
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
                            FracturedRadioInterferenceSFX::new
                    )
            );
        }
    }
}

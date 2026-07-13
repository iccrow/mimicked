package com.crow.mimicked.audio.fx.bundles;

import com.crow.mimicked.audio.fx.SFX;
import com.crow.mimicked.audio.fx.SFXConfig;
import com.crow.mimicked.audio.fx.impl.Shuffle;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class PartialRadioInterferenceSFX implements SFX {

    private final double fxRatio;

    public PartialRadioInterferenceSFX() {
        this.fxRatio = Math.random() * (
                SFXConfig.PARTIAL_RADIO_INTERFERENCE_SFX.RATIO_MAX.get() - SFXConfig.PARTIAL_RADIO_INTERFERENCE_SFX.RATIO_MIN.get()
        ) + SFXConfig.PARTIAL_RADIO_INTERFERENCE_SFX.RATIO_MIN.get();
    }

    @Override
    public String getId() {
        return "partial_radio_interference";
    }

    @Override
    public float[] apply(float[] samples) {
        return Shuffle.bySample(samples, this.fxRatio);
    }

    public static class Config {

        public final ModConfigSpec.DoubleValue WEIGHT;

        public final ModConfigSpec.DoubleValue RATIO_MIN;
        public final ModConfigSpec.DoubleValue RATIO_MAX;


        public Config(ModConfigSpec.Builder builder) {
            builder.comment("Partial Radio Interference shuffles around a portion of the audio samples. This effectively sounds like static is overlaid on top of the input audio.")
                    .push("partial_radio_interference");

            this.WEIGHT = builder
                    .comment("How heavily the partial radio interference sfx should be weighted compared to the normal chance.")
                    .defineInRange("weight", 1.0, 0.0, Double.MAX_VALUE);

            builder.comment("The ratio of static audio to non-static audio.")
                    .push("ratio");
            this.RATIO_MIN = builder
                    .defineInRange("min", 0.2, 0.0, 1.0);

            this.RATIO_MAX = builder
                    .defineInRange("max", 0.3, 0.0, 1.0);
            builder.pop()
                    .pop();

            SFX.SFX_LIST.add(
                    Pair.of(
                            WEIGHT,
                            PartialRadioInterferenceSFX::new
                    )
            );
        }
    }
}

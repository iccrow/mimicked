package com.crow.mimicked.audio.fx.bundles;

import com.crow.mimicked.audio.fx.SFX;
import com.crow.mimicked.audio.fx.impl.Rectifier;
import com.crow.mimicked.audio.fx.impl.Shifter;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class PitchedFullWaveRectifierSFX implements SFX {

    @Override
    public String getId() {
        return "pitched_full_wave_rectifier";
    }

    @Override
    public float[] apply(float[] samples) {
        return Shifter.pitch(
                Rectifier.full(samples),
                -0.5
        );
    }

    public static class Config {

        public final ModConfigSpec.DoubleValue WEIGHT;

        public Config(ModConfigSpec.Builder builder) {
            builder.comment("Pitched Full Wave Rectifier takes the entire input audio and distorts it, creating overlaying deep and normal pitch distorted audio.")
                    .push("pitched_full_wave_rectifier");

            this.WEIGHT = builder.comment("How heavily the pitched full wave rectifier sfx should be weighted compared to the normal chance.")
                    .defineInRange("weight", 1.0, 0.0, Double.MAX_VALUE);

            builder.pop();

            SFX.SFX_LIST.add(
                    Pair.of(
                            WEIGHT,
                            PitchedFullWaveRectifierSFX::new
                    )
            );
        }
    }
}

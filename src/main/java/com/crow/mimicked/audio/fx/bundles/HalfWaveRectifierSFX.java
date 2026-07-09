package com.crow.mimicked.audio.fx.bundles;

import com.crow.mimicked.audio.fx.SFX;
import com.crow.mimicked.audio.fx.impl.Rectifier;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class HalfWaveRectifierSFX implements SFX {

    @Override
    public String getId() {
        return "half_wave_rectifier";
    }

    @Override
    public float[] apply(float[] samples) {
        return Rectifier.half(samples);
    }

    public static class Config {

        public final ForgeConfigSpec.DoubleValue WEIGHT;

        public Config(ForgeConfigSpec.Builder builder) {
            builder.comment("Half Wave Rectifier takes the entire input audio and distorts it. This can often make audio sound like a broken speaker.")
                    .push("half_wave_rectifier");

            this.WEIGHT = builder
                    .comment("How heavily the half wave rectifier sfx should be weighted compared to the normal chance.")
                    .defineInRange("weight", 1.0, 0.0, Double.MAX_VALUE);

            builder.pop();

            SFX.SFX_LIST.add(
                    Pair.of(
                            WEIGHT,
                            HalfWaveRectifierSFX::new
                    )
            );
        }
    }
}

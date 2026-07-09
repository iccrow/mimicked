package com.crow.mimicked.audio.fx.bundles;

import com.crow.mimicked.audio.fx.SFX;
import com.crow.mimicked.audio.fx.SFXConfig;
import com.crow.mimicked.audio.fx.impl.Reversal;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class FullReversalSFX extends SFXConfig implements SFX {

    @Override
    public String getId() {
        return "full_reversal";
    }

    @Override
    public float[] apply(float[] samples) {
        return Reversal.full(samples);
    }

    public static class Config {

        public final ForgeConfigSpec.DoubleValue WEIGHT;

        public Config(ForgeConfigSpec.Builder builder) {
            builder.comment("Full Reversal takes the entire input audio and reverses it.")
                    .push("full_reversal");

            this.WEIGHT = builder
                    .comment("How heavily the full audio reversal sfx should be weighted compared to the normal chance.")
                    .defineInRange("fullReversalWeight", 1.0, 0.0, Double.MAX_VALUE);

            builder.pop();

            SFX.SFX_LIST.add(
                    Pair.of(
                            WEIGHT,
                            FullReversalSFX::new
                    )
            );
        }
    }
}

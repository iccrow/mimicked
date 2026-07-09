package com.crow.mimicked.audio.fx.bundles;

import com.crow.mimicked.audio.fx.SFX;
import com.crow.mimicked.audio.fx.SFXConfig;
import com.crow.mimicked.audio.fx.impl.Shifter;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class PitchShiftSFX implements SFX {

    public final double shift;

    public PitchShiftSFX() {
        this.shift = Math.random() * (
                SFXConfig.PITCH_SHIFT_SFX.SHIFT_MAX.get() - SFXConfig.PITCH_SHIFT_SFX.SHIFT_MIN.get()
        ) + SFXConfig.PITCH_SHIFT_SFX.SHIFT_MIN.get();
    }

    @Override
    public String getId() {
        return "pitch_shift";
    }

    @Override
    public float[] apply(float[] samples) {
        return Shifter.pitch(samples, shift);
    }

    public static class Config {

        public final ForgeConfigSpec.DoubleValue WEIGHT;

        public final ForgeConfigSpec.DoubleValue SHIFT_MIN;
        public final ForgeConfigSpec.DoubleValue SHIFT_MAX;

        public Config(ForgeConfigSpec.Builder builder) {
            builder.comment("Pitch Shift takes the entire input audio and shifts the pitch.")
                    .push("pitch_shift");

            this.WEIGHT = builder
                    .comment("How heavily the pitch shift sfx should be weighted compared to the normal chance.")
                    .defineInRange("weight", 1.0, 0.0, Double.MAX_VALUE);

            builder.comment("The pitch shift amount.")
                    .push("shift");
            this.SHIFT_MIN = builder
                    .defineInRange("min", -0.3, -1, 3.0);
            this.SHIFT_MAX = builder
                    .defineInRange("max", 0.3, -1, 3.0);
            builder.pop();

            builder.pop();

            SFX.SFX_LIST.add(
                    Pair.of(
                            WEIGHT,
                            PitchShiftSFX::new
                    )
            );
        }
    }
}

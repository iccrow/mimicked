package com.crow.mimicked.audio.fx.bundles;

import com.crow.mimicked.audio.fx.SFX;
import com.crow.mimicked.audio.fx.SFXConfig;
import com.crow.mimicked.audio.fx.impl.Shifter;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class TempoShiftSFX implements SFX {

    public final double shift;

    public TempoShiftSFX() {
        this.shift = Math.random() * (
                SFXConfig.TEMPO_SHIFT_SFX.SHIFT_MAX.get() - SFXConfig.TEMPO_SHIFT_SFX.SHIFT_MIN.get()
        ) + SFXConfig.TEMPO_SHIFT_SFX.SHIFT_MIN.get();
    }

    @Override
    public String getId() {
        return "tempo_shift";
    }

    @Override
    public float[] apply(float[] samples) {
        return Shifter.tempo(samples, this.shift);
    }

    public static class Config {

        public final ModConfigSpec.DoubleValue WEIGHT;

        public final ModConfigSpec.DoubleValue SHIFT_MIN;
        public final ModConfigSpec.DoubleValue SHIFT_MAX;

        public Config(ModConfigSpec.Builder builder) {
            builder.comment("Tempo Shift takes the entire input audio and changes the speed without changing pitch.")
                    .push("tempo_shift");

            this.WEIGHT = builder
                    .comment("How heavily the tempo shift sfx should be weighted compared to the normal chance.")
                    .defineInRange("weight", 1.0, 0.0, Double.MAX_VALUE);

            builder.comment("The tempo shift amount.")
                    .push("shift");
            this.SHIFT_MIN = builder
                    .defineInRange("min", -0.3, -1.0, Double.MAX_VALUE);
            this.SHIFT_MAX = builder
                    .defineInRange("max", 0.3, -1.0, Double.MAX_VALUE);
            builder.pop();

            builder.pop();

            SFX.SFX_LIST.add(
                    Pair.of(
                            WEIGHT,
                            TempoShiftSFX::new
                    )
            );
        }
    }
}

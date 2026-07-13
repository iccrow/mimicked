package com.crow.mimicked.audio.fx.bundles;

import com.crow.mimicked.audio.fx.SFX;
import com.crow.mimicked.audio.fx.SFXConfig;
import com.crow.mimicked.audio.fx.impl.Stutter;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class StutterSFX implements SFX {

    public final double stutterDuration;
    public final double stutterCooldown ;
    public final int repeats;

    public StutterSFX() {
        this.stutterDuration = Math.random() * (
                SFXConfig.STUTTER_SFX.STUTTER_DURATION_MAX.get() - SFXConfig.STUTTER_SFX.STUTTER_DURATION_MIN.get()
        ) + SFXConfig.STUTTER_SFX.STUTTER_DURATION_MIN.get();
        this.stutterCooldown = Math.random() * (
                SFXConfig.STUTTER_SFX.STUTTER_COOLDOWN_MAX.get() - SFXConfig.STUTTER_SFX.STUTTER_COOLDOWN_MIN.get()
        ) + SFXConfig.STUTTER_SFX.STUTTER_COOLDOWN_MIN.get();
        this.repeats = (int) (Math.random() * (
                SFXConfig.STUTTER_SFX.REPEATS_MAX.get() - SFXConfig.STUTTER_SFX.REPEATS_MIN.get()
        ) + SFXConfig.STUTTER_SFX.REPEATS_MIN.get());
    }

    @Override
    public String getId() {
        return "stutter";
    }

    @Override
    public float[] apply(float[] samples) {
        return Stutter.stutter(samples, stutterDuration, stutterCooldown, repeats);
    }

    public static class Config {

        public final ModConfigSpec.DoubleValue WEIGHT;

        public final ModConfigSpec.DoubleValue STUTTER_DURATION_MIN;
        public final ModConfigSpec.DoubleValue STUTTER_DURATION_MAX;

        public final ModConfigSpec.DoubleValue STUTTER_COOLDOWN_MIN;
        public final ModConfigSpec.DoubleValue STUTTER_COOLDOWN_MAX;

        public final ModConfigSpec.IntValue REPEATS_MIN;
        public final ModConfigSpec.IntValue REPEATS_MAX;

        public Config(ModConfigSpec.Builder builder) {
            builder.comment("Stutter takes chunks of the input audio and stutters it.")
                    .push("stutter");

            this.WEIGHT = builder
                    .comment("How heavily the stutter sfx should be weighted compared to the normal chance.")
                    .defineInRange("weight", 1.0, 0.0, Double.MAX_VALUE);

            builder.comment("The duration of the stutter effect.")
                    .push("duration");
            this.STUTTER_DURATION_MIN = builder
                    .defineInRange("min", 0.05, 0.0, Double.MAX_VALUE);
            this.STUTTER_DURATION_MAX = builder
                    .defineInRange("max", 0.1, 0.0, Double.MAX_VALUE);
            builder.pop();

            builder.comment("The cooldown between stutter effects.")
                    .push("cooldown");
            this.STUTTER_COOLDOWN_MIN = builder
                    .defineInRange("min", 1, 0.0, Double.MAX_VALUE);
            this.STUTTER_COOLDOWN_MAX = builder
                    .defineInRange("max", 3, 0.0, Double.MAX_VALUE);
            builder.pop();

            builder.comment("The number of times each chunk should be repeated.")
                    .push("repeats");
            this.REPEATS_MIN = builder
                    .defineInRange("min", 2, 0, Integer.MAX_VALUE);
            this.REPEATS_MAX = builder
                    .defineInRange("max", 5, 0, Integer.MAX_VALUE);
            builder.pop();

            builder.pop();

            SFX.SFX_LIST.add(
                    Pair.of(
                            WEIGHT,
                            StutterSFX::new
                    )
            );
        }
    }
}

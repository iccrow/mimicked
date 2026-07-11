package com.crow.mimicked.audio.fx.bundles;

import com.crow.mimicked.audio.fx.SFX;
import com.crow.mimicked.audio.fx.SFXConfig;
import com.crow.mimicked.audio.fx.impl.Reversal;
import com.crow.mimicked.audio.fx.impl.Shuffle;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ChunkShuffleSFX implements SFX {

    private final double fxRatio;
    private final double concentration;

    public ChunkShuffleSFX() {
        this.fxRatio = Math.random() * (
                SFXConfig.CHUNK_SHUFFLE_SFX.RATIO_MAX.get() - SFXConfig.CHUNK_SHUFFLE_SFX.RATIO_MIN.get()
        ) + SFXConfig.CHUNK_SHUFFLE_SFX.RATIO_MIN.get();
        this.concentration = Math.random() * (
                SFXConfig.CHUNK_SHUFFLE_SFX.CONCENTRATION_MAX.get() - SFXConfig.CHUNK_SHUFFLE_SFX.CONCENTRATION_MIN.get()
        ) + SFXConfig.CHUNK_SHUFFLE_SFX.CONCENTRATION_MIN.get();
    }

    @Override
    public String getId() {
        return "chunk_shuffle";
    }

    @Override
    public float[] apply(float[] samples) {
        return Shuffle.byChunk(samples, this.fxRatio, this.concentration);
    }

    public static class Config {

        public final ForgeConfigSpec.DoubleValue WEIGHT;

        public final ForgeConfigSpec.DoubleValue RATIO_MIN;
        public final ForgeConfigSpec.DoubleValue RATIO_MAX;

        public final ForgeConfigSpec.DoubleValue CONCENTRATION_MIN;
        public final ForgeConfigSpec.DoubleValue CONCENTRATION_MAX;

        public Config(ForgeConfigSpec.Builder builder) {
            builder.comment("Chunk Shuffle takes chunks of the input audio and shuffles them around.")
                    .push("chunk_shuffle");

            this.WEIGHT = builder
                    .comment("How heavily the chunk shuffle sfx should be weighted compared to the normal chance.")
                    .defineInRange("weight", 1.0, 0.0, Double.MAX_VALUE);

            builder.comment("The ratio of shuffled audio to unshuffled audio.")
                    .push("ratio");
            this.RATIO_MIN = builder
                    .defineInRange("min", 0.6, 0.0, 1.0);

            this.RATIO_MAX = builder
                    .defineInRange("max", 0.9, 0.0, 1.0);
            builder.pop();

            builder.comment("The conversion rate of number of chunks processed per half-second of shuffled audio.")
                    .push("concentration");
            this.CONCENTRATION_MIN = builder
                    .defineInRange("min", 0.7, 0.0, 24_000.0);

            this.CONCENTRATION_MAX = builder
                    .defineInRange("max", 0.9, 0.0, 24_000.0);
            builder.pop()
                    .pop();

            SFX.SFX_LIST.add(
                    Pair.of(
                            WEIGHT,
                            ChunkShuffleSFX::new
                    )
            );
        }
    }
}

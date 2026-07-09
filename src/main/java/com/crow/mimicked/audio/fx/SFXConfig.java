package com.crow.mimicked.audio.fx;

import com.crow.mimicked.audio.fx.bundles.*;
import net.minecraftforge.common.ForgeConfigSpec;

public class SFXConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder().comment("Settings for Mimicked mod playback sound effects.");

    public static final ForgeConfigSpec.DoubleValue SFX_CHANCE = BUILDER.push("general")
            .comment("The chance of sound effects to be applied to mimic clips.")
            .defineInRange("sfxChance", 0.3, 0.0, 1.0);

    public static final ForgeConfigSpec.DoubleValue SFX_OVERLAP_CHANCE = BUILDER
            .comment("The chance of a second sound effect to be applied to mimic clips.")
            .defineInRange("sfxOverlapChance", 0.3, 0.0, 1.0);

    public static final FullReversalSFX.Config FULL_REVERSAL_SFX = new FullReversalSFX.Config(BUILDER.pop());
    public static final FracturedReversalSFX.Config FRACTURED_REVERSAL_SFX = new FracturedReversalSFX.Config(BUILDER);
    public static final HalfWaveRectifierSFX.Config HALF_WAVE_RECTIFIER_SFX = new HalfWaveRectifierSFX.Config(BUILDER);
    public static final PitchedFullWaveRectifierSFX.Config PITCHED_FULL_WAVE_RECTIFIER_SFX = new PitchedFullWaveRectifierSFX.Config(BUILDER);
    public static final StutterSFX.Config STUTTER_SFX = new StutterSFX.Config(BUILDER);
    public static final PitchShiftSFX.Config PITCH_SHIFT_SFX = new PitchShiftSFX.Config(BUILDER);
    public static final TempoShiftSFX.Config TEMPO_SHIFT_SFX = new TempoShiftSFX.Config(BUILDER);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

}

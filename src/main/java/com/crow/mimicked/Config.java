package com.crow.mimicked;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = Mimicked.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.LongValue TIMEOUT = BUILDER
            .comment("How long of no audio input before assuming the speaker is done talking.")
            .defineInRange("timeout", 400L, 1L, Long.MAX_VALUE);

    public static final ForgeConfigSpec.IntValue AMPLITUDE_THRESHOLD = BUILDER
            .comment("How loud is non-silence.")
            .defineInRange("amplitudeThreshold", 800, 0, Short.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue LOUD_THRESHOLD = BUILDER
            .comment("Portion of samples above threshold to consider the clip as non-silent.")
            .defineInRange("loudThreshold", 0.05, 0.0, 1.0);

    public static final ForgeConfigSpec.DoubleValue RMS_THRESHOLD = BUILDER
            .comment("RMS threshold to consider the clip as non-silent.")
            .defineInRange("rmsThreshold", 500, 0.0, Double.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue RANDOMNESS = BUILDER
            .comment("The portion of the time to NOT record players' speaking.")
            .defineInRange("randomness", 0.8, 0.0, 1.0);

    public static final ForgeConfigSpec.IntValue ODDS = BUILDER
            .comment("Odds for randomly mimicking. One in N.")
            .defineInRange("odds", 20 * 60 * 5, 1, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.BooleanValue DISABLE_SELF = BUILDER
            .comment("Disables hearing yourself.")
            .define("disableSelf", false);

    public static final ForgeConfigSpec.IntValue MAX_CLIP_LENGTH = BUILDER
            .comment("Maximum size of a mimic clip.")
            .defineInRange("maxClipLength", 10, 1, 100);

    public static final ForgeConfigSpec.IntValue MAX_CLIP_STORAGE = BUILDER
            .comment("Maximum number of stored clips for each player.")
            .defineInRange("maxClipStorage", 15, 1, 100);


    static final ForgeConfigSpec SPEC = BUILDER.build();
}

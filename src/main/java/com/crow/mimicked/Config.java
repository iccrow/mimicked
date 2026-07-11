package com.crow.mimicked;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@Mod.EventBusSubscriber(modid = Mimicked.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder().comment("General settings for the Mimicked mod.");

    public static final ForgeConfigSpec.LongValue TIMEOUT = BUILDER.push("thresholds")
            .comment("How many milliseconds of no audio input before assuming the speaker is done talking.")
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

    public static final ForgeConfigSpec.DoubleValue RECORDING_CHANCE = BUILDER.pop().push("recording")
            .comment("The chance to record a player when they are speaking.")
            .defineInRange("recordingChance", 0.2, 0.0, 1.0);

    public static final ForgeConfigSpec.DoubleValue REPLACEMENT_CHANCE = BUILDER
            .comment("The chance of a clip being replaced by a new one when a new recording is made if clip storage is full.")
            .defineInRange("replacementChance", 0.2, 0.0, 1.0);

    public static final ForgeConfigSpec.DoubleValue MAX_CLIP_LENGTH = BUILDER
            .comment("Maximum size of a mimic clip in seconds.")
            .defineInRange("maxClipLength", 10.0, 1.0, 100.0);

    public static final ForgeConfigSpec.IntValue MAX_CLIP_STORAGE = BUILDER
            .comment("Maximum number of stored clips for each player.")
            .defineInRange("maxClipStorage", 15, 1, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue SPARSITY = BUILDER.pop().push("playback")
            .comment("How often should mimic events happen (on average) in minutes.")
            .defineInRange("sparsity", 5, 0, Double.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue DELETION_CHANCE = BUILDER
            .comment("The chance of a clip being deleted after it is used. This is disabled during overdrive.")
            .defineInRange("deletionChance", 0.5, 0.0, 1.0);

    public static final ForgeConfigSpec.BooleanValue DISABLE_SELF = BUILDER
            .comment("Disables hearing yourself.")
            .define("disableSelf", false);

    public static final ForgeConfigSpec.BooleanValue PREFER_SERVER_SIDE = BUILDER
            .comment("Disable client-side audio processing when connected to a server with the mod installed (recommended for quality).")
            .define("preferServerSide", true);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HOST_WHITELIST = BUILDER
            .comment("A whitelist for possible entities to be the source of the mimicking. Note that mimicking can only be done if there is a proper host nearby.")
            .comment("Add the entity's id to the list. An example list: [\"minecraft:zombie\", \"minecraft:piglin\"]")
            .comment("You can also put an entry of \"@modid\" to whitelist all entities from a mod. Example: [\"@mimicked\"]")
            .defineList("hostWhitelist", List.of(), Config::validateEntityName);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> OVERDRIVE_HOSTS = BUILDER
            .comment("Overdrive is an event that occurs when a player is near an overdrive host, making mimicking and distorted mimicking happen much more often. During overdrive, deletion is disabled.")
            .push("overdrive")
            .comment("A list of entities that when near a player, will turn mimicking into overdrive. This list is independent of the host whitelist.")
            .comment("Add the entity's id to the list. An example list: [\"minecraft:zombie\", \"minecraft:piglin\"]")
            .comment("You can also put an entry of \"@modid\" to whitelist all entities from a mod. Example: [\"@mimicked\"]")
            .defineList("overdriveHosts", List.of(), Config::validateEntityName);

    public static final ForgeConfigSpec.DoubleValue OVERDRIVE_SPARSITY = BUILDER
            .comment("How how often should mimic events happen (on average) in minutes when near an overdrive host is near a player. Deletion is disabled during overdrive.")
            .defineInRange("overdrive", 0.2, 0.0, Double.MAX_VALUE);

    public static final ForgeConfigSpec.BooleanValue DEBUG = BUILDER.pop().pop().push("debug")
            .comment("Enables debug logging.")
            .define("debug", false);


    static final ForgeConfigSpec SPEC = BUILDER.pop().build();

    private static boolean validateEntityName(final Object obj) {
        try {
            return obj instanceof final String name && (
                    name.startsWith("@") && ModList.get().isLoaded(name.substring(1)) ||
                            ForgeRegistries.ENTITY_TYPES.containsKey(ResourceLocation.parse(name))
            );
        } catch (Exception e) {
            return false;
        }
    }
}

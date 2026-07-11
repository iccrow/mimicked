package com.crow.mimicked;

import com.crow.mimicked.audio.AudioFileManager;
import com.crow.mimicked.audio.fx.SFXConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Mimicked.MODID)
public class Mimicked
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "mimicked";

    public Mimicked() {

        FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);


        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC, "mimicked-common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SFXConfig.SPEC, "mimicked-sfx.toml");
    }

    public Mimicked(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);


        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC, "mimicked-common.toml");
        context.registerConfig(ModConfig.Type.COMMON, SFXConfig.SPEC, "mimicked-sfx.toml");
    }



    private void commonSetup(final FMLCommonSetupEvent event)
    {
        AudioFileManager.init();
        event.enqueueWork(ModNetwork::register);
    }

}

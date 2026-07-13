package com.crow.mimicked;

import com.crow.mimicked.audio.AudioFileManager;
import com.crow.mimicked.audio.fx.SFXConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Mimicked.MODID)
public class Mimicked
{
    public static final String MODID = "mimicked";

    public Mimicked(IEventBus modEventBus, ModContainer modContainer)
    {

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);


        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC, "mimicked-common.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, SFXConfig.SPEC, "mimicked-sfx.toml");
    }



    private void commonSetup(final FMLCommonSetupEvent event)
    {
        AudioFileManager.init();
    }

}

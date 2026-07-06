package com.crow.mimicked;

import de.maxhenkel.voicechat.api.audiochannel.ClientEntityAudioChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.*;
import java.util.stream.Collectors;

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
        context.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }

    public Mimicked(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);


        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }

    static List<UUID> valids = new ArrayList<>();

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        File file = FMLPaths.GAMEDIR.get().resolve("mimicked").toFile();
        if (!file.exists())
            file.mkdirs();

        File[] files = file.listFiles();
        if (files == null)
            return;

        valids = Arrays.stream(files).filter(File::isDirectory).map(f -> UUID.fromString(f.getName())).collect(Collectors.toList());
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void tick(TickEvent.PlayerTickEvent event) throws IOException, UnsupportedAudioFileException {
            if (event.player != Minecraft.getInstance().player)
                return;

            if (Math.random() > 1.0 / Config.ODDS.get())
                return;

            if (valids.isEmpty())
                return;

            Entity theChosenOne = chooseHostEntity(event.player, Plugin.capi.getVoiceChatDistance()/2.0);
            if (theChosenOne == null)
                return;

            for (int i = 0; i < 5; i++) {
                UUID mimicked = valids.get(
                        (int) (valids.size() * Math.random())
                );

                if (mimicked.equals(event.player.getUUID()) && Config.DISABLE_SELF.get())
                    continue;
                File file = FMLPaths.GAMEDIR.get().resolve("mimicked").resolve(mimicked.toString()).toFile();
                File[] files = file.listFiles();
                if (files == null)
                    return;

                files = Arrays.stream(files).filter(f -> f.getName().endsWith(".wav")).toArray(File[]::new);

                if (files.length == 0)
                    continue;

                file = files[(int) (files.length * Math.random())];

                AudioInputStream ais = AudioSystem.getAudioInputStream(file);
                AudioFormat format = ais.getFormat();

                if (format.getSampleSizeInBits() != 16)
                    return;
                if (format.isBigEndian())
                    return;

                byte[] bytes = ais.readAllBytes();

                ShortBuffer shortBuffer = ByteBuffer
                        .wrap(bytes)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .asShortBuffer();

                short[] samples = new short[shortBuffer.remaining()];
                shortBuffer.get(samples);

                ais.close();

                ClientEntityAudioChannel channel = Plugin.capi.createEntityAudioChannel(theChosenOne.getUUID(), Plugin.capi.fromEntity(theChosenOne));
                if (channel == null)
                    return;
                if (samples.length < 100)
                    return;

                channel.play(samples);
                file.delete();
                return;
            }
        }
    }

    public static Entity chooseHostEntity(Player player, double radius) {
        AABB box = new AABB(
                player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                player.getX() + radius, player.getY() + radius, player.getZ() + radius
        );

        List<Entity> entities = player.level().getEntities(player, box, e -> !(e instanceof Player) && e instanceof LivingEntity);

        if (entities.isEmpty()) return null;

        Random rand = new Random();


        List<Entity> hostiles = entities.stream().filter(e -> (e instanceof Monster monster) && !monster.isNoAi()).toList();

        if (hostiles.isEmpty())
            return entities.get(rand.nextInt(entities.size()));

        return hostiles.get(rand.nextInt(hostiles.size()));
    }
}

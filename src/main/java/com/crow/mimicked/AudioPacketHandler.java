package com.crow.mimicked;

import com.crow.mimicked.audio.PCMStorage;
import com.crow.mimicked.audio.AudioFileManager;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Mod.EventBusSubscriber(modid = Mimicked.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AudioPacketHandler {

    private static final ConcurrentMap<UUID, OpusDecoder> decoders = new ConcurrentHashMap<>();

    public static void receivePacket(MicrophonePacketEvent event) {
        if (event.getSenderConnection() == null || event.getSenderConnection().getPlayer() == null)
            return;

        UUID uuid = event.getSenderConnection().getPlayer().getUuid();
        PCMStorage storage = AudioFileManager.getPCMStorage(uuid);

        OpusDecoder decoder = decoders.computeIfAbsent(uuid, k -> Plugin.api.createDecoder());
        storage.appendPacket(
                decoder.decode(
                        event.getPacket().getOpusEncodedData()
                )
        );
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END)
            return;

        List<UUID> timedOut = AudioFileManager.checkTimeouts();

        for (UUID uuid : timedOut) {
            decoders.remove(uuid);
        }
    }


    public static void receivePacket(ClientReceiveSoundEvent.EntitySound e) {
        if (ModNetwork.isServerSidePresent() && Config.PREFER_SERVER_SIDE.get())
            return;

        UUID uuid = e.getEntityId();
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null &&
                        !mc.getConnection().getOnlinePlayerIds().contains(uuid))
            return;

        PCMStorage storage = AudioFileManager.getPCMStorage(uuid);
        short[] audio = e.getRawAudio();
        storage.appendPacket(audio);
    }

    public static void receivePacket(ClientSoundEvent e) {
        if (ModNetwork.isServerSidePresent() && Config.PREFER_SERVER_SIDE.get())
            return;

        if (Minecraft.getInstance().player == null || Config.DISABLE_SELF.get())
            return;

        UUID uuid = Minecraft.getInstance().player.getUUID();
        PCMStorage storage = AudioFileManager.getPCMStorage(uuid);
        short[] audio = e.getRawAudio();
        storage.appendPacket(audio);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END)
            return;

        AudioFileManager.checkTimeouts();
    }
}

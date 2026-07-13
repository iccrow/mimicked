package com.crow.mimicked;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = Mimicked.MODID)
public class ModNetwork {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1")
                .optional();

        registrar.playToServer(
                Main.TYPE,
                Main.STREAM_CODEC,
                (a, b) -> {}
        );
    }

    public static boolean isServerSidePresent() {
        if (EffectiveSide.get().isServer())
            return true;

        Minecraft mc = Minecraft.getInstance();

        if (mc.getConnection() == null)
            return false;

        return mc.getConnection().hasChannel(Main.CHANNEL_ID);
    }

    public record Main() implements CustomPacketPayload {

        private static final ResourceLocation CHANNEL_ID = ResourceLocation.fromNamespaceAndPath(Mimicked.MODID, "main");
        public static final CustomPacketPayload.Type<Main> TYPE = new CustomPacketPayload.Type<>(CHANNEL_ID);

        public static final StreamCodec<ByteBuf, Main> STREAM_CODEC = StreamCodec.unit(new Main());

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}

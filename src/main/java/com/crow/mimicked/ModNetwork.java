package com.crow.mimicked;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Mimicked.MODID, "main"),
            () -> PROTOCOL_VERSION,
            NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION),
            NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION)
    );

    public static void register() {}

    public static boolean isServerSidePresent() {
        if (EffectiveSide.get().isServer())
            return true;

        Minecraft mc = Minecraft.getInstance();

        if (mc.getConnection() == null)
            return false;

        Connection connection = mc.getConnection().getConnection();
        return CHANNEL.isRemotePresent(connection);
    }
}

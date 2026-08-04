package com.crow.mimicked;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;


public class ModNetwork {

    private static final int PROTOCOL_VERSION = 1;
    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(Mimicked.MODID, "main"))
            .networkProtocolVersion(PROTOCOL_VERSION)
            .clientAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
            .serverAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
            .optional()
            .simpleChannel();

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

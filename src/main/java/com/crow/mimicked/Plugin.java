package com.crow.mimicked;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.*;

@ForgeVoicechatPlugin
public class Plugin implements VoicechatPlugin {

    public static VoicechatApi api;
    public static VoicechatClientApi capi;
    public static VoicechatServerApi sapi;

    @Override
    public void initialize(VoicechatApi api) {
        Plugin.api = api;
    }

    @Override
    public String getPluginId() {
        return Mimicked.MODID;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(ClientReceiveSoundEvent.EntitySound.class, AudioPacketHandler::receivePacket);
        registration.registerEvent(ClientSoundEvent.class, AudioPacketHandler::receivePacket);
        registration.registerEvent(ClientVoicechatInitializationEvent.class, event -> capi = event.getVoicechat());

        registration.registerEvent(VoicechatServerStartedEvent.class, event -> sapi = event.getVoicechat());
        registration.registerEvent(MicrophonePacketEvent.class, AudioPacketHandler::receivePacket);

        registration.registerEvent(EntitySoundPacketEvent.class, AudioPacketHandler::sendPacket);
    }
}

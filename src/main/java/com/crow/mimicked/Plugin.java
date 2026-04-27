package com.crow.mimicked;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientVoicechatInitializationEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;

@ForgeVoicechatPlugin
public class Plugin implements VoicechatPlugin {

    public static VoicechatApi api;
    public static VoicechatClientApi capi;

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
        registration.registerEvent(ClientReceiveSoundEvent.EntitySound.class, PacketHandler::receivePacket);
        registration.registerEvent(ClientSoundEvent.class, PacketHandler::receivePacket);
        registration.registerEvent(ClientVoicechatInitializationEvent.class, event -> capi = event.getVoicechat());
    }
}

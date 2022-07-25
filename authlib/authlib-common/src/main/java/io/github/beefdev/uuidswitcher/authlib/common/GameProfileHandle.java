package io.github.beefdev.uuidswitcher.authlib.common;

import com.mojang.authlib.GameProfile;
import io.github.beefdev.uuidswitcher.common.event.AsyncPlayerProfileCreationEvent;
import io.github.beefdev.uuidswitcher.common.wrapper.WrappedGameProfile;
import org.bukkit.Bukkit;

import java.net.InetAddress;

public final class GameProfileHandle {

    private final GameProfile original;

    public GameProfileHandle(GameProfile original) {
        this.original = original;
    }

    public GameProfile fireEvents() {
        return this.fireEvents(null);
    }

    public GameProfile fireEvents(InetAddress address) {
        AsyncPlayerProfileCreationEvent asyncPlayerProfileCreationEvent = new AsyncPlayerProfileCreationEvent(WrappedGameProfile.fromGameProfile(this.original), address);
        try {
            Bukkit.getPluginManager().callEvent(asyncPlayerProfileCreationEvent);
        } catch (Throwable throwable) {
            return this.original;
        }

        return asyncPlayerProfileCreationEvent.getGameProfile() == null ? this.original : asyncPlayerProfileCreationEvent.getGameProfile().toGameProfile();
    }
}

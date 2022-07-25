package io.github.beefdev.uuidswitcher.authlib.post1_11;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import io.github.beefdev.uuidswitcher.authlib.common.GameProfileHandle;

import java.net.InetAddress;
import java.util.Map;

@SuppressWarnings("unused")
public final class MinecraftSessionServiceDelegator implements MinecraftSessionService {

    private final MinecraftSessionService delegate;

    public MinecraftSessionServiceDelegator(Object delegate) {
        this((MinecraftSessionService) delegate);
    }

    public MinecraftSessionServiceDelegator(MinecraftSessionService delegate) {
        this.delegate = delegate;
    }

    public MinecraftSessionService getDelegate() {
        return this.delegate;
    }

    @Override
    public GameProfile hasJoinedServer(GameProfile gameProfile, String s, InetAddress inetAddress) throws AuthenticationUnavailableException {
        return new GameProfileHandle(this.getDelegate().hasJoinedServer(gameProfile, s, inetAddress)).fireEvents(inetAddress);
    }

    @Override
    public void joinServer(GameProfile gameProfile, String s, String s1) throws AuthenticationException {
        this.getDelegate().joinServer(gameProfile, s, s1);
    }

    @Override
    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures(GameProfile gameProfile, boolean b) {
        return this.getDelegate().getTextures(gameProfile, b);
    }

    @Override
    public GameProfile fillProfileProperties(GameProfile gameProfile, boolean b) {
        return this.getDelegate().fillProfileProperties(gameProfile, b);
    }

    @Override
    public String getSecurePropertyValue(Property property) throws InsecurePublicKeyException {
        return this.getDelegate().getSecurePropertyValue(property);
    }
}

package io.github.beefdev.uuidswitcher.authlib.pre1_11;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import io.github.beefdev.uuidswitcher.authlib.common.GameProfileHandle;

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
    public GameProfile hasJoinedServer(GameProfile gameProfile, String s) throws AuthenticationUnavailableException {
        return new GameProfileHandle(this.getDelegate().hasJoinedServer(gameProfile, s)).fireEvents();
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
}

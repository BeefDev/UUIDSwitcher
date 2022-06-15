package io.github.beefdev.uuidswitcher.versions.v1_17_1_R0_1;

import com.mojang.authlib.GameProfile;
import io.github.beefdev.uuidswitcher.common.utils.PlayerPostLoginHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.LoginListener;

public final class USLoginListener extends LoginListener {
    public USLoginListener(MinecraftServer minecraftserver, NetworkManager networkmanager) {
        super(minecraftserver, networkmanager);
    }

    @Override
    public void c() {
        new PlayerPostLoginHandler(LoginListener.class, GameProfile.class, this).updateFields();
        super.c();
    }
}

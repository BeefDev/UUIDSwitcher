package io.github.beefdev.uuidswitcher.versions.v1_19_R0_1;

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
    public void d() {
        new PlayerPostLoginHandler(LoginListener.class, GameProfile.class, this).updateFields();
        super.d();
    }
}

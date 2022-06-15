package io.github.beefdev.uuidswitcher.versions.v1_8_8_R0_1;

import com.mojang.authlib.GameProfile;
import io.github.beefdev.uuidswitcher.common.utils.PlayerPostLoginHandler;
import net.minecraft.server.v1_8_R3.LoginListener;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.NetworkManager;

public final class USLoginListener extends LoginListener {
    public USLoginListener(MinecraftServer minecraftserver, NetworkManager networkmanager) {
        super(minecraftserver, networkmanager);
    }

    @Override
    public void b() {
        new PlayerPostLoginHandler(LoginListener.class, GameProfile.class, this).updateFields();
        super.b();
    }
}

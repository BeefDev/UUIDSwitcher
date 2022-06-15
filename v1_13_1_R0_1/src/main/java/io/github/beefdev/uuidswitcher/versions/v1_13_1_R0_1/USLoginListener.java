package io.github.beefdev.uuidswitcher.versions.v1_13_1_R0_1;

import com.mojang.authlib.GameProfile;
import io.github.beefdev.uuidswitcher.common.utils.PlayerPostLoginHandler;
import net.minecraft.server.v1_13_R2.LoginListener;
import net.minecraft.server.v1_13_R2.MinecraftServer;
import net.minecraft.server.v1_13_R2.NetworkManager;

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

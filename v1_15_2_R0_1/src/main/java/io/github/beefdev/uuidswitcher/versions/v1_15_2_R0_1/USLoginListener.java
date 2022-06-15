package io.github.beefdev.uuidswitcher.versions.v1_15_2_R0_1;

import com.mojang.authlib.GameProfile;
import io.github.beefdev.uuidswitcher.common.utils.PlayerPostLoginHandler;
import net.minecraft.server.v1_15_R1.LoginListener;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.NetworkManager;

public final class USLoginListener extends LoginListener {
    public USLoginListener(MinecraftServer minecraftserver, NetworkManager networkmanager) {
        super(minecraftserver, networkmanager);
    }

    @Override
    public void c() {
        new PlayerPostLoginHandler(LoginListener.class, GameProfile.class, this).updateFields();;
        super.c();
    }
}

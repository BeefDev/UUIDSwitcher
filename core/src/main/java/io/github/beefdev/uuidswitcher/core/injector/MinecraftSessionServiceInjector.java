package io.github.beefdev.uuidswitcher.core.injector;

import io.github.beefdev.uuidswitcher.authlib.core.MinecraftSessionServiceHandle;
import io.github.beefdev.uuidswitcher.common.util.ReflectionHelper;
import io.github.beefdev.uuidswitcher.common.util.version.MinecraftVersion;

public final class MinecraftSessionServiceInjector extends FieldInjector {

    private static final Class<?> MINECRAFT_SERVER_CLASS;
    private static final Class<?> MINECRAFT_SESSION_SERVICE_CLASS;

    static {
        MINECRAFT_SERVER_CLASS = ReflectionHelper.fetchClass(
                MinecraftVersion.getCurrentNMSTopLevelPackage().concat(".MinecraftServer"),
                "net.minecraft.server.MinecraftServer"
        );

        MINECRAFT_SESSION_SERVICE_CLASS = ReflectionHelper.fetchClass("com.mojang.authlib.minecraft.MinecraftSessionService");
    }

    public MinecraftSessionServiceInjector() {
        super(MINECRAFT_SERVER_CLASS, MINECRAFT_SESSION_SERVICE_CLASS);
    }

    @Override
    protected Object patchObject(Object original) {
        return new MinecraftSessionServiceHandle(original).createMinecraftSessionServiceWrapper();
    }
}

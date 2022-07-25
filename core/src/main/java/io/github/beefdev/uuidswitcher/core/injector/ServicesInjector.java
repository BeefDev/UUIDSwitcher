package io.github.beefdev.uuidswitcher.core.injector;

import io.github.beefdev.uuidswitcher.authlib.core.MinecraftSessionServiceHandle;
import io.github.beefdev.uuidswitcher.common.util.ReflectionHelper;
import io.github.beefdev.uuidswitcher.common.util.version.MinecraftVersion;

public final class ServicesInjector extends FieldInjector {

    private static final Class<?> MINECRAFT_SERVER_CLASS;
    private static final Class<?> SERVICES_CLASS;

    static {
        MINECRAFT_SERVER_CLASS = ReflectionHelper.fetchClass(
                MinecraftVersion.getCurrentNMSTopLevelPackage().concat(".MinecraftServer"),
                "net.minecraft.server.MinecraftServer"
        );

        SERVICES_CLASS = ReflectionHelper.fetchClass("net.minecraft.server.Services");
    }

    public ServicesInjector() {
        super(MINECRAFT_SERVER_CLASS, SERVICES_CLASS);
    }

    @Override
    protected Object patchObject(Object original) {
        Class<?> minecraftSessionServiceClass = ReflectionHelper.fetchClass("com.mojang.authlib.minecraft.MinecraftSessionService");
        Class<?> signatureValidatorClass = ReflectionHelper.fetchClass("net.minecraft.util.SignatureValidator");
        Class<?> gameProfileRepositoryClass = ReflectionHelper.fetchClass("com.mojang.authlib.GameProfileRepository");
        Class<?> userCacheClass = ReflectionHelper.fetchClass("net.minecraft.server.players.UserCache");

        return ReflectionHelper.invokeConstructor(SERVICES_CLASS, new Class[]{
                minecraftSessionServiceClass,
                signatureValidatorClass,
                gameProfileRepositoryClass,
                userCacheClass
        }, new Object[]{
                new MinecraftSessionServiceHandle(ReflectionHelper.fetchProperty(SERVICES_CLASS, minecraftSessionServiceClass, original)).createMinecraftSessionServiceWrapper(),
                ReflectionHelper.fetchProperty(SERVICES_CLASS, signatureValidatorClass, original),
                ReflectionHelper.fetchProperty(SERVICES_CLASS, gameProfileRepositoryClass, original),
                ReflectionHelper.fetchProperty(SERVICES_CLASS, userCacheClass, original)
        });
    }
}

package io.github.beefdev.uuidswitcher.authlib.core;

import io.github.beefdev.uuidswitcher.common.util.ReflectionHelper;
import io.github.beefdev.uuidswitcher.common.util.version.MinecraftVersion;

public final class MinecraftSessionServiceHandle {

    private final Object minecraftSessionService;

    public MinecraftSessionServiceHandle(Object minecraftSessionService) {
        this.minecraftSessionService = minecraftSessionService;
    }

    public Object createMinecraftSessionServiceWrapper() {
        Class<?> delegatorClass;
        if(MinecraftVersion.RUNNING.olderThan(MinecraftVersion.v1_11)) {
            delegatorClass = ReflectionHelper.fetchClass("io.github.beefdev.uuidswitcher.authlib.pre1_11.MinecraftSessionServiceDelegator");
        } else {
            delegatorClass = ReflectionHelper.fetchClass("io.github.beefdev.uuidswitcher.authlib.post1_11.MinecraftSessionServiceDelegator");
        }

        return ReflectionHelper.invokeConstructor(delegatorClass, new Class[]{Object.class}, new Object[]{minecraftSessionService});
    }
}

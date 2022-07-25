package io.github.beefdev.uuidswitcher.core;

import io.github.beefdev.uuidswitcher.common.util.ReflectionHelper;
import io.github.beefdev.uuidswitcher.common.util.version.MinecraftVersion;
import io.github.beefdev.uuidswitcher.core.injector.MinecraftSessionServiceInjector;
import io.github.beefdev.uuidswitcher.core.injector.ServicesInjector;
import io.github.beefdev.uuidswitcher.core.injector.base.Injector;

final class UUIDSwitcherHandle {

    private Injector injector;

    public void onEnable() {
        if(this.injector != null) {
            throw new IllegalStateException("Injector is already initialized, did you call onEnable twice?");
        }

        this.injector = MinecraftVersion.RUNNING.newerThan(MinecraftVersion.v1_19) ? new ServicesInjector() : new MinecraftSessionServiceInjector();
        this.injector.inject(this.getMinecraftServer());
    }

    public void onDisable() {
        if(injector == null) {
            throw new IllegalStateException("onDisable called, but injector is null, did you forget to call UUIDSwitcher#onEnable");
        }

        this.injector.uninject(this.getMinecraftServer());
    }

    private Object getMinecraftServer() {
        return ReflectionHelper.invokeMethod(ReflectionHelper.fetchClass(
                MinecraftVersion.getCurrentNMSTopLevelPackage().concat(".MinecraftServer"),
                "net.minecraft.server.MinecraftServer"
        ), null, "getServer", new Class[0], new Object[0]);
    }
}

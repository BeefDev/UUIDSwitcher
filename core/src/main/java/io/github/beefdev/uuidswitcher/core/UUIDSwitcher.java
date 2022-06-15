package io.github.beefdev.uuidswitcher.core;

import io.github.beefdev.uuidswitcher.core.internal.NMSHelper;
import io.github.beefdev.uuidswitcher.core.internal.inject.server.USServerConnectionInjector;
import org.apache.commons.lang.reflect.MethodUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class UUIDSwitcher {

    private UUIDSwitcher() throws InstantiationException {
        throw new InstantiationException("Cannot instantiate utility class " + this.getClass().getName());
    }

    public static void onEnable() {
        Class<?> minecraftServerClass = NMSHelper.fetchClass(
                "MinecraftServer",
                "net.minecraft.server.MinecraftServer"
        );

        Class<?> serverConnectionClass = NMSHelper.fetchClass(
                "ServerConnection",
                "net.minecraft.server.network.ServerConnection"
        );

        Object minecraftServer;
        Object serverConnection = null;

        try {
            minecraftServer = MethodUtils.invokeStaticMethod(minecraftServerClass, "getServer", new Object[0]);

            for(Method method : minecraftServerClass.getDeclaredMethods()) {
                if(serverConnectionClass.isAssignableFrom(method.getReturnType()) && method.getParameterCount() == 0) {
                    serverConnection = method.invoke(minecraftServer);
                    break;
                }
            }

            if(serverConnection == null) {
                throw new RuntimeException("ServerConnection is null, unless you have modified the MinecraftServer class this is a bug, please report it to me");
            }
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException exception) {
            throw new RuntimeException("Unexpected exception, unless you have modified the MinecraftServer class this is a bug, please report it to me", exception);
        }

        try {
            new USServerConnectionInjector(minecraftServer, serverConnection).inject();
        } catch (IllegalAccessException exception) {
            throw new RuntimeException("Unexpected exception encountered while injecting, this is a bug, please report it to me", exception);
        }
    }
}

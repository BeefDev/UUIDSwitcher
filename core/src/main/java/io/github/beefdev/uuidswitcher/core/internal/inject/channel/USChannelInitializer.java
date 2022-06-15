package io.github.beefdev.uuidswitcher.core.internal.inject.channel;

import io.github.beefdev.uuidswitcher.core.internal.HandshakeListenerProvider;
import io.github.beefdev.uuidswitcher.core.internal.MinecraftVersionParser;
import io.github.beefdev.uuidswitcher.core.internal.NMSHelper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class USChannelInitializer extends ChannelInitializer<Channel> {

    private final Object minecraftServer;
    private final ChannelInitializer<Channel> originalChannelInitializer;

    public USChannelInitializer(Object minecraftServer, ChannelInitializer<Channel> originalChannelInitializer) {
        this.minecraftServer = minecraftServer;
        this.originalChannelInitializer = originalChannelInitializer;
    }

    private void executeOriginalChannelInitializer(Channel channel) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
        method.setAccessible(true);
        method.invoke(this.originalChannelInitializer, channel);
        method.setAccessible(false);
    }

    private void executeCurrentChannelInitializer(Channel channel) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        Class<?> minecraftServerClass = NMSHelper.fetchClass(
                "MinecraftServer",
                "net.minecraft.server.MinecraftServer"
        );

        Class<?> networkManagerClass = NMSHelper.fetchClass(
                "NetworkManager",
                "net.minecraft.network.NetworkManager"
        );

        Class<?> packetListenerClass = NMSHelper.fetchClass(
                "PacketListener",
                "net.minecraft.network.PacketListener"
        );

        Class<?> usHandshakeListenerClass = new HandshakeListenerProvider("io.github.beefdev.uuidswitcher.versions", new MinecraftVersionParser().parseVersion()).retrieveCustomHandshakeListenerClass();

        Object networkManager = channel.pipeline().get("packet_handler");
        Object usHandshakeListener = usHandshakeListenerClass.getDeclaredConstructor(minecraftServerClass, networkManagerClass).newInstance(this.minecraftServer, networkManager);

        Method setPacketListenerMethod = null;
        for(Method method : networkManagerClass.getDeclaredMethods()) {
            if(method.getReturnType() == void.class && method.getParameterCount() == 1 && method.getParameterTypes()[0] == packetListenerClass) {
                setPacketListenerMethod = method;
                break;
            }
        }

        if(setPacketListenerMethod == null) {
            throw new RuntimeException("Could not find the set packet listener method of NetworkManager, this is a bug, please report it to me");
        }

        setPacketListenerMethod.invoke(networkManager, usHandshakeListener);
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        this.executeOriginalChannelInitializer(channel);

        try {
            this.executeCurrentChannelInitializer(channel);
        } catch (Throwable throwable) {
            throw new RuntimeException("Injection failed, please repor this to me", throwable);
        }
    }
}

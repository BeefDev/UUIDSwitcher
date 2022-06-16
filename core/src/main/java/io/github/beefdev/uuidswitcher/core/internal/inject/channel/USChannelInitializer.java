package io.github.beefdev.uuidswitcher.core.internal.inject.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class USChannelInitializer extends ChannelInitializer<Channel> {

    private final Object minecraftServer;
    private final ChannelInitializer<Channel> originalChannelInitializer;
    private final Class<?> minecraftServerClass;
    private final Class<?> networkManagerClass;
    private final Class<?> packetListenerClass;
    private final Class<?> usHandshakeListenerClass;
    private final Method setPacketListenerMethod;

    public USChannelInitializer(Class<?> minecraftServerClass, Class<?> networkManagerClass, Class<?> packetListenerClass, Class<?> usHandshakeListenerClass, Method setPacketListenerMethod,Object minecraftServer, ChannelInitializer<Channel> originalChannelInitializer) {
        this.minecraftServerClass = minecraftServerClass;
        this.networkManagerClass = networkManagerClass;
        this.packetListenerClass = packetListenerClass;
        this.usHandshakeListenerClass = usHandshakeListenerClass;
        this.setPacketListenerMethod = setPacketListenerMethod;
        this.minecraftServer = minecraftServer;
        this.originalChannelInitializer = originalChannelInitializer;
    }

    public ChannelInitializer<Channel> getOriginalChannelInitializer() {
        return this.originalChannelInitializer;
    }

    private void executeOriginalChannelInitializer(Channel channel) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
        method.setAccessible(true);
        method.invoke(this.originalChannelInitializer, channel);
        method.setAccessible(false);
    }

    private void executeCurrentChannelInitializer(Channel channel) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        Object networkManager = channel.pipeline().get("packet_handler");
        Object usHandshakeListener = usHandshakeListenerClass.getDeclaredConstructor(minecraftServerClass, networkManagerClass).newInstance(this.minecraftServer, networkManager);

        setPacketListenerMethod.invoke(networkManager, usHandshakeListener);
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        this.executeOriginalChannelInitializer(channel);

        try {
            this.executeCurrentChannelInitializer(channel);
        } catch (Throwable throwable) {
            throw new RuntimeException("Injection failed, please report this to me", throwable);
        }
    }
}

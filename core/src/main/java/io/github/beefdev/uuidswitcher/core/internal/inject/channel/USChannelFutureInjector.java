package io.github.beefdev.uuidswitcher.core.internal.inject.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;

import java.lang.reflect.Field;

public final class USChannelFutureInjector {

    private final Object minecraftServer;
    private final ChannelFuture injectionTarget;

    public USChannelFutureInjector(Object minecraftServer, ChannelFuture injectionTarget) {
        this.minecraftServer = minecraftServer;
        this.injectionTarget = injectionTarget;
    }

    public void inject() {
        ChannelHandler bootstrapAcceptor = this.retrieveBootstrapAcceptor();
        ChannelInitializer<Channel> oldChannelInitializer = this.retrieveChildHandler(bootstrapAcceptor);
        ChannelInitializer<Channel> newChannelInitializer = new USChannelInitializer(this.minecraftServer, oldChannelInitializer);

        this.injectChildHandler(bootstrapAcceptor, newChannelInitializer);
    }

    private ChannelHandler retrieveBootstrapAcceptor() {
        ChannelHandler bootstrapAcceptor = null;

        for(String channelHandlerName : this.injectionTarget.channel().pipeline().names()) {
            ChannelHandler channelHandler = this.injectionTarget.channel().pipeline().get(channelHandlerName);

            try {
                Field field = channelHandler.getClass().getDeclaredField("childHandler");
                field.setAccessible(true);
                field.get(channelHandler);
                field.setAccessible(false);
            } catch (Exception exception) {
                continue;
            }

            bootstrapAcceptor = channelHandler;
            break;
        }

        if(bootstrapAcceptor == null) {
            bootstrapAcceptor = this.injectionTarget.channel().pipeline().first();
        }

        return bootstrapAcceptor;
    }

    @SuppressWarnings("unchecked")
    private ChannelInitializer<Channel> retrieveChildHandler(ChannelHandler bootstrapAcceptor) {
        try {
            Field field = bootstrapAcceptor.getClass().getDeclaredField("childHandler");
            field.setAccessible(true);

            ChannelInitializer<Channel> result = (ChannelInitializer<Channel>) field.get(bootstrapAcceptor);
            field.setAccessible(false);

            return result;
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(String.format("Unexpected exception encountered while getting childHandler of %s, this is a bug, please report it to me", bootstrapAcceptor.getClass().getName()), exception);
        } catch (NoSuchFieldException ignored) {
            throw new RuntimeException("Passed in bootstrap acceptor doesn't have a child handler field, unless manually invoked this is a bug");
        }
    }

    private void injectChildHandler(ChannelHandler bootstrapAcceptor, ChannelInitializer<Channel> channelInitializer) {
        try {
            Field field = bootstrapAcceptor.getClass().getDeclaredField("childHandler");
            field.setAccessible(true);
            field.set(bootstrapAcceptor, channelInitializer);
            field.setAccessible(false);
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(String.format("Unexpected exception encountered while injecting childHandler to %s, this is a bug, please report it to me", bootstrapAcceptor.getClass().getName()), exception);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Passed in bootstrap acceptor doesn't have a child handler field, unless manually invoked this is a bug");
        }
    }
}

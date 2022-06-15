package io.github.beefdev.uuidswitcher.core.internal.inject.server;

import com.google.common.base.Preconditions;
import io.github.beefdev.uuidswitcher.core.internal.inject.channel.USChannelFutureInjector;
import io.github.beefdev.uuidswitcher.core.internal.utils.SynchronizedListWrapper;
import io.netty.channel.ChannelFuture;

import java.lang.reflect.Field;
import java.util.List;

public final class USServerConnectionInjector {

    private final Object minecraftServer;
    private final Object serverConnection;

    public USServerConnectionInjector(Object minecraftServer, Object serverConnection) {
        this.minecraftServer = minecraftServer;
        this.serverConnection = serverConnection;
    }

    @SuppressWarnings("unchecked")
    public void inject() throws IllegalAccessException {
        Field channelFutureListField = this.retrieveActiveConnectionListField();

        channelFutureListField.setAccessible(true);
        List<ChannelFuture> activeConnectionList = (List<ChannelFuture>) channelFutureListField.get(this.serverConnection);
        List<ChannelFuture> wrappedActiveConnectionList = new SynchronizedListWrapper<>(activeConnectionList, this::injectChannelFuture);

        synchronized (activeConnectionList) {
            for(ChannelFuture future : activeConnectionList) {
                this.injectChannelFuture(future);
            }

            channelFutureListField.set(this.serverConnection, wrappedActiveConnectionList);
        }

        channelFutureListField.setAccessible(false);
    }

    private Field retrieveActiveConnectionListField() {
        Preconditions.checkNotNull(this.serverConnection, "Attempted to inject null ServerConnection, if you did not manually invoke the injector and this exception is a result of the normal UUIDSwitcher injector, this a bug, please report it to me");

        Field channelFutureListField = null;
        for(Field field : this.serverConnection.getClass().getDeclaredFields()) {
            if(List.class.isAssignableFrom(field.getType()) && field.getGenericType().getTypeName().contains(ChannelFuture.class.getName())) {
                channelFutureListField = field;
                break;
            }
        }

        Preconditions.checkNotNull(channelFutureListField, "The List<ChannelFuture> active connection list of ServerConnection was not found, unless a plugin has assigned a custom ServerConnection that doesn't include these fields, this is a bug, please report it to me");
        return channelFutureListField;
    }

    private void injectChannelFuture(ChannelFuture future) {
        new USChannelFutureInjector(this.minecraftServer, future).inject();
    }
}

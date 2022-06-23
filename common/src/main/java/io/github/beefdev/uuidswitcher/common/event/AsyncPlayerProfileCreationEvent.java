package io.github.beefdev.uuidswitcher.common.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.net.InetAddress;
import java.util.UUID;

public class AsyncPlayerProfileCreationEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final InetAddress address;
    private String name;
    private UUID uuid;

    public AsyncPlayerProfileCreationEvent(InetAddress address, String name, UUID uuid) {
        super(true);
        this.address = address;
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() {
        return this.name;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

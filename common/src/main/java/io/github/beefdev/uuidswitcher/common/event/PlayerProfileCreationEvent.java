package io.github.beefdev.uuidswitcher.common.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class PlayerProfileCreationEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private String name;
    private UUID uuid;

    public PlayerProfileCreationEvent(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() {
        return this.name;
    }

    public UUID getUUID() {
        return this.uuid;
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

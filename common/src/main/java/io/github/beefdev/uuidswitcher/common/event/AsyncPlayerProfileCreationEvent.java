package io.github.beefdev.uuidswitcher.common.event;

import com.mojang.authlib.GameProfile;
import io.github.beefdev.uuidswitcher.common.wrapper.WrappedGameProfile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.net.InetAddress;
import java.util.Optional;

/**
 * An event called when a players GameProfile is "created" inside the {@link com.mojang.authlib.minecraft.MinecraftSessionService#hasJoinedServer(GameProfile, String)  MinecraftSessionService#hasJoinedServer} method
 *
 * @see WrappedGameProfile
 * @see io.github.beefdev.uuidswitcher.common.wrapper.WrappedSignedPropertyMap WrappedSignedPropertyMap
 * @see io.github.beefdev.uuidswitcher.common.wrapper.WrappedSignedProperty WrappedSignedProperty
 */
@SuppressWarnings("unused")
public class AsyncPlayerProfileCreationEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private WrappedGameProfile gameProfile;
    private final InetAddress address;

    /**
     * Constructs a new AsyncPlayerProfileCreationEvent using the specified {@link WrappedGameProfile} and no address
     * @param gameProfile The current GameProfile of the player when this event is called
     * @see #AsyncPlayerProfileCreationEvent(WrappedGameProfile, InetAddress)
     * @see #getGameProfile()
     */
    public AsyncPlayerProfileCreationEvent(WrappedGameProfile gameProfile) {
        this(gameProfile, null);
    }

    /**
     * Constructs a new AsyncPlayerProfileCreationEvent using the specified {@link WrappedGameProfile} and the specified {@link InetAddress} for the IP address
     * @param gameProfile The current GameProfile of the player when this event is called
     * @param address The IP address a player is authenticating with, only available after 1.11
     * @see #getGameProfile()
     * @see #getAddress()
     */
    public AsyncPlayerProfileCreationEvent(WrappedGameProfile gameProfile, InetAddress address) {
        super(true);
        this.gameProfile = gameProfile;
        this.address = address;
    }

    /**
     * Returns a {@link WrappedGameProfile} representing the GameProfile which will be applied to the player after login
     * @return The profile
     */
    public WrappedGameProfile getGameProfile() {
        return this.gameProfile;
    }

    /**
     * Returns the {@link InetAddress} which the player is connecting under
     * @return The address
     */
    public Optional<InetAddress> getAddress() {
        return Optional.ofNullable(this.address);
    }

    /**
     * Sets the {@link WrappedGameProfile} representing the GameProfile the player will be assigned
     * @param gameProfile The profile
     */
    public void setGameProfile(WrappedGameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

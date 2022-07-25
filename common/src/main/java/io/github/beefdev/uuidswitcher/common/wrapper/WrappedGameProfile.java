package io.github.beefdev.uuidswitcher.common.wrapper;

import com.mojang.authlib.GameProfile;

import java.util.UUID;

/**
 * A class used to represent a mojang {@link GameProfile}
 */
@SuppressWarnings("unused")
public final class WrappedGameProfile {

    private final UUID uuid;
    private final String name;
    private final WrappedSignedPropertyMap propertyMap;

    private WrappedGameProfile(GameProfile handle) {
        this(handle.getId(), handle.getName(), WrappedSignedPropertyMap.fromHandle(handle.getProperties()));
    }

    /**
     * Construct a WrappedGameProfile using the passed in name and UUID, with no properties
     * @param uuid The UUID of this profile
     * @param name The name of this profile
     * @see #WrappedGameProfile(UUID, String, WrappedSignedPropertyMap)
     */
    public WrappedGameProfile(UUID uuid, String name) {
        this(uuid, name, new WrappedSignedPropertyMap());
    }

    /**
     *
     * Construct a WrappedGameProfile using the passed in name, UUID and properties
     * @param uuid The UUID of this profile
     * @param name The name of this profile
     * @param propertyMap The properties of this profile
     * @see WrappedSignedPropertyMap
     */
    public WrappedGameProfile(UUID uuid, String name, WrappedSignedPropertyMap propertyMap) {
        this.uuid = uuid;
        this.name = name;
        this.propertyMap = propertyMap;
    }

    /**
     * Returns the UUID associated with this WrappedGameProfile
     * @return The UUID
     */
    public UUID getUuid() {
        return this.uuid;
    }

    /**
     * Returns the name associated with this WrappedGameProfile
     * @return The name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the properties associated with this WrappedGameProfile
     * @return THe properties
     * @see WrappedSignedPropertyMap
     */
    public WrappedSignedPropertyMap getProperties() {
        return this.propertyMap;
    }

    /**
     * Constructs a mojang {@link GameProfile} from this wrapper
     * @return The GameProfile
     */
    public GameProfile toGameProfile() {
        GameProfile profile = new GameProfile(this.getUuid(), this.getName());
        profile.getProperties().clear();
        profile.getProperties().putAll(this.getProperties().toPropertyMap());

        return profile;
    }

    /**
     * Constructs a wrapper around this {@link GameProfile}
     * @param profile The GameProfile to wrap
     * @return The wrapper
     */
    public static WrappedGameProfile fromGameProfile(GameProfile profile) {
        return new WrappedGameProfile(profile);
    }
}

package io.github.beefdev.uuidswitcher.common.wrapper;

import com.mojang.authlib.properties.Property;

/**
 * A class used to represent a mojang {@link Property}
 * @see WrappedSignedPropertyMap
 */
@SuppressWarnings("unused")
public final class WrappedSignedProperty {

    private final String name;
    private final String value;
    private final String signature;

    private WrappedSignedProperty(Property property) {
        this(property.getName(), property.getValue(), property.getSignature());
    }

    /**
     * Constructs a new WrappedSignedProperty with the specified parameters
     * @param name The name of this property
     * @param value The value of this property
     * @see #WrappedSignedProperty(String, String, String)
     * @see #getName()
     * @see #getValue()
     */
    public WrappedSignedProperty(String name, String value) {
        this(name, value, null);
    }

    /**
     * Constructs a new WrappedSignedProperty with the specified parameters
     * @param name The name of this property
     * @param value The value of this property
     * @param signature The signature of this property
     * @see #getName()
     * @see #getValue()
     * @see #getSignature()
     */
    public WrappedSignedProperty(String name, String value, String signature) {
        this.name = name;
        this.value = value;
        this.signature = signature;
    }

    /**
     * Returns the name of this property ex: new WrappedSignedProperty("textures", "value", "signature").getName() -> "textures"
     * @return The name of this property
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the value of this property ex: new WrappedSignedProperty("textures", "value", "signature").getValue() -> "value"
     * @return The name of this property
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Returns the signature of this property ex: new WrappedSignedProperty("textures", "value", "signature").getSignature() -> "signature"
     * @return The name of this property
     */
    public String getSignature() {
        return this.signature;
    }

    /**
     * Constructs a {@link Property} from this wrapper
     * @return The minecraft Property
     */
    public Property getProperty() {
        return new Property(this.getName(), this.getValue(), this.getSignature());
    }

    /**
     * Constructs a wrapper around a minecraft {@link Property}
     * @param property The property to wrap
     * @return The wrapper
     */
    public static WrappedSignedProperty fromHandle(Property property) {
        return new WrappedSignedProperty(property);
    }
}

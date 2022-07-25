package io.github.beefdev.uuidswitcher.common.wrapper;

import com.google.common.collect.ForwardingMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A class used to represent a mojang {@link PropertyMap}
 * @see WrappedSignedProperty
 * @see WrappedGameProfile#getProperties()
 */
@SuppressWarnings("unused")
public final class WrappedSignedPropertyMap extends ForwardingMultimap<String, WrappedSignedProperty> {

    private final Multimap<String, WrappedSignedProperty> delegate;

    private WrappedSignedPropertyMap(PropertyMap propertyMap) {
        Multimap<String, WrappedSignedProperty> delegate = HashMultimap.create();
        for(String key : propertyMap.keySet()) {
            Collection<Property> properties = propertyMap.get(key);
            Collection<WrappedSignedProperty> mappedProperties = properties.stream().map(WrappedSignedProperty::fromHandle).collect(Collectors.toList());
            delegate.putAll(key, mappedProperties);
        }

        this.delegate = delegate;
    }

    /**
     * Constructs a new empty WrappedSignedPropertyMap
     * @see #WrappedSignedPropertyMap(Multimap)
     */
    public WrappedSignedPropertyMap() {
        this(HashMultimap.create());
    }

    /**
     * Constructs a new WrappedSignedPropertyMap containing the passed in properties
     * @param delegate A Multimap used as a delegate for all this classes methods
     */
    public WrappedSignedPropertyMap(Multimap<String, WrappedSignedProperty> delegate) {
        this.delegate = delegate;
    }

    @Override
    protected Multimap<String, WrappedSignedProperty> delegate() {
        return this.delegate;
    }

    /**
     * Constructs a {@link PropertyMap} from this wrapper
     * @return The PropertyMap
     */
    public PropertyMap toPropertyMap() {
        PropertyMap map = new PropertyMap();
        for(String key : this.delegate.keySet()) {
            Collection<WrappedSignedProperty> wrappedProperties = this.delegate.get(key);
            Collection<Property> properties = wrappedProperties.stream().map(WrappedSignedProperty::getProperty).collect(Collectors.toList());
            map.putAll(key, properties);
        }

        return map;
    }

    /**
     * Constructs a wrapper around the passed in {@link PropertyMap}
     * @param propertyMap The PropertyMap to wrap
     * @return The wrapper
     */
    public static WrappedSignedPropertyMap fromHandle(PropertyMap propertyMap) {
        return new WrappedSignedPropertyMap(propertyMap);
    }
}

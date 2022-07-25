package io.github.beefdev.uuidswitcher.core.injector.base;

import java.util.HashMap;
import java.util.Map;

public abstract class Injector {

    private final Map<Object, Object> originalValues;

    public Injector() {
        this.originalValues = new HashMap<>();
    }

    public final void inject(Object target) {
        this.originalValues.put(target, this.inject0(target));
    }

    public final void uninject(Object target) {
        if(!this.originalValues.containsKey(target)) {
            throw new IllegalArgumentException(String.format("Injector#uninject called on %s, whose previous state is not registered, this means object has not been injected (use Injector#inject(Object)). Note if the object is a different instance with the same properties, it must override the hashCode & equal methods to function properly.", target.toString()));
        }

        this.uninject0(target, this.originalValues.remove(target));
    }

    protected abstract Object inject0(Object target);

    protected abstract void uninject0(Object target, Object previous);
}

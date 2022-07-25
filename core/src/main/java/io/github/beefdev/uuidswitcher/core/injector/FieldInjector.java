package io.github.beefdev.uuidswitcher.core.injector;

import io.github.beefdev.uuidswitcher.common.util.ReflectionHelper;
import io.github.beefdev.uuidswitcher.core.injector.base.Injector;

import java.lang.reflect.Field;

public abstract class FieldInjector extends Injector {

    private final Class<?> targetType;
    private final Class<?> fieldType;

    public FieldInjector(Class<?> targetType, Class<?> fieldType) {
        this.targetType = targetType;
        this.fieldType = fieldType;
    }

    @Override
    protected final Object inject0(Object target) {
        if(!this.targetType.isInstance(target)) {
            throw new IllegalArgumentException(String.format("%s is not a subclass of %s", target.getClass().getName(), this.targetType.getName()));
        }

        Field field = ReflectionHelper.fetchField(this.targetType, this.fieldType);
        Object original = ReflectionHelper.fetchProperty(field, target);

        Object patched = this.patchObject(original);

        ReflectionHelper.setFieldUnrestricted(target, field, patched);
        return original;
    }

    @Override
    protected final void uninject0(Object target, Object original) {
        if(!this.targetType.isInstance(target)) {
            throw new IllegalArgumentException(String.format("%s is not a subclass of %s", target.getClass().getName(), this.targetType.getName()));
        }

        Field field = ReflectionHelper.fetchField(this.targetType, this.fieldType);
        ReflectionHelper.setFieldUnrestricted(target, field, original);
    }

    protected abstract Object patchObject(Object original);
}
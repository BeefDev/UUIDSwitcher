package io.github.beefdev.uuidswitcher.common.util;

import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

@SuppressWarnings("unused")
public final class ReflectionHelper {

    private static final Unsafe UNSAFE;

    static {
        Field unsafeField;

        try {
            unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        } catch (NoSuchFieldException exception) {
            throw new RuntimeException("Exception encountered while getting the Unsafe field", exception);
        }

        unsafeField.setAccessible(true);
        try {
            UNSAFE = (Unsafe) unsafeField.get(null);
        } catch (IllegalAccessException exception) {
            throw new RuntimeException("Exception encountered while getting the Unsafe from the unsafe field", exception);
        }

        unsafeField.setAccessible(false);
    }

    private ReflectionHelper() throws InstantiationException {
        throw new InstantiationException("Can not instantiate utility clas " + this.getClass().getName());
    }

    public static Class<?> fetchClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException exception) {
            throw new RuntimeException(String.format("Exception encountered while getting class %s", name), exception);
        }
    }

    public static Class<?> fetchClass(String name, String fallback) {
        try {
            return fetchClass(name);
        } catch (RuntimeException ignored) {}

        try {
            return fetchClass(fallback);
        } catch (RuntimeException exception) {
            throw new RuntimeException(String.format("Exception encountered while getting class %s", fallback), exception);
        }
    }

    public static Field fetchField(Class<?> declaringClassType, Class<?> fieldType) {
        for(Field field : declaringClassType.getDeclaredFields()) {
            if(field.getType() == fieldType) {
                return field;
            }
        }

        throw new RuntimeException(String.format("No fields of type %s found in %s", fieldType.getName(), declaringClassType.getName()));
    }

    public static Object fetchProperty(Class<?> declaringClassType, Class<?> fieldType, Object holder) {
        return fetchProperty(fetchField(declaringClassType, fieldType), holder);
    }

    public static Object fetchStaticProperty(Class<?> declaringClassType, Class<?> fieldType) {
        return fetchStaticProperty(fetchField(declaringClassType, fieldType));
    }

    public static Object fetchProperty(Field field, Object holder) {
        field.setAccessible(true);

        try {
            Object object = field.get(holder);
            field.setAccessible(false);

            return object;
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(String.format("Exception encountered while attempting to get field %s", field), exception);
        }
    }

    public static Object fetchStaticProperty(Field field) {
        field.setAccessible(true);

        try {
            Object object = field.get(null);
            field.setAccessible(false);

            return object;
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(String.format("Exception encountered while attempting to get field %s", field), exception);
        }
    }

    public static Object invokeMethod(Class<?> declaringClass, Object holder, String name, Class<?>[] parameterTypes, Object[] parameters) {
        try {
            Method method = declaringClass.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);

            Object object = method.invoke(holder, parameters);
            method.setAccessible(false);

            return object;
        } catch (NoSuchMethodException exception) {
            throw new RuntimeException(String.format("Exception encountered while trying to get method %s in %s", name, declaringClass.getName()), exception);
        } catch (InvocationTargetException | IllegalAccessException exception) {
            throw new RuntimeException(String.format("Exception encountered while trying to invoke method %s in %s, with parameters %s", name, declaringClass.getName(), Arrays.toString(parameters)), exception);
        }
    }

    public static Object invokeStaticMethod(Class<?> declaringClass, String name, Class<?>[] parameterTypes, Object[] parameters) {
        return invokeMethod(declaringClass, null, name, parameterTypes, parameters);
    }

    public static Object invokeConstructor(Class<?> declaringClass, Class<?>[] parameterTypes, Object[] parameters) {
        try {
            Constructor<?> constructor = declaringClass.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);

            Object object = constructor.newInstance(parameters);
            constructor.setAccessible(false);

            return object;
        } catch (NoSuchMethodException exception) {
            throw new RuntimeException(String.format("Exception encountered while trying to get constructor %s(%s)", declaringClass.getName(), Arrays.toString(parameterTypes)), exception);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException exception) {
            throw new RuntimeException(String.format("Exception encountered while trying to invoke constructor in %s, with parameters %s", declaringClass.getName(), Arrays.toString(parameters)), exception);
        }
    }

    public static void setFieldUnrestricted(Object holder, Field field, Object value) {
        UNSAFE.putObject(holder, UNSAFE.objectFieldOffset(field), value);
    }
}

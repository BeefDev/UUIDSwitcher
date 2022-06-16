package io.github.beefdev.uuidswitcher.common.utils;

import io.github.beefdev.uuidswitcher.common.event.PlayerProfileCreationEvent;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PlayerPostLoginHandler {

    private final Class<?> loginListenerClass;
    private final Class<?> gameProfileClass;
    private final Object loginListener;

    private Field gameProfileField;
    private Field gameProfileNameField;
    private Field gameProfileUUIDField;
    private Constructor<?> gameProfileConstructor;

    public PlayerPostLoginHandler(Class<?> loginListenerClass, Class<?> gameProfileClass, Object loginListener) {
        this.loginListenerClass = loginListenerClass;
        this.gameProfileClass = gameProfileClass;
        this.loginListener = loginListener;
    }

    public void updateFields() {
        if(this.gameProfileField == null) {
            try {
                for(Field field : this.loginListenerClass.getDeclaredFields()) {
                    if(this.gameProfileClass.isAssignableFrom(field.getType())) {
                        gameProfileField = field;
                        break;
                    }
                }

                if(gameProfileField == null) throw new NoSuchFieldException("Game profile field not found");
            } catch (NoSuchFieldException exception) {
                throw new RuntimeException(String.format("game profile field of LoginListener %s, could not be found", this.loginListenerClass.getName()), exception);
            }

            gameProfileField.setAccessible(true);
        }

        Object oldGameProfile;

        try {
            oldGameProfile = gameProfileField.get(this.loginListener);
        } catch (IllegalAccessException exception) {
            throw new RuntimeException("Unexpected exception while getting game profile, please report this", exception);
        }

        String oldName;
        UUID oldUUID;

        if(this.gameProfileUUIDField == null || this.gameProfileNameField == null) {
            for(Field field : this.gameProfileClass.getDeclaredFields()) {
                if(field.getType() == String.class) {
                    gameProfileNameField = field;
                    if(gameProfileUUIDField != null) break;
                } else if(field.getType() == UUID.class) {
                    gameProfileUUIDField = field;
                    if(gameProfileNameField != null) break;
                }
            }

            if(gameProfileNameField == null) throw new RuntimeException("The name field of GameProfile was not found, this is a bug, please report it.");
            if(gameProfileUUIDField == null) throw new RuntimeException("The UUID field of GameProfile was not found, this is a bug, please report it.");

            gameProfileNameField.setAccessible(true);
            gameProfileUUIDField.setAccessible(true);
        }

        try {
            oldName = (String) gameProfileNameField.get(oldGameProfile);
            oldUUID = (UUID) gameProfileUUIDField.get(oldGameProfile);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(String.format("Unexpected exception while getting uuid and name from game profile, please report this to me. \nGameProfile class=%s\nname field name=%s\nuuid field name=%s", this.gameProfileClass.getName(), gameProfileNameField.getName(), gameProfileUUIDField.getName()));
        }

        PlayerProfileCreationEvent profileCreationEvent = new PlayerProfileCreationEvent(oldName, oldUUID);
        Bukkit.getPluginManager().callEvent(profileCreationEvent);

        String newName = profileCreationEvent.getName();
        UUID newUUID = profileCreationEvent.getUUID();

        Object newGameProfile;

        try {
            if(gameProfileConstructor == null) gameProfileConstructor = this.gameProfileClass.getDeclaredConstructor(UUID.class, String.class);
            newGameProfile = gameProfileConstructor.newInstance(newUUID, newName);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(String.format("Unexpected exception while invoking game profile constructor, please report this to me. \nGameProfile class=%s\nconstructor=%s", this.gameProfileClass.getName(), gameProfileConstructor));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(String.format("Unexpected exception while getting game profile constructor, please report this to me. \nGameProfile class=%s\nAvailable class constructors=%s", this.gameProfileClass.getName(), Arrays.stream(this.gameProfileClass.getDeclaredConstructors()).map(Constructor::toString).collect(Collectors.toList())));
        }

        try {
            gameProfileField.set(this.loginListener, newGameProfile);
        } catch (IllegalAccessException exception) {
            throw new RuntimeException("Unexpected exception while setting new game profile, please report this", exception);
        }
    }
}

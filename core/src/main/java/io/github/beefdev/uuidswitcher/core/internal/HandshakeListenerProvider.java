package io.github.beefdev.uuidswitcher.core.internal;

import java.util.HashMap;
import java.util.Map;

public final class HandshakeListenerProvider {

    private static final Map<String, String> REPLACEMENT_TABLE;

    static {
        REPLACEMENT_TABLE = new HashMap<>();

        REPLACEMENT_TABLE.put("v1_8_4_R0_1", "v1_8_8_R0_1");
        REPLACEMENT_TABLE.put("v1_8_5_R0_1", "v1_8_8_R0_1");
        REPLACEMENT_TABLE.put("v1_8_6_R0_1", "v1_8_8_R0_1");
        REPLACEMENT_TABLE.put("v1_8_7_R0_1", "v1_8_8_R0_1");
        REPLACEMENT_TABLE.put("v1_10_R0_1", "v1_10_2_R0_1");
        REPLACEMENT_TABLE.put("v1_11_1_R0_1", "v1_11_2_R0_1");
        REPLACEMENT_TABLE.put("v1_14_1_R0_1", "v1_14_2_R0_1");
    }

    private final String parsedMinecraftVersion;
    private final String basePackage;

    public HandshakeListenerProvider(String basePackage, String parsedMinecraftVersion) {
        this.basePackage = basePackage;
        this.parsedMinecraftVersion = REPLACEMENT_TABLE.getOrDefault(parsedMinecraftVersion, parsedMinecraftVersion);
    }

    public Class<?> retrieveCustomHandshakeListenerClass() {
        try {
            return Class.forName(String.format("%s.%s.USHandshakeListener", this.basePackage, this.parsedMinecraftVersion));
        } catch (ClassNotFoundException exception) {
            throw new RuntimeException(String.format("Custom handshake listener class was not found. This usually means that you are trying to run the plugin on an unsupported server version (%s). If you are certain your version is supported then this is a bug, please report it to me", this.parsedMinecraftVersion), exception);
        }
    }
}

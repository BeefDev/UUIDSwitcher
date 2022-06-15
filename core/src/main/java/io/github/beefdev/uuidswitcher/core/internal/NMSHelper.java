package io.github.beefdev.uuidswitcher.core.internal;

import org.bukkit.Bukkit;

public final class NMSHelper {

    private NMSHelper() throws InstantiationException {
        throw new InstantiationException("Cannot instantiate utility class " + this.getClass().getName());
    }

    private static String getNmsVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    private static String getNmsTopLevelPackageName() {
        String nmsVersion = getNmsVersion();
        int majorVersion = Integer.parseInt(nmsVersion.substring(3,4).replace("_", ""));

        if(majorVersion > 16) return "net.minecraft.server.network";
        else return String.format("net.minecraft.server.%s", nmsVersion);
    }

    public static Class<?> fetchClass(String className, String fallbackFullClassName) {
        try {
            return Class.forName(String.format("%s.%s", getNmsTopLevelPackageName(), className));
        } catch (ClassNotFoundException exception) {
            try {
                return Class.forName(fallbackFullClassName);
            } catch (ClassNotFoundException unexpected) {
                throw new RuntimeException("Unexpected exception, the fallback class couldn't be found, this is a bug, please report it to me", unexpected);
            }
        }
    }
}

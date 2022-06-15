package io.github.beefdev.uuidswitcher.core.internal;

import org.bukkit.Bukkit;

public final class MinecraftVersionParser {

    private final String rawMinecraftVersion;

    public MinecraftVersionParser() {
        this(Bukkit.getBukkitVersion());
    }

    public MinecraftVersionParser(String rawMinecraftVersion) {
        this.rawMinecraftVersion = rawMinecraftVersion;
    }

    public String parseVersion() {
        String snapshotLessVersion = this.rawMinecraftVersion.replace("-SNAPSHOT", "");
        String legalizedVersion = "v".concat(snapshotLessVersion.replace(".", "_").replace("-", "_"));

        if(legalizedVersion.endsWith("R0_1")) return legalizedVersion;
        else return legalizedVersion.concat("_R0_1");
    }
}

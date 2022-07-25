package io.github.beefdev.uuidswitcher.common.util.version;

import org.bukkit.Bukkit;

import java.util.Objects;

@SuppressWarnings("unused")
public final class MinecraftVersion {

    public static final MinecraftVersion RUNNING;

    static {
        RUNNING = MinecraftVersion.getRunning();
    }

    public static final MinecraftVersion v1_19_1 = new MinecraftVersion(19, 1);
    public static final MinecraftVersion v1_19 = new MinecraftVersion(19);
    public static final MinecraftVersion v1_18_2 = new MinecraftVersion(18, 2);
    public static final MinecraftVersion v1_18_1 = new MinecraftVersion(18, 1);
    public static final MinecraftVersion v1_18 = new MinecraftVersion(18);
    public static final MinecraftVersion v1_17_1 = new MinecraftVersion(17, 1);
    public static final MinecraftVersion v1_17 = new MinecraftVersion(17);
    public static final MinecraftVersion v1_16_5 = new MinecraftVersion(16, 5);
    public static final MinecraftVersion v1_16_4 = new MinecraftVersion(16, 4);
    public static final MinecraftVersion v1_16_3 = new MinecraftVersion(16, 3);
    public static final MinecraftVersion v1_16_2 = new MinecraftVersion(16, 2);
    public static final MinecraftVersion v1_16_1 = new MinecraftVersion(16, 1);
    public static final MinecraftVersion v1_16 = new MinecraftVersion(16);
    public static final MinecraftVersion v1_15_2 = new MinecraftVersion(15, 2);
    public static final MinecraftVersion v1_15_1 = new MinecraftVersion(15, 1);
    public static final MinecraftVersion v1_15 = new MinecraftVersion(15);
    public static final MinecraftVersion v1_14_4 = new MinecraftVersion(14, 4);
    public static final MinecraftVersion v1_14_3 = new MinecraftVersion(14, 3);
    public static final MinecraftVersion v1_14_2 = new MinecraftVersion(14, 2);
    public static final MinecraftVersion v1_14_1 = new MinecraftVersion(14, 1);
    public static final MinecraftVersion v1_14 = new MinecraftVersion(14);
    public static final MinecraftVersion v1_13_2 = new MinecraftVersion(13, 2);
    public static final MinecraftVersion v1_13_1 = new MinecraftVersion(13, 1);
    public static final MinecraftVersion v1_13 = new MinecraftVersion(13);
    public static final MinecraftVersion v1_12_2 = new MinecraftVersion(12, 2);
    public static final MinecraftVersion v1_12_1 = new MinecraftVersion(12, 1);
    public static final MinecraftVersion v1_12 = new MinecraftVersion(12);
    public static final MinecraftVersion v1_11_2 = new MinecraftVersion(11, 2);
    public static final MinecraftVersion v1_11_1 = new MinecraftVersion(11, 1);
    public static final MinecraftVersion v1_11 = new MinecraftVersion(11);
    public static final MinecraftVersion v1_10_2 = new MinecraftVersion(10, 2);
    public static final MinecraftVersion v1_10_1 = new MinecraftVersion(10, 1);
    public static final MinecraftVersion v1_10 = new MinecraftVersion(10);
    public static final MinecraftVersion v1_9_4 = new MinecraftVersion(9, 4);
    public static final MinecraftVersion v1_9_3 = new MinecraftVersion(9, 3);
    public static final MinecraftVersion v1_9_2 = new MinecraftVersion(9, 2);
    public static final MinecraftVersion v1_9_1 = new MinecraftVersion(9, 1);
    public static final MinecraftVersion v1_9 = new MinecraftVersion(9);
    public static final MinecraftVersion v1_8_9 = new MinecraftVersion(8, 9);
    public static final MinecraftVersion v1_8_8 = new MinecraftVersion(8, 8);
    public static final MinecraftVersion v1_8_7 = new MinecraftVersion(8, 7);
    public static final MinecraftVersion v1_8_6 = new MinecraftVersion(8, 6);
    public static final MinecraftVersion v1_8_5 = new MinecraftVersion(8, 5);
    public static final MinecraftVersion v1_8_4 = new MinecraftVersion(8, 4);
    public static final MinecraftVersion v1_8_3 = new MinecraftVersion(8, 3);
    public static final MinecraftVersion v1_8_2 = new MinecraftVersion(8, 2);
    public static final MinecraftVersion v1_8_1 = new MinecraftVersion(8, 1);
    public static final MinecraftVersion v1_8 = new MinecraftVersion(8);

    private final int major;
    private final int minor;

    public MinecraftVersion(int major) {
        this(major, 0);
    }


    public MinecraftVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    public int getMajor() {
        return this.major;
    }

    public int getMinor() {
        return this.minor;
    }

    public boolean newerThan(MinecraftVersion other) {
        if(this.getMajor() > other.getMajor()) return true;
        if(this.getMajor() == other.getMajor()) return this.getMinor() > other.getMinor();

        return false;
    }

    public boolean olderThan(MinecraftVersion other) {
        if(this.getMajor() < other.getMajor()) return true;
        if(this.getMajor() == other.getMajor()) return this.getMinor() < other.getMinor();

        return false;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof MinecraftVersion && ((MinecraftVersion) other).getMajor() == this.getMajor() && ((MinecraftVersion) other).getMinor() == this.getMinor();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getMajor(), this.getMinor());
    }

    @Override
    public String toString() {
        return String.format("1.%s.%s", this.getMajor(), this.getMinor());
    }

    public static MinecraftVersion getRunning() {
        String rawVersionString = Bukkit.getBukkitVersion();
        String formattedVersion = rawVersionString.split("-")[0];
        String formattedVersionNumbers = formattedVersion.substring(2);

        String[] versionNumbers = formattedVersionNumbers.split("\\.");

        return new MinecraftVersion(
                Integer.parseInt(versionNumbers[0]),
                Integer.parseInt(versionNumbers[1])
        );
    }

    public static String getCurrentNMSVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    public static String getCurrentNMSTopLevelPackage() {
        if(RUNNING.newerThan(v1_16_5)) {
            return "net.minecraft.server.network";
        } else {
            return String.format("net.minecraft.server.%s", getCurrentNMSVersion());
        }
    }
}

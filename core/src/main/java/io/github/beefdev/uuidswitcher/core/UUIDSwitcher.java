package io.github.beefdev.uuidswitcher.core;

/**
 * A static handle for the {@link UUIDSwitcherHandle} class. The central class of UUIDSwitcher
 */
@SuppressWarnings("unused")
public final class UUIDSwitcher {

    private static UUIDSwitcherHandle HANDLE;

    private UUIDSwitcher() throws InstantiationException {
        throw new InstantiationException("Cannot instantiate utility class " + this.getClass().getName());
    }

    /**
     * Enable the UUIDSwitcher API
     * @see <a href="https://github.com/BeefDev/UUIDSwitcher/blob/main/README.md">WIKI</a>
     */
    public static void onEnable() {
        HANDLE = new UUIDSwitcherHandle();
        HANDLE.onEnable();
    }

    /**
     * Disable the UUIDSwitcher API
     * @see <a href="https://github.com/BeefDev/UUIDSwitcher/blob/main/README.md">WIKI</a>
     */
    public static void onDisable() {
        HANDLE.onDisable();
    }
}

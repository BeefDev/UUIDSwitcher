package io.github.beefdev.uuidswitcher.versions.v1_8_3_R0_1;

import com.google.gson.Gson;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.server.v1_8_R2.*;
import org.apache.logging.log4j.LogManager;
import org.spigotmc.SpigotConfig;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class USHandshakeListener implements PacketHandshakingInListener {
    private static final Gson gson = new Gson();
    private static final HashMap<InetAddress, Long> throttleTracker = new HashMap();
    private static int throttleCounter = 0;
    private final MinecraftServer a;
    private final NetworkManager b;

    public USHandshakeListener(MinecraftServer minecraftserver, NetworkManager networkmanager) {
        this.a = minecraftserver;
        this.b = networkmanager;
    }

    public void a(PacketHandshakingInSetProtocol packethandshakinginsetprotocol) {
        switch (packethandshakinginsetprotocol.a()) {
            case LOGIN:
                this.b.a(EnumProtocol.LOGIN);

                ChatComponentText chatcomponenttext;
                try {
                    long currentTime = System.currentTimeMillis();
                    long connectionThrottle = MinecraftServer.getServer().server.getConnectionThrottle();
                    InetAddress address = ((InetSocketAddress)this.b.getSocketAddress()).getAddress();
                    synchronized(throttleTracker) {
                        if (throttleTracker.containsKey(address) && !"127.0.0.1".equals(address.getHostAddress()) && currentTime - (Long)throttleTracker.get(address) < connectionThrottle) {
                            throttleTracker.put(address, currentTime);
                            chatcomponenttext = new ChatComponentText("Connection throttled! Please wait before reconnecting.");
                            this.b.handle(new PacketLoginOutDisconnect(chatcomponenttext));
                            this.b.close(chatcomponenttext);
                            return;
                        }

                        throttleTracker.put(address, currentTime);
                        ++throttleCounter;
                        if (throttleCounter > 200) {
                            throttleCounter = 0;
                            Iterator iter = throttleTracker.entrySet().iterator();

                            while(iter.hasNext()) {
                                Map.Entry<InetAddress, Long> entry = (Map.Entry)iter.next();
                                if ((Long)entry.getValue() > connectionThrottle) {
                                    iter.remove();
                                }
                            }
                        }
                    }
                } catch (Throwable var13) {
                    LogManager.getLogger().debug("Failed to check connection throttle", var13);
                }

                if (packethandshakinginsetprotocol.b() > 47) {
                    chatcomponenttext = new ChatComponentText(MessageFormat.format(SpigotConfig.outdatedServerMessage, "1.8.3"));
                    this.b.handle(new PacketLoginOutDisconnect(chatcomponenttext));
                    this.b.close(chatcomponenttext);
                } else if (packethandshakinginsetprotocol.b() < 47) {
                    chatcomponenttext = new ChatComponentText(MessageFormat.format(SpigotConfig.outdatedClientMessage, "1.8.3"));
                    this.b.handle(new PacketLoginOutDisconnect(chatcomponenttext));
                    this.b.close(chatcomponenttext);
                } else {
                    this.b.a(new USLoginListener(this.a, this.b));
                    if (SpigotConfig.bungee) {
                        String[] split = packethandshakinginsetprotocol.b.split("\u0000");
                        if (split.length != 3 && split.length != 4) {
                            chatcomponenttext = new ChatComponentText("If you wish to use IP forwarding, please enable it in your BungeeCord config as well!");
                            this.b.handle(new PacketLoginOutDisconnect(chatcomponenttext));
                            this.b.close(chatcomponenttext);
                            return;
                        }

                        packethandshakinginsetprotocol.b = split[0];
                        this.b.l = new InetSocketAddress(split[1], ((InetSocketAddress)this.b.getSocketAddress()).getPort());
                        this.b.spoofedUUID = UUIDTypeAdapter.fromString(split[2]);
                        if (split.length == 4) {
                            this.b.spoofedProfile = (Property[])gson.fromJson(split[3], Property[].class);
                        }
                    }

                    try {
                        LoginListener.class.getDeclaredField("hostname");
                        ((LoginListener)this.b.getPacketListener()).hostname = packethandshakinginsetprotocol.b + ":" + packethandshakinginsetprotocol.c;
                    } catch (Throwable ignored){}

                }
                break;
            case STATUS:
                this.b.a(EnumProtocol.STATUS);
                this.b.a(new PacketStatusListener(this.a, this.b));
                break;
            default:
                throw new UnsupportedOperationException("Invalid intention " + packethandshakinginsetprotocol.a());
        }

    }

    public void a(IChatBaseComponent ichatbasecomponent) {
    }

    static class SyntheticClass_1 {
        static final int[] a = new int[EnumProtocol.values().length];

        static {
            try {
                a[EnumProtocol.LOGIN.ordinal()] = 1;
            } catch (NoSuchFieldError var1) {
            }

            try {
                a[EnumProtocol.STATUS.ordinal()] = 2;
            } catch (NoSuchFieldError var0) {
            }

        }

        SyntheticClass_1() {
        }
    }
}

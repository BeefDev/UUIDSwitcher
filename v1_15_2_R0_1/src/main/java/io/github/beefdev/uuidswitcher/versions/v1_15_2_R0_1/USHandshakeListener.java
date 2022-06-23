package io.github.beefdev.uuidswitcher.versions.v1_15_2_R0_1;

import com.google.gson.Gson;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.server.v1_15_R1.*;
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
        switch (packethandshakinginsetprotocol.b().ordinal()+1) {
            case 3:
                this.b.setProtocol(EnumProtocol.STATUS);
                this.b.setPacketListener(new PacketStatusListener(this.a, this.b));
                break;
            case 4:
                this.b.setProtocol(EnumProtocol.LOGIN);

                ChatMessage chatmessage;
                try {
                    long currentTime = System.currentTimeMillis();
                    long connectionThrottle = MinecraftServer.getServer().server.getConnectionThrottle();
                    InetAddress address = ((InetSocketAddress)this.b.getSocketAddress()).getAddress();
                    synchronized(throttleTracker) {
                        if (throttleTracker.containsKey(address) && !"127.0.0.1".equals(address.getHostAddress()) && currentTime - (Long)throttleTracker.get(address) < connectionThrottle) {
                            throttleTracker.put(address, currentTime);
                            chatmessage = new ChatMessage("Connection throttled! Please wait before reconnecting.", new Object[0]);
                            this.b.sendPacket(new PacketLoginOutDisconnect(chatmessage));
                            this.b.close(chatmessage);
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
                } catch (Throwable var12) {
                    LogManager.getLogger().debug("Failed to check connection throttle", var12);
                }

                if (packethandshakinginsetprotocol.c() > SharedConstants.getGameVersion().getProtocolVersion()) {
                    chatmessage = new ChatMessage(MessageFormat.format(SpigotConfig.outdatedServerMessage.replaceAll("'", "''"), SharedConstants.getGameVersion().getName()), new Object[0]);
                    this.b.sendPacket(new PacketLoginOutDisconnect(chatmessage));
                    this.b.close(chatmessage);
                } else if (packethandshakinginsetprotocol.c() < SharedConstants.getGameVersion().getProtocolVersion()) {
                    chatmessage = new ChatMessage(MessageFormat.format(SpigotConfig.outdatedClientMessage.replaceAll("'", "''"), SharedConstants.getGameVersion().getName()), new Object[0]);
                    this.b.sendPacket(new PacketLoginOutDisconnect(chatmessage));
                    this.b.close(chatmessage);
                } else {
                    this.b.setPacketListener(new USLoginListener(this.a, this.b));
                    if (SpigotConfig.bungee) {
                        String[] split = packethandshakinginsetprotocol.hostname.split("\u0000");
                        if (split.length != 3 && split.length != 4) {
                            chatmessage = new ChatMessage("If you wish to use IP forwarding, please enable it in your BungeeCord config as well!", new Object[0]);
                            this.b.sendPacket(new PacketLoginOutDisconnect(chatmessage));
                            this.b.close(chatmessage);
                            return;
                        }

                        packethandshakinginsetprotocol.hostname = split[0];
                        this.b.socketAddress = new InetSocketAddress(split[1], ((InetSocketAddress)this.b.getSocketAddress()).getPort());
                        this.b.spoofedUUID = UUIDTypeAdapter.fromString(split[2]);
                        if (split.length == 4) {
                            this.b.spoofedProfile = (Property[])gson.fromJson(split[3], Property[].class);
                        }
                    }

                    ((USLoginListener)this.b.i()).hostname = packethandshakinginsetprotocol.hostname + ":" + packethandshakinginsetprotocol.port;
                }
                break;
            default:
                throw new UnsupportedOperationException("Invalid intention " + packethandshakinginsetprotocol.b());
        }

    }

    public void a(IChatBaseComponent ichatbasecomponent) {
    }

    public NetworkManager a() {
        return this.b;
    }
}

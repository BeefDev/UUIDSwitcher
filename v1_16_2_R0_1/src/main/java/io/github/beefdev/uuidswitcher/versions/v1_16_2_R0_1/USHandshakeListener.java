package io.github.beefdev.uuidswitcher.versions.v1_16_2_R0_1;

import com.google.gson.Gson;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.server.v1_16_R2.*;
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
    private static final IChatBaseComponent a = new ChatComponentText("Ignoring status request");
    private final MinecraftServer b;
    private final NetworkManager c;

    public USHandshakeListener(MinecraftServer minecraftserver, NetworkManager networkmanager) {
        this.b = minecraftserver;
        this.c = networkmanager;
    }

    public void a(PacketHandshakingInSetProtocol packethandshakinginsetprotocol) {
        switch (packethandshakinginsetprotocol.b().ordinal()+1) {
            case 3:
                if (this.b.al()) {
                    this.c.setProtocol(EnumProtocol.STATUS);
                    this.c.setPacketListener(new PacketStatusListener(this.b, this.c));
                } else {
                    this.c.close(a);
                }
                break;
            case 4:
                this.c.setProtocol(EnumProtocol.LOGIN);

                ChatMessage chatmessage;
                try {
                    long currentTime = System.currentTimeMillis();
                    long connectionThrottle = this.b.server.getConnectionThrottle();
                    InetAddress address = ((InetSocketAddress)this.c.getSocketAddress()).getAddress();
                    synchronized(throttleTracker) {
                        if (throttleTracker.containsKey(address) && !"127.0.0.1".equals(address.getHostAddress()) && currentTime - (Long)throttleTracker.get(address) < connectionThrottle) {
                            throttleTracker.put(address, currentTime);
                            chatmessage = new ChatMessage("Connection throttled! Please wait before reconnecting.");
                            this.c.sendPacket(new PacketLoginOutDisconnect(chatmessage));
                            this.c.close(chatmessage);
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
                    chatmessage = new ChatMessage(MessageFormat.format(SpigotConfig.outdatedServerMessage.replaceAll("'", "''"), SharedConstants.getGameVersion().getName()));
                    this.c.sendPacket(new PacketLoginOutDisconnect(chatmessage));
                    this.c.close(chatmessage);
                } else if (packethandshakinginsetprotocol.c() < SharedConstants.getGameVersion().getProtocolVersion()) {
                    chatmessage = new ChatMessage(MessageFormat.format(SpigotConfig.outdatedClientMessage.replaceAll("'", "''"), SharedConstants.getGameVersion().getName()));
                    this.c.sendPacket(new PacketLoginOutDisconnect(chatmessage));
                    this.c.close(chatmessage);
                } else {
                    this.c.setPacketListener(new USLoginListener(this.b, this.c));
                    if (SpigotConfig.bungee) {
                        String[] split = packethandshakinginsetprotocol.hostname.split("\u0000");
                        if (split.length != 3 && split.length != 4) {
                            chatmessage = new ChatMessage("If you wish to use IP forwarding, please enable it in your BungeeCord config as well!");
                            this.c.sendPacket(new PacketLoginOutDisconnect(chatmessage));
                            this.c.close(chatmessage);
                            return;
                        }

                        packethandshakinginsetprotocol.hostname = split[0];
                        this.c.socketAddress = new InetSocketAddress(split[1], ((InetSocketAddress)this.c.getSocketAddress()).getPort());
                        this.c.spoofedUUID = UUIDTypeAdapter.fromString(split[2]);
                        if (split.length == 4) {
                            this.c.spoofedProfile = (Property[])gson.fromJson(split[3], Property[].class);
                        }
                    }

                    ((USLoginListener)this.c.j()).hostname = packethandshakinginsetprotocol.hostname + ":" + packethandshakinginsetprotocol.port;
                }
                break;
            default:
                throw new UnsupportedOperationException("Invalid intention " + packethandshakinginsetprotocol.b());
        }

    }

    public void a(IChatBaseComponent ichatbasecomponent) {
    }

    public NetworkManager a() {
        return this.c;
    }
}

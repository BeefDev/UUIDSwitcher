package io.github.beefdev.uuidswitcher.versions.v1_18_2_R0_1;

import com.google.gson.Gson;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.SharedConstants;
import net.minecraft.network.EnumProtocol;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.ChatMessage;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.handshake.PacketHandshakingInListener;
import net.minecraft.network.protocol.handshake.PacketHandshakingInSetProtocol;
import net.minecraft.network.protocol.login.PacketLoginOutDisconnect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.LoginListener;
import net.minecraft.server.network.PacketStatusListener;
import org.apache.logging.log4j.LogManager;
import org.spigotmc.SpigotConfig;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public final class USHandshakeListener implements PacketHandshakingInListener {
    private static final Gson gson = new Gson();
    static final Pattern HOST_PATTERN = Pattern.compile("[0-9a-f\\.:]{0,45}");
    static final Pattern PROP_PATTERN = Pattern.compile("\\w{0,16}");
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
                if (this.b.ai()) {
                    this.c.a(EnumProtocol.c);
                    this.c.a(new PacketStatusListener(this.b, this.c));
                } else {
                    this.c.a(a);
                }
                break;
            case 4:
                this.c.a(EnumProtocol.d);

                try {
                    long currentTime = System.currentTimeMillis();
                    long connectionThrottle = this.b.server.getConnectionThrottle();
                    InetAddress address = ((InetSocketAddress)this.c.c()).getAddress();
                    synchronized(throttleTracker) {
                        if (throttleTracker.containsKey(address) && !"127.0.0.1".equals(address.getHostAddress()) && currentTime - (Long)throttleTracker.get(address) < connectionThrottle) {
                            throttleTracker.put(address, currentTime);
                            ChatMessage chatmessage = new ChatMessage("Connection throttled! Please wait before reconnecting.");
                            this.c.a(new PacketLoginOutDisconnect(chatmessage));
                            this.c.a(chatmessage);
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
                } catch (Throwable var11) {
                    LogManager.getLogger().debug("Failed to check connection throttle", var11);
                }

                if (packethandshakinginsetprotocol.c() != SharedConstants.b().getProtocolVersion()) {
                    ChatMessage chatmessage;
                    if (packethandshakinginsetprotocol.c() < 754) {
                        chatmessage = new ChatMessage(MessageFormat.format(SpigotConfig.outdatedClientMessage.replaceAll("'", "''"), SharedConstants.b().getName()));
                    } else {
                        chatmessage = new ChatMessage(MessageFormat.format(SpigotConfig.outdatedServerMessage.replaceAll("'", "''"), SharedConstants.b().getName()));
                    }

                    this.c.a(new PacketLoginOutDisconnect(chatmessage));
                    this.c.a(chatmessage);
                    break;
                }

                this.c.a(new USLoginListener(this.b, this.c));
                String[] split = packethandshakinginsetprotocol.c.split("\u0000");
                ChatMessage chatmessage;
                if (!SpigotConfig.bungee) {
                    if ((split.length == 3 || split.length == 4) && HOST_PATTERN.matcher(split[1]).matches()) {
                        chatmessage = new ChatMessage("Unknown data in login hostname, did you forget to enable BungeeCord in spigot.yml?");
                        this.c.a(new PacketLoginOutDisconnect(chatmessage));
                        this.c.a(chatmessage);
                        return;
                    }
                } else {
                    if (split.length != 3 && split.length != 4 || !HOST_PATTERN.matcher(split[1]).matches()) {
                        chatmessage = new ChatMessage("If you wish to use IP forwarding, please enable it in your BungeeCord config as well!");
                        this.c.a(new PacketLoginOutDisconnect(chatmessage));
                        this.c.a(chatmessage);
                        return;
                    }

                    packethandshakinginsetprotocol.c = split[0];
                    this.c.n = new InetSocketAddress(split[1], ((InetSocketAddress)this.c.c()).getPort());
                    this.c.spoofedUUID = UUIDTypeAdapter.fromString(split[2]);
                    if (split.length == 4) {
                        this.c.spoofedProfile = (Property[])gson.fromJson(split[3], Property[].class);
                    }
                }

                ((LoginListener)this.c.j()).hostname = packethandshakinginsetprotocol.c + ":" + packethandshakinginsetprotocol.d;
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

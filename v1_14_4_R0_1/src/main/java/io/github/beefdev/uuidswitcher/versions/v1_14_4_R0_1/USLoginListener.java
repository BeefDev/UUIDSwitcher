package io.github.beefdev.uuidswitcher.versions.v1_14_4_R0_1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.properties.Property;
import io.github.beefdev.uuidswitcher.common.event.AsyncPlayerProfileCreationEvent;
import net.minecraft.server.v1_14_R1.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.craftbukkit.v1_14_R1.util.Waitable;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;

import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public final class USLoginListener extends LoginListener implements PacketLoginInListener {
    private static final AtomicInteger b = new AtomicInteger(0);
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Random random = new Random();
    private final byte[] e = new byte[4];
    private final MinecraftServer server;
    public final NetworkManager networkManager;
    private USLoginListener.EnumProtocolState g;
    private int h;
    private GameProfile i;
    private final String j;
    private SecretKey loginKey;
    private EntityPlayer l;
    public String hostname = "";

    public USLoginListener(MinecraftServer minecraftserver, NetworkManager networkmanager) {
        super(minecraftserver, networkmanager);

        this.g = USLoginListener.EnumProtocolState.HELLO;
        this.j = "";
        this.server = minecraftserver;
        this.networkManager = networkmanager;
        random.nextBytes(this.e);
    }

    public void tick() {
        if (this.g == USLoginListener.EnumProtocolState.READY_TO_ACCEPT) {
            this.c();
        } else if (this.g == USLoginListener.EnumProtocolState.DELAY_ACCEPT) {
            EntityPlayer entityplayer = this.server.getPlayerList().a(this.i.getId());
            if (entityplayer == null) {
                this.g = USLoginListener.EnumProtocolState.READY_TO_ACCEPT;
                this.server.getPlayerList().a(this.networkManager, this.l);
                this.l = null;
            }
        }

        if (this.h++ == 600) {
            this.disconnect((IChatBaseComponent)(new ChatMessage("multiplayer.disconnect.slow_login", new Object[0])));
        }

    }

    /** @deprecated */
    @Deprecated
    public void disconnect(String s) {
        try {
            IChatBaseComponent ichatbasecomponent = new ChatComponentText(s);
            LOGGER.info("Disconnecting {}: {}", this.d(), s);
            this.networkManager.sendPacket(new PacketLoginOutDisconnect(ichatbasecomponent));
            this.networkManager.close(ichatbasecomponent);
        } catch (Exception var3) {
            LOGGER.error("Error whilst disconnecting player", var3);
        }

    }

    public NetworkManager a() {
        return this.networkManager;
    }

    public void disconnect(IChatBaseComponent ichatbasecomponent) {
        try {
            LOGGER.info("Disconnecting {}: {}", this.d(), ichatbasecomponent.getString());
            this.networkManager.sendPacket(new PacketLoginOutDisconnect(ichatbasecomponent));
            this.networkManager.close(ichatbasecomponent);
        } catch (Exception var3) {
            LOGGER.error("Error whilst disconnecting player", var3);
        }

    }

    public void initUUID() {
        UUID uuid;
        if (this.networkManager.spoofedUUID != null) {
            uuid = this.networkManager.spoofedUUID;
        } else {
            uuid = EntityHuman.getOfflineUUID(this.i.getName());
        }

        this.i = new GameProfile(uuid, this.i.getName());
        if (this.networkManager.spoofedProfile != null) {
            Property[] var5;
            int var4 = (var5 = this.networkManager.spoofedProfile).length;

            for(int var3 = 0; var3 < var4; ++var3) {
                Property property = var5[var3];
                this.i.getProperties().put(property.getName(), property);
            }
        }

    }

    public void c() {
        EntityPlayer s = this.server.getPlayerList().attemptLogin(this, this.i, this.hostname);
        if (s != null) {
            this.g = USLoginListener.EnumProtocolState.ACCEPTED;
            if (this.server.az() >= 0 && !this.networkManager.isLocal()) {
                this.networkManager.sendPacket(new PacketLoginOutSetCompression(this.server.az()), (channelfuture) -> {
                    this.networkManager.setCompressionLevel(this.server.az());
                });
            }

            this.networkManager.sendPacket(new PacketLoginOutSuccess(this.i));
            EntityPlayer entityplayer = this.server.getPlayerList().a(this.i.getId());
            if (entityplayer != null) {
                this.g = USLoginListener.EnumProtocolState.DELAY_ACCEPT;
                this.l = this.server.getPlayerList().processLogin(this.i, s);
            } else {
                this.server.getPlayerList().a(this.networkManager, this.server.getPlayerList().processLogin(this.i, s));
            }
        }

    }

    public void a(IChatBaseComponent ichatbasecomponent) {
        LOGGER.info("{} lost connection: {}", this.d(), ichatbasecomponent.getString());
    }

    public String d() {
        return this.i != null ? this.i + " (" + this.networkManager.getSocketAddress() + ")" : String.valueOf(this.networkManager.getSocketAddress());
    }

    public void a(PacketLoginInStart packetlogininstart) {
        Validate.validState(this.g == USLoginListener.EnumProtocolState.HELLO, "Unexpected hello packet", new Object[0]);
        this.i = packetlogininstart.b();
        if (this.server.getOnlineMode() && !this.networkManager.isLocal()) {
            this.g = USLoginListener.EnumProtocolState.KEY;
            this.networkManager.sendPacket(new PacketLoginOutEncryptionBegin("", this.server.getKeyPair().getPublic(), this.e));
        } else {
            (new Thread("User Authenticator #" + b.incrementAndGet()) {
                public void run() {
                    try {
                        USLoginListener.this.initUUID();
                        (USLoginListener.this.new LoginHandler()).fireEvents();
                    } catch (Exception var2) {
                        USLoginListener.this.disconnect("Failed to verify username!");
                        USLoginListener.this.server.server.getLogger().log(Level.WARNING, "Exception verifying " + USLoginListener.this.i.getName(), var2);
                    }

                }
            }).start();
        }

    }

    public void a(PacketLoginInEncryptionBegin packetlogininencryptionbegin) {
        Validate.validState(this.g == USLoginListener.EnumProtocolState.KEY, "Unexpected key packet", new Object[0]);
        PrivateKey privatekey = this.server.getKeyPair().getPrivate();
        if (!Arrays.equals(this.e, packetlogininencryptionbegin.b(privatekey))) {
            throw new IllegalStateException("Invalid nonce!");
        } else {
            this.loginKey = packetlogininencryptionbegin.a(privatekey);
            this.g = USLoginListener.EnumProtocolState.AUTHENTICATING;
            this.networkManager.a(this.loginKey);
            Thread thread = new Thread("User Authenticator #" + b.incrementAndGet()) {
                public void run() {
                    GameProfile gameprofile = USLoginListener.this.i;

                    try {
                        String s = (new BigInteger(MinecraftEncryption.a("", USLoginListener.this.server.getKeyPair().getPublic(), USLoginListener.this.loginKey))).toString(16);
                        USLoginListener.this.i = USLoginListener.this.server.getMinecraftSessionService().hasJoinedServer(new GameProfile((UUID)null, gameprofile.getName()), s, this.a());
                        if (USLoginListener.this.i != null) {
                            if (!USLoginListener.this.networkManager.isConnected()) {
                                return;
                            }

                            (USLoginListener.this.new LoginHandler()).fireEvents();
                        } else if (USLoginListener.this.server.isEmbeddedServer()) {
                            USLoginListener.LOGGER.warn("Failed to verify username but will let them in anyway!");
                            USLoginListener.this.i = USLoginListener.this.a(gameprofile);
                            USLoginListener.this.g = USLoginListener.EnumProtocolState.READY_TO_ACCEPT;
                        } else {
                            USLoginListener.this.disconnect((IChatBaseComponent)(new ChatMessage("multiplayer.disconnect.unverified_username", new Object[0])));
                            USLoginListener.LOGGER.error("Username '{}' tried to join with an invalid session", gameprofile.getName());
                        }
                    } catch (AuthenticationUnavailableException var3) {
                        if (USLoginListener.this.server.isEmbeddedServer()) {
                            USLoginListener.LOGGER.warn("Authentication servers are down but will let them in anyway!");
                            USLoginListener.this.i = USLoginListener.this.a(gameprofile);
                            USLoginListener.this.g = USLoginListener.EnumProtocolState.READY_TO_ACCEPT;
                        } else {
                            USLoginListener.this.disconnect((IChatBaseComponent)(new ChatMessage("multiplayer.disconnect.authservers_down", new Object[0])));
                            USLoginListener.LOGGER.error("Couldn't verify username because servers are unavailable");
                        }
                    } catch (Exception var4) {
                        USLoginListener.this.disconnect("Failed to verify username!");
                        USLoginListener.this.server.server.getLogger().log(Level.WARNING, "Exception verifying " + gameprofile.getName(), var4);
                    }

                }

                @Nullable
                private InetAddress a() {
                    SocketAddress socketaddress = USLoginListener.this.networkManager.getSocketAddress();
                    return USLoginListener.this.server.U() && socketaddress instanceof InetSocketAddress ? ((InetSocketAddress)socketaddress).getAddress() : null;
                }
            };
            thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
            thread.start();
        }
    }

    public void a(PacketLoginInCustomPayload packetloginincustompayload) {
        this.disconnect((IChatBaseComponent)(new ChatMessage("multiplayer.disconnect.unexpected_query_response", new Object[0])));
    }

    protected GameProfile a(GameProfile gameprofile) {
        UUID uuid = EntityHuman.getOfflineUUID(gameprofile.getName());
        return new GameProfile(uuid, gameprofile.getName());
    }

    static enum EnumProtocolState {
        HELLO,
        KEY,
        AUTHENTICATING,
        NEGOTIATING,
        READY_TO_ACCEPT,
        DELAY_ACCEPT,
        ACCEPTED;

        private EnumProtocolState() {
        }
    }

    public class LoginHandler {
        public LoginHandler() {
        }

        public void fireEvents() throws Exception {
            String playerName = USLoginListener.this.i.getName();
            InetAddress address = ((InetSocketAddress) USLoginListener.this.networkManager.getSocketAddress()).getAddress();
            UUID uniqueId = USLoginListener.this.i.getId();

            final CraftServer server = USLoginListener.this.server.server;

            AsyncPlayerProfileCreationEvent asyncProfileCreationEvent = new AsyncPlayerProfileCreationEvent(address, playerName, uniqueId);
            server.getPluginManager().callEvent(asyncProfileCreationEvent);

            uniqueId = asyncProfileCreationEvent.getUUID();
            playerName = asyncProfileCreationEvent.getName();

            USLoginListener.this.i = new GameProfile(uniqueId, playerName);

            AsyncPlayerPreLoginEvent asyncEvent = new AsyncPlayerPreLoginEvent(playerName, address, uniqueId);
            server.getPluginManager().callEvent(asyncEvent);
            if (PlayerPreLoginEvent.getHandlerList().getRegisteredListeners().length != 0) {
                final PlayerPreLoginEvent event = new PlayerPreLoginEvent(playerName, address, uniqueId);
                if (asyncEvent.getResult() != PlayerPreLoginEvent.Result.ALLOWED) {
                    event.disallow(asyncEvent.getResult(), asyncEvent.getKickMessage());
                }

                Waitable<PlayerPreLoginEvent.Result> waitable = new Waitable<PlayerPreLoginEvent.Result>() {
                    protected PlayerPreLoginEvent.Result evaluate() {
                        server.getPluginManager().callEvent(event);
                        return event.getResult();
                    }
                };
                USLoginListener.this.server.processQueue.add(waitable);
                if (waitable.get() != PlayerPreLoginEvent.Result.ALLOWED) {
                    USLoginListener.this.disconnect(event.getKickMessage());
                    return;
                }
            } else if (asyncEvent.getLoginResult() != org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.ALLOWED) {
                USLoginListener.this.disconnect(asyncEvent.getKickMessage());
                return;
            }

            USLoginListener.LOGGER.info("UUID of player {} is {}", USLoginListener.this.i.getName(), USLoginListener.this.i.getId());
            USLoginListener.this.g = USLoginListener.EnumProtocolState.READY_TO_ACCEPT;
        }
    }
}

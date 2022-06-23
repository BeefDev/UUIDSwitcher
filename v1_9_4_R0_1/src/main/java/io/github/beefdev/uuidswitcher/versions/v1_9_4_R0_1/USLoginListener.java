package io.github.beefdev.uuidswitcher.versions.v1_9_4_R0_1;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.properties.Property;
import io.github.beefdev.uuidswitcher.common.event.AsyncPlayerProfileCreationEvent;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.server.v1_9_R2.*;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.v1_9_R2.CraftServer;
import org.bukkit.craftbukkit.v1_9_R2.util.Waitable;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class USLoginListener extends LoginListener implements PacketLoginInListener, ITickable {
    private static final AtomicInteger b = new AtomicInteger(0);
    private static final Logger c = LogManager.getLogger();
    private static final Random random = new Random();
    private final byte[] e = new byte[4];
    private final MinecraftServer server;
    public final NetworkManager networkManager;
    private USLoginListener.EnumProtocolState g;
    private int h;
    private GameProfile i;
    private String j;
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

    public void c() {
        if (this.g == USLoginListener.EnumProtocolState.READY_TO_ACCEPT) {
            this.b();
        } else if (this.g == USLoginListener.EnumProtocolState.DELAY_ACCEPT) {
            EntityPlayer entityplayer = this.server.getPlayerList().a(this.i.getId());
            if (entityplayer == null) {
                this.g = USLoginListener.EnumProtocolState.READY_TO_ACCEPT;
                this.server.getPlayerList().a(this.networkManager, this.l);
                this.l = null;
            }
        }

        if (this.h++ == 600) {
            this.disconnect("Took too long to log in");
        }

    }

    public void disconnect(String s) {
        try {
            c.info("Disconnecting " + this.d() + ": " + s);
            ChatComponentText chatcomponenttext = new ChatComponentText(s);
            this.networkManager.sendPacket(new PacketLoginOutDisconnect(chatcomponenttext));
            this.networkManager.close(chatcomponenttext);
        } catch (Exception var3) {
            c.error("Error whilst disconnecting player", var3);
        }

    }

    public void initUUID() {
        UUID uuid;
        if (this.networkManager.spoofedUUID != null) {
            uuid = this.networkManager.spoofedUUID;
        } else {
            uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + this.i.getName()).getBytes(Charsets.UTF_8));
        }

        this.i = new GameProfile(uuid, this.i.getName());
        if (this.networkManager.spoofedProfile != null) {
            Property[] var2;
            int var3 = (var2 = this.networkManager.spoofedProfile).length;

            for(int var4 = 0; var4 < var3; ++var4) {
                Property property = var2[var4];
                this.i.getProperties().put(property.getName(), property);
            }
        }

    }

    public void b() {
        EntityPlayer s = this.server.getPlayerList().attemptLogin(this, this.i, this.hostname);
        if (s != null) {
            this.g = USLoginListener.EnumProtocolState.ACCEPTED;
            if (this.server.aF() >= 0 && !this.networkManager.isLocal()) {
                this.networkManager.sendPacket(new PacketLoginOutSetCompression(this.server.aF()), new ChannelFutureListener() {
                    public void a(ChannelFuture channelfuture) throws Exception {
                        USLoginListener.this.networkManager.setCompressionLevel(USLoginListener.this.server.aF());
                    }

                    public void operationComplete(ChannelFuture future) throws Exception {
                        this.a(future);
                    }
                }, new GenericFutureListener[0]);
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
        c.info(this.d() + " lost connection: " + ichatbasecomponent.toPlainText());
    }

    public String d() {
        return this.i != null ? this.i.toString() + " (" + this.networkManager.getSocketAddress().toString() + ")" : String.valueOf(this.networkManager.getSocketAddress());
    }

    public void a(PacketLoginInStart packetlogininstart) {
        Validate.validState(this.g == USLoginListener.EnumProtocolState.HELLO, "Unexpected hello packet", new Object[0]);
        this.i = packetlogininstart.a();
        if (this.server.getOnlineMode() && !this.networkManager.isLocal()) {
            this.g = USLoginListener.EnumProtocolState.KEY;
            this.networkManager.sendPacket(new PacketLoginOutEncryptionBegin(this.j, this.server.O().getPublic(), this.e));
        } else {
            (new Thread("User Authenticator #" + b.incrementAndGet()) {
                public void run() {
                    try {
                        USLoginListener.this.initUUID();
                        (USLoginListener.this.new LoginHandler()).fireEvents();
                        USLoginListener.this.g = USLoginListener.EnumProtocolState.READY_TO_ACCEPT;
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
        PrivateKey privatekey = this.server.O().getPrivate();
        if (!Arrays.equals(this.e, packetlogininencryptionbegin.b(privatekey))) {
            throw new IllegalStateException("Invalid nonce!");
        } else {
            this.loginKey = packetlogininencryptionbegin.a(privatekey);
            this.g = USLoginListener.EnumProtocolState.AUTHENTICATING;
            this.networkManager.a(this.loginKey);
            (new Thread("User Authenticator #" + b.incrementAndGet()) {
                public void run() {
                    GameProfile gameprofile = USLoginListener.this.i;

                    try {
                        String s = (new BigInteger(MinecraftEncryption.a(USLoginListener.this.j, USLoginListener.this.server.O().getPublic(), USLoginListener.this.loginKey))).toString(16);
                        USLoginListener.this.i = USLoginListener.this.server.ay().hasJoinedServer(new GameProfile((UUID)null, gameprofile.getName()), s);
                        if (USLoginListener.this.i != null) {
                            if (!USLoginListener.this.networkManager.isConnected()) {
                                return;
                            }

                            (USLoginListener.this.new LoginHandler()).fireEvents();
                        } else if (USLoginListener.this.server.R()) {
                            USLoginListener.c.warn("Failed to verify username but will let them in anyway!");
                            USLoginListener.this.i = USLoginListener.this.a(gameprofile);
                            USLoginListener.this.g = USLoginListener.EnumProtocolState.READY_TO_ACCEPT;
                        } else {
                            USLoginListener.this.disconnect("Failed to verify username!");
                            USLoginListener.c.error("Username '" + gameprofile.getName() + "' tried to join with an invalid session");
                        }
                    } catch (AuthenticationUnavailableException var3) {
                        if (USLoginListener.this.server.R()) {
                            USLoginListener.c.warn("Authentication servers are down but will let them in anyway!");
                            USLoginListener.this.i = USLoginListener.this.a(gameprofile);
                            USLoginListener.this.g = USLoginListener.EnumProtocolState.READY_TO_ACCEPT;
                        } else {
                            USLoginListener.this.disconnect("Authentication servers are down. Please try again later, sorry!");
                            USLoginListener.c.error("Couldn't verify username because servers are unavailable");
                        }
                    } catch (Exception var4) {
                        USLoginListener.this.disconnect("Failed to verify username!");
                        USLoginListener.this.server.server.getLogger().log(Level.WARNING, "Exception verifying " + gameprofile.getName(), var4);
                    }

                }
            }).start();
        }
    }

    protected GameProfile a(GameProfile gameprofile) {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + gameprofile.getName()).getBytes(Charsets.UTF_8));
        return new GameProfile(uuid, gameprofile.getName());
    }

    static enum EnumProtocolState {
        HELLO,
        KEY,
        AUTHENTICATING,
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

            USLoginListener.c.info("UUID of player " + USLoginListener.this.i.getName() + " is " + USLoginListener.this.i.getId());
            USLoginListener.this.g = USLoginListener.EnumProtocolState.READY_TO_ACCEPT;
        }
    }
}

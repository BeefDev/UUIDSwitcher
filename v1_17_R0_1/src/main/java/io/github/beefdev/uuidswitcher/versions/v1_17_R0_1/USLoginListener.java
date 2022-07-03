package io.github.beefdev.uuidswitcher.versions.v1_17_R0_1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.properties.Property;
import io.github.beefdev.uuidswitcher.common.event.AsyncPlayerProfileCreationEvent;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.ChatMessage;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutKickDisconnect;
import net.minecraft.network.protocol.login.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.LoginListener;
import net.minecraft.util.CryptographyException;
import net.minecraft.util.MinecraftEncryption;
import net.minecraft.world.entity.player.EntityHuman;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.util.Waitable;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;

import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public final class USLoginListener extends LoginListener implements PacketLoginInListener {
    private static final AtomicInteger b = new AtomicInteger(0);
    static final Logger c = LogManager.getLogger(LoginListener.class);
    private static final int d = 600;
    private static final Random e = new Random();
    private final byte[] f = new byte[4];
    final MinecraftServer g;
    public final NetworkManager a;
    USLoginListener.EnumProtocolState h;
    private int i;
    @Nullable
    GameProfile j;
    private final String k;
    @Nullable
    private EntityPlayer l;
    public String hostname = "";

    public USLoginListener(MinecraftServer minecraftserver, NetworkManager networkmanager) {
        super(minecraftserver, networkmanager);

        this.h = USLoginListener.EnumProtocolState.a;
        this.k = "";
        this.g = minecraftserver;
        this.a = networkmanager;
        e.nextBytes(this.f);
    }

    public void tick() {
        if (this.h == USLoginListener.EnumProtocolState.e) {
            this.c();
        } else if (this.h == USLoginListener.EnumProtocolState.f) {
            EntityPlayer entityplayer = this.g.getPlayerList().getPlayer(this.j.getId());
            if (entityplayer == null) {
                this.h = USLoginListener.EnumProtocolState.e;
                this.a(this.l);
                this.l = null;
            }
        }

        if (this.i++ == 600) {
            this.disconnect((IChatBaseComponent)(new ChatMessage("multiplayer.disconnect.slow_login")));
        }

    }

    /** @deprecated */
    @Deprecated
    public void disconnect(String s) {
        try {
            IChatBaseComponent ichatbasecomponent = new ChatComponentText(s);
            c.info("Disconnecting {}: {}", this.d(), s);
            this.a.sendPacket(new PacketLoginOutDisconnect(ichatbasecomponent));
            this.a.close(ichatbasecomponent);
        } catch (Exception var3) {
            c.error("Error whilst disconnecting player", var3);
        }

    }

    public NetworkManager a() {
        return this.a;
    }

    public void disconnect(IChatBaseComponent ichatbasecomponent) {
        try {
            c.info("Disconnecting {}: {}", this.d(), ichatbasecomponent.getString());
            this.a.sendPacket(new PacketLoginOutDisconnect(ichatbasecomponent));
            this.a.close(ichatbasecomponent);
        } catch (Exception var3) {
            c.error("Error whilst disconnecting player", var3);
        }

    }

    public void initUUID() {
        UUID uuid;
        if (this.a.spoofedUUID != null) {
            uuid = this.a.spoofedUUID;
        } else {
            uuid = EntityHuman.getOfflineUUID(this.j.getName());
        }

        this.j = new GameProfile(uuid, this.j.getName());
        if (this.a.spoofedProfile != null) {
            Property[] var5;
            int var4 = (var5 = this.a.spoofedProfile).length;

            for(int var3 = 0; var3 < var4; ++var3) {
                Property property = var5[var3];
                if (USHandshakeListener.PROP_PATTERN.matcher(property.getName()).matches()) {
                    this.j.getProperties().put(property.getName(), property);
                }
            }
        }

    }

    public void c() {
        EntityPlayer s = this.g.getPlayerList().attemptLogin(this, this.j, this.hostname);
        if (s != null) {
            this.h = USLoginListener.EnumProtocolState.g;
            if (this.g.aw() >= 0 && !this.a.isLocal()) {
                this.a.sendPacket(new PacketLoginOutSetCompression(this.g.aw()), (channelfuture) -> {
                    this.a.setCompressionLevel(this.g.aw());
                });
            }

            this.a.sendPacket(new PacketLoginOutSuccess(this.j));
            EntityPlayer entityplayer = this.g.getPlayerList().getPlayer(this.j.getId());

            try {
                EntityPlayer entityplayer1 = this.g.getPlayerList().processLogin(this.j, s);
                if (entityplayer != null) {
                    this.h = USLoginListener.EnumProtocolState.f;
                    this.l = entityplayer1;
                } else {
                    this.a(entityplayer1);
                }
            } catch (Exception var5) {
                ChatMessage chatmessage = new ChatMessage("multiplayer.disconnect.invalid_player_data");
                this.a.sendPacket(new PacketPlayOutKickDisconnect(chatmessage));
                this.a.close(chatmessage);
            }
        }

    }

    private void a(EntityPlayer entityplayer) {
        this.g.getPlayerList().a(this.a, entityplayer);
    }

    public void a(IChatBaseComponent ichatbasecomponent) {
        c.info("{} lost connection: {}", this.d(), ichatbasecomponent.getString());
    }

    public String d() {
        return this.j != null ? this.j + " (" + this.a.getSocketAddress() + ")" : String.valueOf(this.a.getSocketAddress());
    }

    public void a(PacketLoginInStart packetlogininstart) {
        Validate.validState(this.h == USLoginListener.EnumProtocolState.a, "Unexpected hello packet", new Object[0]);
        this.j = packetlogininstart.b();
        if (this.g.getOnlineMode() && !this.a.isLocal()) {
            this.h = USLoginListener.EnumProtocolState.b;
            this.a.sendPacket(new PacketLoginOutEncryptionBegin("", this.g.getKeyPair().getPublic().getEncoded(), this.f));
        } else {
            (new Thread("User Authenticator #" + b.incrementAndGet()) {
                public void run() {
                    try {
                        USLoginListener.this.initUUID();
                        (USLoginListener.this.new LoginHandler()).fireEvents();
                    } catch (Exception var2) {
                        USLoginListener.this.disconnect("Failed to verify username!");
                        USLoginListener.this.g.server.getLogger().log(Level.WARNING, "Exception verifying " + USLoginListener.this.j.getName(), var2);
                    }

                }
            }).start();
        }

    }

    public void a(PacketLoginInEncryptionBegin packetlogininencryptionbegin) {
        Validate.validState(this.h == USLoginListener.EnumProtocolState.b, "Unexpected key packet", new Object[0]);
        PrivateKey privatekey = this.g.getKeyPair().getPrivate();

        final String s;
        try {
            if (!Arrays.equals(this.f, packetlogininencryptionbegin.b(privatekey))) {
                throw new IllegalStateException("Protocol error");
            }

            SecretKey secretkey = packetlogininencryptionbegin.a(privatekey);
            Cipher cipher = MinecraftEncryption.a(2, secretkey);
            Cipher cipher1 = MinecraftEncryption.a(1, secretkey);
            s = (new BigInteger(MinecraftEncryption.a("", this.g.getKeyPair().getPublic(), secretkey))).toString(16);
            this.h = USLoginListener.EnumProtocolState.c;
            this.a.a(cipher, cipher1);
        } catch (CryptographyException var7) {
            throw new IllegalStateException("Protocol error", var7);
        }

        Thread thread = new Thread("User Authenticator #" + b.incrementAndGet()) {
            public void run() {
                GameProfile gameprofile = USLoginListener.this.j;

                try {
                    USLoginListener.this.j = USLoginListener.this.g.getMinecraftSessionService().hasJoinedServer(new GameProfile((UUID)null, gameprofile.getName()), s, this.a());
                    if (USLoginListener.this.j != null) {
                        if (!USLoginListener.this.a.isConnected()) {
                            return;
                        }

                        (USLoginListener.this.new LoginHandler()).fireEvents();
                    } else if (USLoginListener.this.g.isEmbeddedServer()) {
                        USLoginListener.c.warn("Failed to verify username but will let them in anyway!");
                        USLoginListener.this.j = USLoginListener.this.a(gameprofile);
                        USLoginListener.this.h = USLoginListener.EnumProtocolState.e;
                    } else {
                        USLoginListener.this.disconnect((IChatBaseComponent)(new ChatMessage("multiplayer.disconnect.unverified_username")));
                        USLoginListener.c.error("Username '{}' tried to join with an invalid session", gameprofile.getName());
                    }
                } catch (AuthenticationUnavailableException var3) {
                    if (USLoginListener.this.g.isEmbeddedServer()) {
                        USLoginListener.c.warn("Authentication servers are down but will let them in anyway!");
                        USLoginListener.this.j = USLoginListener.this.a(gameprofile);
                        USLoginListener.this.h = USLoginListener.EnumProtocolState.e;
                    } else {
                        USLoginListener.this.disconnect((IChatBaseComponent)(new ChatMessage("multiplayer.disconnect.authservers_down")));
                        USLoginListener.c.error("Couldn't verify username because servers are unavailable");
                    }
                } catch (Exception var4) {
                    USLoginListener.this.disconnect("Failed to verify username!");
                    USLoginListener.this.g.server.getLogger().log(Level.WARNING, "Exception verifying " + gameprofile.getName(), var4);
                }

            }

            private @Nullable InetAddress a() {
                SocketAddress socketaddress = USLoginListener.this.a.getSocketAddress();
                return USLoginListener.this.g.X() && socketaddress instanceof InetSocketAddress ? ((InetSocketAddress)socketaddress).getAddress() : null;
            }
        };
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(c));
        thread.start();
    }

    public void a(PacketLoginInCustomPayload packetloginincustompayload) {
        this.disconnect((IChatBaseComponent)(new ChatMessage("multiplayer.disconnect.unexpected_query_response")));
    }

    protected GameProfile a(GameProfile gameprofile) {
        UUID uuid = EntityHuman.getOfflineUUID(gameprofile.getName());
        return new GameProfile(uuid, gameprofile.getName());
    }

    private static enum EnumProtocolState {
        a,
        b,
        c,
        d,
        e,
        f,
        g;

        private EnumProtocolState() {
        }
    }

    public class LoginHandler {
        public LoginHandler() {
        }

        public void fireEvents() throws Exception {
            String playerName = USLoginListener.this.j.getName();
            InetAddress address = ((InetSocketAddress) USLoginListener.this.a.getSocketAddress()).getAddress();
            UUID uniqueId = USLoginListener.this.j.getId();

            final CraftServer server = USLoginListener.this.g.server;

            AsyncPlayerProfileCreationEvent asyncProfileCreationEvent = new AsyncPlayerProfileCreationEvent(address, playerName, uniqueId);
            server.getPluginManager().callEvent(asyncProfileCreationEvent);

            uniqueId = asyncProfileCreationEvent.getUUID();
            playerName = asyncProfileCreationEvent.getName();

            GameProfile gameProfile = new GameProfile(uniqueId, playerName);
            gameProfile.getProperties().clear();
            for(Map.Entry<String, Property> entry : USLoginListener.this.j.getProperties().entries()) {
                gameProfile.getProperties().put(entry.getKey(), entry.getValue());
            }

            USLoginListener.this.j = gameProfile;

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
                USLoginListener.this.g.processQueue.add(waitable);
                if (waitable.get() != PlayerPreLoginEvent.Result.ALLOWED) {
                    USLoginListener.this.disconnect(event.getKickMessage());
                    return;
                }
            } else if (asyncEvent.getLoginResult() != org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.ALLOWED) {
                USLoginListener.this.disconnect(asyncEvent.getKickMessage());
                return;
            }

            USLoginListener.c.info("UUID of player {} is {}", USLoginListener.this.j.getName(), USLoginListener.this.j.getId());
            USLoginListener.this.h = USLoginListener.EnumProtocolState.e;
        }
    }
}
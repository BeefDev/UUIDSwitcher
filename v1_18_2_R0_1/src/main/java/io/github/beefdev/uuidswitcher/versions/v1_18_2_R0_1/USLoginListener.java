package io.github.beefdev.uuidswitcher.versions.v1_18_2_R0_1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.properties.Property;
import com.mojang.logging.LogUtils;
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
import org.apache.commons.lang3.Validate;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.util.Waitable;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    static final Logger c = LoggerFactory.getLogger(LoginListener.class);
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

    public void c() {
        if (this.h == USLoginListener.EnumProtocolState.e) {
            this.d();
        } else if (this.h == USLoginListener.EnumProtocolState.f) {
            EntityPlayer entityplayer = this.g.ac().a(this.j.getId());
            if (entityplayer == null) {
                this.h = USLoginListener.EnumProtocolState.e;
                this.a(this.l);
                this.l = null;
            }
        }

        if (this.i++ == 600) {
            this.b(new ChatMessage("multiplayer.disconnect.slow_login"));
        }

    }

    /** @deprecated */
    @Deprecated
    public void disconnect(String s) {
        try {
            IChatBaseComponent ichatbasecomponent = new ChatComponentText(s);
            c.info("Disconnecting {}: {}", this.e(), s);
            this.a.a(new PacketLoginOutDisconnect(ichatbasecomponent));
            this.a.a(ichatbasecomponent);
        } catch (Exception var3) {
            c.error("Error whilst disconnecting player", var3);
        }

    }

    public NetworkManager a() {
        return this.a;
    }

    public void b(IChatBaseComponent ichatbasecomponent) {
        try {
            c.info("Disconnecting {}: {}", this.e(), ichatbasecomponent.getString());
            this.a.a(new PacketLoginOutDisconnect(ichatbasecomponent));
            this.a.a(ichatbasecomponent);
        } catch (Exception var3) {
            c.error("Error whilst disconnecting player", var3);
        }

    }

    public void initUUID() {
        UUID uuid;
        if (this.a.spoofedUUID != null) {
            uuid = this.a.spoofedUUID;
        } else {
            uuid = EntityHuman.c(this.j.getName());
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

    public void d() {
        EntityPlayer s = this.g.ac().canPlayerLogin(this, this.j, this.hostname);
        if (s != null) {
            this.h = USLoginListener.EnumProtocolState.g;
            if (this.g.au() >= 0 && !this.a.d()) {
                this.a.a(new PacketLoginOutSetCompression(this.g.au()), (channelfuture) -> {
                    this.a.a(this.g.au(), true);
                });
            }

            this.a.a(new PacketLoginOutSuccess(this.j));
            EntityPlayer entityplayer = this.g.ac().a(this.j.getId());

            try {
                EntityPlayer entityplayer1 = this.g.ac().getPlayerForLogin(this.j, s);
                if (entityplayer != null) {
                    this.h = USLoginListener.EnumProtocolState.f;
                    this.l = entityplayer1;
                } else {
                    this.a(entityplayer1);
                }
            } catch (Exception var5) {
                c.error("Couldn't place player in world", var5);
                ChatMessage chatmessage = new ChatMessage("multiplayer.disconnect.invalid_player_data");
                this.a.a(new PacketPlayOutKickDisconnect(chatmessage));
                this.a.a(chatmessage);
            }
        }

    }

    private void a(EntityPlayer entityplayer) {
        this.g.ac().a(this.a, entityplayer);
    }

    public void a(IChatBaseComponent ichatbasecomponent) {
        c.info("{} lost connection: {}", this.e(), ichatbasecomponent.getString());
    }

    public String e() {
        return this.j != null ? this.j + " (" + this.a.c() + ")" : String.valueOf(this.a.c());
    }

    public void a(PacketLoginInStart packetlogininstart) {
        Validate.validState(this.h == USLoginListener.EnumProtocolState.a, "Unexpected hello packet", new Object[0]);
        this.j = packetlogininstart.b();
        Validate.validState(a(this.j.getName()), "Invalid characters in username", new Object[0]);
        if (this.g.U() && !this.a.d()) {
            this.h = USLoginListener.EnumProtocolState.b;
            this.a.a(new PacketLoginOutEncryptionBegin("", this.g.L().getPublic().getEncoded(), this.f));
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

    public static boolean a(String s) {
        return s.chars().filter((i) -> {
            return i <= 32 || i >= 127;
        }).findAny().isEmpty();
    }

    public void a(PacketLoginInEncryptionBegin packetlogininencryptionbegin) {
        Validate.validState(this.h == USLoginListener.EnumProtocolState.b, "Unexpected key packet", new Object[0]);
        PrivateKey privatekey = this.g.L().getPrivate();

        final String s;
        try {
            if (!Arrays.equals(this.f, packetlogininencryptionbegin.b(privatekey))) {
                throw new IllegalStateException("Protocol error");
            }

            SecretKey secretkey = packetlogininencryptionbegin.a(privatekey);
            Cipher cipher = MinecraftEncryption.a(2, secretkey);
            Cipher cipher1 = MinecraftEncryption.a(1, secretkey);
            s = (new BigInteger(MinecraftEncryption.a("", this.g.L().getPublic(), secretkey))).toString(16);
            this.h = USLoginListener.EnumProtocolState.c;
            this.a.a(cipher, cipher1);
        } catch (CryptographyException var7) {
            throw new IllegalStateException("Protocol error", var7);
        }

        Thread thread = new Thread("User Authenticator #" + b.incrementAndGet()) {
            public void run() {
                GameProfile gameprofile = USLoginListener.this.j;

                try {
                    USLoginListener.this.j = USLoginListener.this.g.am().hasJoinedServer(new GameProfile((UUID)null, gameprofile.getName()), s, this.getAddress());
                    if (USLoginListener.this.j != null) {
                        if (!USLoginListener.this.a.h()) {
                            return;
                        }

                        (USLoginListener.this.new LoginHandler()).fireEvents();
                    } else if (USLoginListener.this.g.O()) {
                        USLoginListener.c.warn("Failed to verify username but will let them in anyway!");
                        USLoginListener.this.j = USLoginListener.this.a(gameprofile);
                        USLoginListener.this.h = USLoginListener.EnumProtocolState.e;
                    } else {
                        USLoginListener.this.b(new ChatMessage("multiplayer.disconnect.unverified_username"));
                        USLoginListener.c.error("Username '{}' tried to join with an invalid session", gameprofile.getName());
                    }
                } catch (AuthenticationUnavailableException var3) {
                    if (USLoginListener.this.g.O()) {
                        USLoginListener.c.warn("Authentication servers are down but will let them in anyway!");
                        USLoginListener.this.j = USLoginListener.this.a(gameprofile);
                        USLoginListener.this.h = USLoginListener.EnumProtocolState.e;
                    } else {
                        USLoginListener.this.b(new ChatMessage("multiplayer.disconnect.authservers_down"));
                        USLoginListener.c.error("Couldn't verify username because servers are unavailable");
                    }
                } catch (Exception var4) {
                    USLoginListener.this.disconnect("Failed to verify username!");
                    USLoginListener.this.g.server.getLogger().log(Level.WARNING, "Exception verifying " + gameprofile.getName(), var4);
                }

            }

            private @Nullable InetAddress getAddress() {
                SocketAddress socketaddress = USLoginListener.this.a.c();
                return USLoginListener.this.g.V() && socketaddress instanceof InetSocketAddress ? ((InetSocketAddress)socketaddress).getAddress() : null;
            }
        };
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(c));
        thread.start();
    }

    public void a(PacketLoginInCustomPayload packetloginincustompayload) {
        this.b(new ChatMessage("multiplayer.disconnect.unexpected_query_response"));
    }

    protected GameProfile a(GameProfile gameprofile) {
        UUID uuid = EntityHuman.c(gameprofile.getName());
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
            InetAddress address = ((InetSocketAddress) USLoginListener.this.a.c()).getAddress();
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

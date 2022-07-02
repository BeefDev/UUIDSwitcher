package io.github.beefdev.uuidswitcher.versions.v1_19_R0_1;

import com.google.common.primitives.Ints;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.properties.Property;
import com.mojang.logging.LogUtils;
import io.github.beefdev.uuidswitcher.common.event.AsyncPlayerProfileCreationEvent;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.chat.ThrowingComponent;
import net.minecraft.network.protocol.game.PacketPlayOutKickDisconnect;
import net.minecraft.network.protocol.login.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.LoginListener;
import net.minecraft.util.CryptographyException;
import net.minecraft.util.MinecraftEncryption;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.apache.commons.lang3.Validate;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.util.Waitable;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public final class USLoginListener extends LoginListener implements PacketLoginInListener {
    private static final AtomicInteger b = new AtomicInteger(0);
    static final Logger c = LogUtils.getLogger();
    private static final int d = 600;
    private static final RandomSource e = RandomSource.a();
    private static final IChatBaseComponent f = IChatBaseComponent.c("multiplayer.disconnect.missing_public_key");
    private static final IChatBaseComponent g = IChatBaseComponent.c("multiplayer.disconnect.invalid_public_key_signature");
    private static final IChatBaseComponent h = IChatBaseComponent.c("multiplayer.disconnect.invalid_public_key");
    private final byte[] i;
    final MinecraftServer j;
    public final NetworkManager a;
    USLoginListener.EnumProtocolState k;
    private int l;
    @Nullable
    GameProfile m;
    private final String n;
    @Nullable
    private EntityPlayer o;
    @Nullable
    private ProfilePublicKey p;
    public String hostname = "";

    public USLoginListener(MinecraftServer minecraftserver, NetworkManager networkmanager) {
        super(minecraftserver, networkmanager);

        this.k = USLoginListener.EnumProtocolState.a;
        this.n = "";
        this.j = minecraftserver;
        this.a = networkmanager;
        this.i = Ints.toByteArray(e.f());
    }

    public void c() {
        if (this.k == USLoginListener.EnumProtocolState.e) {
            this.d();
        } else if (this.k == USLoginListener.EnumProtocolState.f) {
            EntityPlayer entityplayer = this.j.ac().a(this.m.getId());
            if (entityplayer == null) {
                this.k = USLoginListener.EnumProtocolState.e;
                this.a(this.o);
                this.o = null;
            }
        }

        if (this.l++ == 600) {
            this.b(IChatBaseComponent.c("multiplayer.disconnect.slow_login"));
        }

    }

    /** @deprecated */
    @Deprecated
    public void disconnect(String s) {
        this.b(IChatBaseComponent.b(s));
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
            uuid = UUIDUtil.a(this.m.getName());
        }

        this.m = new GameProfile(uuid, this.m.getName());
        if (this.a.spoofedProfile != null) {
            Property[] var5;
            int var4 = (var5 = this.a.spoofedProfile).length;

            for(int var3 = 0; var3 < var4; ++var3) {
                Property property = var5[var3];
                if (USHandshakeListener.PROP_PATTERN.matcher(property.getName()).matches()) {
                    this.m.getProperties().put(property.getName(), property);
                }
            }
        }

    }

    public void d() {
        EntityPlayer s = this.j.ac().canPlayerLogin(this, this.m, this.p, this.hostname);
        if (s != null) {
            this.k = USLoginListener.EnumProtocolState.g;
            if (this.j.av() >= 0 && !this.a.d()) {
                this.a.a(new PacketLoginOutSetCompression(this.j.av()), (channelfuture) -> {
                    this.a.a(this.j.av(), true);
                });
            }

            this.a.a(new PacketLoginOutSuccess(this.m));
            EntityPlayer entityplayer = this.j.ac().a(this.m.getId());

            try {
                EntityPlayer entityplayer1 = this.j.ac().getPlayerForLogin(this.m, s);
                if (entityplayer != null) {
                    this.k = USLoginListener.EnumProtocolState.f;
                    this.o = entityplayer1;
                } else {
                    this.a(entityplayer1);
                }
            } catch (Exception var5) {
                c.error("Couldn't place player in world", var5);
                IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.c("multiplayer.disconnect.invalid_player_data");
                this.a.a(new PacketPlayOutKickDisconnect(ichatmutablecomponent));
                this.a.a(ichatmutablecomponent);
            }
        }

    }

    private void a(EntityPlayer entityplayer) {
        this.j.ac().a(this.a, entityplayer);
    }

    public void a(IChatBaseComponent ichatbasecomponent) {
        c.info("{} lost connection: {}", this.e(), ichatbasecomponent.getString());
    }

    public String e() {
        return this.m != null ? this.m + " (" + this.a.c() + ")" : String.valueOf(this.a.c());
    }

    private static @Nullable ProfilePublicKey a(PacketLoginInStart packetlogininstart, SignatureValidator signaturevalidator, boolean flag) throws USLoginListener.a {
        try {
            Optional<ProfilePublicKey.a> optional = packetlogininstart.c();
            if (optional.isEmpty()) {
                if (flag) {
                    throw new a(f);
                } else {
                    return null;
                }
            } else {
                return ProfilePublicKey.a(signaturevalidator, (ProfilePublicKey.a)optional.get());
            }
        } catch (InsecurePublicKeyException.MissingException var4) {
            if (flag) {
                throw new a(g, var4);
            } else {
                return null;
            }
        } catch (CryptographyException var5) {
            throw new a(h, var5);
        } catch (Exception var6) {
            throw new a(g, var6);
        }
    }

    public void a(PacketLoginInStart packetlogininstart) {
        Validate.validState(this.k == USLoginListener.EnumProtocolState.a, "Unexpected hello packet", new Object[0]);
        Validate.validState(a(packetlogininstart.b()), "Invalid characters in username", new Object[0]);

        try {
            this.p = a(packetlogininstart, this.j.an(), this.j.aw());
        } catch (USLoginListener.a var3) {
            c.error(var3.getMessage(), var3.getCause());
            if (!this.a.d()) {
                this.b(var3.a());
                return;
            }
        }

        GameProfile gameprofile = this.j.M();
        if (gameprofile != null && packetlogininstart.b().equalsIgnoreCase(gameprofile.getName())) {
            this.m = gameprofile;
            this.k = USLoginListener.EnumProtocolState.e;
        } else {
            this.m = new GameProfile((UUID)null, packetlogininstart.b());
            if (this.j.T() && !this.a.d()) {
                this.k = USLoginListener.EnumProtocolState.b;
                this.a.a(new PacketLoginOutEncryptionBegin("", this.j.K().getPublic().getEncoded(), this.i));
            } else {
                (new Thread("User Authenticator #" + b.incrementAndGet()) {
                    public void run() {
                        try {
                            USLoginListener.this.initUUID();
                            (USLoginListener.this.new LoginHandler()).fireEvents();
                        } catch (Exception var2) {
                            USLoginListener.this.disconnect("Failed to verify username!");
                            USLoginListener.this.j.server.getLogger().log(Level.WARNING, "Exception verifying " + USLoginListener.this.m.getName(), var2);
                        }

                    }
                }).start();
            }
        }

    }

    public static boolean a(String s) {
        return s.chars().filter((i) -> {
            return i <= 32 || i >= 127;
        }).findAny().isEmpty();
    }

    public void a(PacketLoginInEncryptionBegin packetlogininencryptionbegin) {
        Validate.validState(this.k == USLoginListener.EnumProtocolState.b, "Unexpected key packet", new Object[0]);

        final String s;
        try {
            PrivateKey privatekey = this.j.K().getPrivate();
            if (this.p != null) {
                if (!packetlogininencryptionbegin.a(this.i, this.p)) {
                    throw new IllegalStateException("Protocol error");
                }
            } else if (!packetlogininencryptionbegin.a(this.i, privatekey)) {
                throw new IllegalStateException("Protocol error");
            }

            SecretKey secretkey = packetlogininencryptionbegin.a(privatekey);
            Cipher cipher = MinecraftEncryption.a(2, secretkey);
            Cipher cipher1 = MinecraftEncryption.a(1, secretkey);
            s = (new BigInteger(MinecraftEncryption.a("", this.j.K().getPublic(), secretkey))).toString(16);
            this.k = USLoginListener.EnumProtocolState.c;
            this.a.a(cipher, cipher1);
        } catch (CryptographyException var7) {
            throw new IllegalStateException("Protocol error", var7);
        }

        Thread thread = new Thread("User Authenticator #" + b.incrementAndGet()) {
            public void run() {
                GameProfile gameprofile = USLoginListener.this.m;

                try {
                    USLoginListener.this.m = USLoginListener.this.j.am().hasJoinedServer(new GameProfile((UUID)null, gameprofile.getName()), s, this.getAddress());
                    if (USLoginListener.this.m != null) {
                        if (!USLoginListener.this.a.h()) {
                            return;
                        }

                        (USLoginListener.this.new LoginHandler()).fireEvents();
                    } else if (USLoginListener.this.j.N()) {
                        USLoginListener.c.warn("Failed to verify username but will let them in anyway!");
                        USLoginListener.this.m = USLoginListener.this.a(gameprofile);
                        USLoginListener.this.k = USLoginListener.EnumProtocolState.e;
                    } else {
                        USLoginListener.this.b(IChatBaseComponent.c("multiplayer.disconnect.unverified_username"));
                        USLoginListener.c.error("Username '{}' tried to join with an invalid session", gameprofile.getName());
                    }
                } catch (AuthenticationUnavailableException var3) {
                    if (USLoginListener.this.j.N()) {
                        USLoginListener.c.warn("Authentication servers are down but will let them in anyway!");
                        USLoginListener.this.m = USLoginListener.this.a(gameprofile);
                        USLoginListener.this.k = USLoginListener.EnumProtocolState.e;
                    } else {
                        USLoginListener.this.b(IChatBaseComponent.c("multiplayer.disconnect.authservers_down"));
                        USLoginListener.c.error("Couldn't verify username because servers are unavailable");
                    }
                } catch (Exception var4) {
                    USLoginListener.this.disconnect("Failed to verify username!");
                    USLoginListener.this.j.server.getLogger().log(Level.WARNING, "Exception verifying " + gameprofile.getName(), var4);
                }

            }

            private @Nullable InetAddress getAddress() {
                SocketAddress socketaddress = USLoginListener.this.a.c();
                return USLoginListener.this.j.U() && socketaddress instanceof InetSocketAddress ? ((InetSocketAddress)socketaddress).getAddress() : null;
            }
        };
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(c));
        thread.start();
    }

    public void a(PacketLoginInCustomPayload packetloginincustompayload) {
        this.b(IChatBaseComponent.c("multiplayer.disconnect.unexpected_query_response"));
    }

    protected GameProfile a(GameProfile gameprofile) {
        UUID uuid = UUIDUtil.a(gameprofile.getName());
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
            String playerName = USLoginListener.this.m.getName();
            InetAddress address = ((InetSocketAddress) USLoginListener.this.a.c()).getAddress();
            UUID uniqueId = USLoginListener.this.m.getId();

            final CraftServer server = USLoginListener.this.j.server;

            AsyncPlayerProfileCreationEvent asyncProfileCreationEvent = new AsyncPlayerProfileCreationEvent(address, playerName, uniqueId);
            server.getPluginManager().callEvent(asyncProfileCreationEvent);

            uniqueId = asyncProfileCreationEvent.getUUID();
            playerName = asyncProfileCreationEvent.getName();

            GameProfile gameProfile = new GameProfile(uniqueId, playerName);
            gameProfile.getProperties().clear();
            for(Map.Entry<String, Property> entry : USLoginListener.this.m.getProperties().entries()) {
                gameProfile.getProperties().put(entry.getKey(), entry.getValue());
            }

            USLoginListener.this.m = gameProfile;

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
                USLoginListener.this.j.processQueue.add(waitable);
                if (waitable.get() != PlayerPreLoginEvent.Result.ALLOWED) {
                    USLoginListener.this.disconnect(event.getKickMessage());
                    return;
                }
            } else if (asyncEvent.getLoginResult() != org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.ALLOWED) {
                USLoginListener.this.disconnect(asyncEvent.getKickMessage());
                return;
            }

            USLoginListener.c.info("UUID of player {} is {}", USLoginListener.this.m.getName(), USLoginListener.this.m.getId());
            USLoginListener.this.k = USLoginListener.EnumProtocolState.e;
        }
    }

    private static class a extends ThrowingComponent {
        public a(IChatBaseComponent ichatbasecomponent) {
            super(ichatbasecomponent);
        }

        public a(IChatBaseComponent ichatbasecomponent, Throwable throwable) {
            super(ichatbasecomponent, throwable);
        }
    }
}

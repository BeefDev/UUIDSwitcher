# UUIDSwitcher

An easy to use but powerful API for minecraft servers which gives developers control over the UUID, name, and properties/textures a player logs in with. This changes the result of Player#getUniqueId, Player#getName, GameProfile#getProperties effectively giving the player a "new minecraft account" for that specific server, as plugins and minecraft itself don't connect them to the previous UUID & name in any way. Using this API players can connect as brand-new accounts, or connect to the server as another player.

# API Usage

Originally the project used <a href="https://jitpack.io">Jitpack</a>, however due to a couple issues with it, we have migrated over to <a href="https://codemc.io/">CodeMC</a>.
<br><br>
<details>
    <summary>Importing the API using maven</summary>

First, add the CodeMC repository to your repositories if you haven't already.
```xml
<repository>
    <id>codemc-repo</id>
    <url>https://repo.codemc.org/repository/maven-public/</url>
</repository>
```
Then add the UUIDSwitcher dependency to your project
```xml
<dependency>
    <groupId>io.github.beefdev.uuidswitcher</groupId>
    <artifactId>core</artifactId>
    <version>VERSION</version>
    <scope>compile</scope>
</dependency>
```
I highly recommend relocating the dependency using the <a href="https://maven.apache.org/plugins/maven-shade-plugin/">maven shade plugin</a>, as failure to do that will likely cause conflicts with other plugins using the API.
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.3.0</version>
            <executions>
                <execution>
                    <id>shade</id>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <relocations>
                    <relocation>
                        <pattern>io.github.beefdev.uuidswitcher</pattern>
                        <shadedPattern>YOUR PACKAGE HERE</shadedPattern>
                    </relocation>
                </relocations>
            </configuration>
        </plugin>
    </plugins>
</build>
```
</details>

# Documentation
Using UUIDSwitcher is very simple, here is a short explanation of how to use it
<br>
<br>
To use the API, you must enable it using `UUIDSwitcher.onEnable`. It is also a good practice to disable it inside your onDisable method using `UUIDSwitcher.onDisable`. 
```java
public final class MyPlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        UUIDSwitcher.onEnable();
    }
    
    @Override
    public void onDisable() {
        UUIDSwitcher.onDisable();
    }
}
```
If no exceptions are thrown upon calling `UUIDSwitcher.onEnable`, the API will be enabled and ready for you to use!
Whenever a player logs in an event called `AsyncPlayerProfileCreationEvent` is fired. It is important to note that this event is fired before the PlayerPreLogin events & every other spigot event, thus you might experience errors trying to load information from other plugin APIs inside this events listener.
<br><br>
You can listen to the event and modify its properties as shown below.

```java

public final class MyListener implements Listener {
    @EventHandler
    public void onProfileCreation(AsyncPlayerProfileCreationEvent event) {
        UUID originalUUID = event.getGameProfile().getUuid;
        String originalName = event.getGameProfile().getName();
        WrappedSignedPropertyMap originalProperties = event.getGameProfile().getProperties();

        WrappedGameProfile newProfile = new WrappedGameProfile(myNewUUID, "myNewName", new WrappedSignedPropertyMap());
        event.setGameProfile(newProfile);
        
        event.getAddress().ifPresent(address -> {});
    }
}
```
Let's go over it step by step. The event has a WrappedGameProfile attached to it, you can get that by using `AsyncPlayerProfileCreationEvent#getGameProfile`, you can later set that profile to change the profile the player logs in with using `AsyncPlayerProfileCreationEvent#setGameProfile(WrappedGameProfile)`.
<ul>
    <li>WrappedGameProfile#getUuid returns the UUID attached to this game profile</li>
    <li>WrappedGameProfile#getName returns the name attached to this game profile</li>
    <li>WrappedGameProfile#getProperties returns a WrappedSignedPropertyMap containing all the properties (mostly used for textures/skins) in the form of a WrappedSignedProperty.</li>
    <ul>
        <li>WrappedSignedPropertyMap can be used exactly like the Mojang PropertyMap</li>
        <li>WrappedSignedProperty can be used exactly like a mojang Property</li>
    </ul>
</ul>
The only other field of the event is an InetAddress representing the players IP address, however that is not provided in versions prior to 1.11 so the method getAddress returns an Optional instead.

# How it works
To understand how UUIDSwitcher works you need to first have a basic understand of how login is handled inside the minecraft server code.

<ul>
    <li>Server - Client connection initialized</li>
    <ul>
        <li>Servers assigns a HandshakeListener to the players NetworkManager</li>
    </ul>
    <li>Client sends a handshake packet with an id of 2 (LOGIN)</li>
    <ul>
        <li>Servers checks the id of the packet and assigns a LoginListener to the players NetworkManager</li>
    </ul>
    <li>...</li>
    <li>Client sends the encryption start packet</li>
    <ul>
        <li>The server starts a new thread for authentication</li>
        <ul>
            <li><b>The LoginListener gets a field of the MinecraftServer, the MinecraftSessionService and calls MinecraftSessionService#hasJoinedServer. This takes in a GameProfile without a UUID, does its magic and returns the actual profile. </b></li>
            <li>After the profile is retrieved PreLogin events and other stuff such as adding the player to the server happen.</li>
        </ul>
    </ul>
</ul>

Previously UUIDSwitcher extended LoginListener and spoofed the GameProfile before the PreLogin events were fired. This however required a rewrite for every minor version of minecraft and was incompatible with other platforms.
<br><br>
However the new method is much more efficient, implementing the MinecraftSessionService interface and using the default minecraft one as a delegate, only really changing the result of the hasJoinedServer method to replace the GameProfile. That delegator is later set as the new MinecraftSessionService inside MinecraftServer using some black magic (sun.misc.Unsafe). Whats more, the new method will likely eliminate any (possible) performance issues the previous one had.
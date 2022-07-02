# UUIDSwitcher
An easy to use but powerful API for spigot servers which gives developers control over the UUID and name a player logs in with. This changes the result of Player#getUniqueId and Player#getName effectively giving the player a "new minecraft account" for that specific server, as plugins and minecraft itself don't connect them to the previous uuid in any way. Using this API players can connect as brand-new accounts, or connect to the server as a different players account.

# Documentation
Using UUIDSwitcher is extremely simple and can work in all minecraft versions 1.8 - 1.19 without any extra work from the developer.

To enable UUIDSwitcher (typically in the onEnable method of your plugin) simply call `UUIDSwitcher.onEnable();`

After it is enabled whenever a player logs in a <b>PlayerProfileCreationEvent</b> event will be fired
```java
@EventHandler
public void onLogin(PlayerProfileCreationEvent event) {
    event.setName(myName);
    event.setUUID(myUUID);
}
```
As shown above you can set the name and uuid a player will have through a listener and UUIDSwitcher will handle the rest.

# How it works
When the onEnable method is called, UUIDSwitcher gets the nms MinecraftServer instance, finds the ServerConnection object, and replaces the connection list (client list) with a custom list wrapper. Whenever a connection is added to that list the channel initializer is swapped with a custom one. That initializer still runs the previous one (to ensure login works correctly and to be compatible with other plugins using a similar method eg: ViaVersion). 
<br>
<br>
Normally the Minecraft initializer would assign a HandshakeListener to the clients NetworkManager to accept a handshake packet (that is what starts the login process). However, UUIDSwitcher replaces that with a custom HandshakeListener. This has been coded and tested separately for every spigot version to ensure compatibility, it works exactly like the normal HandshakeListener, but when the handshake packet to start the login process is received, it sets the packet listener to a custom LoginListener rather than the vanilla one. That listener also works exactly like the vanilla one, but it overrides the method called after login is complete, to fire the event and change the obtained GameProfile before calling the normal method to add the player to the server.

# API Usage
To use UUIDSwitcher in your own project, simply import it through maven. 
<br>
First add the Jitpack repository.
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
Then depend on the core module (you may depend on other modules but they are unlikely to be of any use to you)
```xml
<dependency>
    <groupId>com.github.BeefDev.UUIDSwitcher</groupId>
    <artifactId>core</artifactId>
    <version>{VERSION}</version>
    <scope>compile</scope>
</dependency>
```

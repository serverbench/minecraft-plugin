package io.serverbench.client.spigot;

import io.serverbench.client.common.ConnectionManager;
import io.serverbench.client.common.IdleProvider;
import io.serverbench.client.common.strayWorker.ConnectedMember;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;

public class ConnectionListener implements org.bukkit.event.Listener {

    boolean slave;
    Plugin plugin;
    @Nullable
    IdleProvider idleProvider;

    ConnectionListener(boolean slave, Plugin plugin, @Nullable IdleProvider idleProvider) {
        this.slave = slave;
        this.plugin = plugin;
        this.idleProvider = idleProvider;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        ConnectionManager.getInstance().registerHostname(
                event.getPlayer().getUniqueId(),
                event.getHostname()
        );
    }

    @EventHandler()
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(slave) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ConnectionManager.getInstance().openConnection(
                    event.getPlayer().getUniqueId(),
                    event.getPlayer().getName(),
                    "minecraft/java",
                    null,
                    event.getPlayer().getAddress(),
                    idleProvider != null && idleProvider.isIdle(event.getPlayer().getUniqueId())
            );
        });
    }

    @EventHandler()
    public void onPlayerQuit(PlayerQuitEvent event) {
        ConnectedMember connectedMember = ConnectionManager.getInstance().getConnected().get(event.getPlayer().getUniqueId());
        if(connectedMember != null) {
            ConnectionManager.getInstance().unsetHostname(connectedMember.id());
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                ConnectionManager.getInstance().closeConnection(
                        connectedMember.id(),
                        connectedMember.name(),
                        connectedMember.platform()
                );
            });
        }
    }
}

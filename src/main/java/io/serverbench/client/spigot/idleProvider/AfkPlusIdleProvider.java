package io.serverbench.client.spigot.idleProvider;

import com.cjcrafter.foliascheduler.ServerImplementation;
import io.serverbench.client.common.IdleProvider;
import io.serverbench.client.common.ProxyMessaging;
import net.lapismc.afkplus.AFKPlus;
import net.lapismc.afkplus.api.AFKStartEvent;
import net.lapismc.afkplus.api.AFKStopEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class AfkPlusIdleProvider extends IdleProvider implements Listener {

    Plugin plugin;
    AFKPlus afkPlus;
    ServerImplementation serverImpl;

    public AfkPlusIdleProvider(Plugin plugin, boolean slave, ProxyMessaging proxyMessaging, ServerImplementation serverImpl) {
        super(slave, proxyMessaging);
        this.plugin = plugin;
        this.afkPlus = (AFKPlus) AFKPlus.getInstance();
        this.serverImpl = serverImpl;
    }

    @EventHandler
    public void onAfk(AFKStartEvent event) {
        Player player = plugin.getServer().getPlayer(event.getPlayer().getUUID());
        if(player == null) return;
        serverImpl.async().runNow(() -> setIdle(
                true,
                player.getUniqueId(),
                player.getName(),
                "minecraft/java",
                player.getAddress()
        ));
    }

    @EventHandler
    public void onStopAfk(AFKStopEvent event) {
        Player player = plugin.getServer().getPlayer(event.getPlayer().getUUID());
        if(player == null) return;
        serverImpl.async().runNow(() -> setIdle(
                false,
                player.getUniqueId(),
                player.getName(),
                "minecraft/java",
                player.getAddress()
        ));
    }

    @Override
    public boolean isIdle(UUID id) {
        return afkPlus.getPlayer(id).isAFK();
    }

    @Override
    public void register() {
        plugin.getLogger().info("Using AFKPlus as the idle/AFK provider");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}

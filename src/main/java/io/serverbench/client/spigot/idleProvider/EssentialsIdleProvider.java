package io.serverbench.client.spigot.idleProvider;

import com.earth2me.essentials.Essentials;
import io.serverbench.client.common.ConnectionManager;
import io.serverbench.client.common.IdleProvider;
import io.serverbench.client.common.ProxyMessaging;
import net.ess3.api.events.AfkStatusChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class EssentialsIdleProvider extends IdleProvider implements Listener {

    private final Plugin plugin;
    private final Essentials ess;

    public EssentialsIdleProvider(Plugin plugin, boolean slave, ProxyMessaging proxyMessaging) {
        super(slave, proxyMessaging);
        this.plugin = plugin;
        this.ess = (Essentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
    }

    @EventHandler
    public void onIdleChange(AfkStatusChangeEvent event) {
        Player player = event.getAffected().getBase();
        boolean idle = event.getValue();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()-> setIdle(
                idle,
                player.getUniqueId(),
                player.getName(),
                "minecraft/java",
                player.getAddress()
        ));
    }

    @Override
    public boolean isIdle(UUID id) {
        return this.ess.getUser(id).isAfk();
    }

    @Override
    public void register() {
        plugin.getLogger().info("Using Essentials as the idle/AFK provider");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}

package io.serverbench.client.spigot;

import com.vexsoftware.votifier.model.VotifierEvent;
import io.serverbench.client.common.VoteManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class VoteListener implements Listener {

    Plugin plugin;

    VoteListener(Plugin plugin) {
        this.plugin = plugin;
        this.plugin.getLogger().info("created NuVotifier listener");
    }

    @EventHandler
    public void onVote(VotifierEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> VoteManager.forwardVote(event.getVote())
            .then((e)-> plugin.getLogger().info("forwarded vote"))
            .capture((e)-> plugin.getLogger().warning("error while forwarding vote: " + e.getMessage()))
            .send());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        VoteManager.getInstance().removeCache(event.getPlayer().getUniqueId());
    }

}

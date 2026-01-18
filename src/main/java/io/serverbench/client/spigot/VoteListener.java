package io.serverbench.client.spigot;

import com.cjcrafter.foliascheduler.ServerImplementation;
import com.vexsoftware.votifier.model.VotifierEvent;
import io.serverbench.client.common.VoteManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class VoteListener implements Listener {

    Plugin plugin;
    ServerImplementation serverImpl;

    VoteListener(Plugin plugin, ServerImplementation serverImpl) {
        this.plugin = plugin;
        this.plugin.getLogger().info("created NuVotifier listener");
        this.serverImpl = serverImpl;
    }

    @EventHandler
    public void onVote(VotifierEvent event) {
        serverImpl.async().runNow(() -> VoteManager.forwardVote(event.getVote())
            .then((e)-> plugin.getLogger().info("forwarded vote"))
            .capture((e)-> plugin.getLogger().warning("error while forwarding vote: " + e.getMessage()))
            .send());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        VoteManager.getInstance().removeCache(event.getPlayer().getUniqueId());
    }

}

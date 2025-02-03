package io.serverbench.client.bungee;

import com.vexsoftware.votifier.bungee.events.VotifierEvent;
import io.serverbench.client.common.VoteManager;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class VoteListener implements Listener {

    Plugin plugin;

    VoteListener(Plugin plugin){
        this.plugin = plugin;
        this.plugin.getLogger().info("created NuVotifier listener");
    }

    @EventHandler
    public void onVote(VotifierEvent event) {
        VoteManager.forwardVote(event.getVote()).then((e)->{
            plugin.getLogger().info("forwarded vote");
        }).capture((e)->{
            plugin.getLogger().warning("error while forwarding vote: "+e.getMessage());
        }).send();
    }

}

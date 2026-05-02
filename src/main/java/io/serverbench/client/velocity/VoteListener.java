package io.serverbench.client.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.vexsoftware.votifier.velocity.event.VotifierEvent;
import io.serverbench.client.common.VoteManager;

public class VoteListener {

    private final Main main;

    public VoteListener(Main main) {
        this.main = main;
        this.main.getLogger().info("created NuVotifier listener");
    }

    @Subscribe
    public void onVote(VotifierEvent event) {
        VoteManager.forwardVote(event.getVote()).then((e) -> {
            main.getLogger().info("forwarded vote");
        }).capture((e) -> {
            main.getLogger().warn("error while forwarding vote: {}", e.getMessage());
        }).send();
    }
}


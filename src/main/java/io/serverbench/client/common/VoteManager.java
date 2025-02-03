package io.serverbench.client.common;

import com.vexsoftware.votifier.model.Vote;
import io.serverbench.client.lib.Action;
import io.serverbench.client.lib.Client;

public class VoteManager {

    public static Action forwardVote(Vote vote){
        Action action = Client.getInstance()
                .community("listing.vote")
                .addArg("protocol", "NuVotifier")
                .addArg("site", vote.getServiceName())
                .addArg("username", vote.getUsername());
        if (vote.getAddress()!=null){
            action = action.addArg("address", vote.getAddress());
        }
        return action;
    }

}

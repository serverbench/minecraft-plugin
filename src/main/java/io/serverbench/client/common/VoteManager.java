package io.serverbench.client.common;

import com.vexsoftware.votifier.model.Vote;
import io.serverbench.client.lib.Action;
import io.serverbench.client.lib.Client;
import io.serverbench.client.lib.NotReadyException;
import io.serverbench.client.lib.obj.vote.VoteDisplay;
import io.serverbench.client.lib.obj.vote.VoterStatus;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VoteManager {

    private final ConcurrentHashMap<UUID, VoterStatus> votes;

    private VoteManager() {
        votes = new ConcurrentHashMap<>();
    }

    private static @Nullable VoteManager instance = null;

    public static VoteManager getInstance() {
        if(instance == null) {
            instance = new VoteManager();
        }
        return instance;
    }

    public void clearVotes() {
        votes.clear();
    }

    public void refreshCache(List<VoterStatus> displays) {
        this.clearVotes();
        for(VoterStatus status : displays) {
            votes.put(
                    UUID.fromString(status.member.eid),
                    status
            );
        }
    }

    public void removeCache(UUID uuid){
        votes.remove(uuid);
    }

    public void requestCacheIfDue() throws NotReadyException {
        for(VoteDisplay display : votes.values()){
            if(display.shouldBeRefreshed()){
                Client.getInstance().requestVoters();
                return;
            }
        }
    }

    public boolean hasVoted(UUID uuid){
        if(votes.containsKey(uuid)){
            return votes.get(uuid).hasVoterStatus();
        }
        return false;
    }

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

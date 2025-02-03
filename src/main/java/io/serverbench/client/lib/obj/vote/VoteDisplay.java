package io.serverbench.client.lib.obj.vote;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.serverbench.client.lib.Client;
import io.serverbench.client.lib.obj.Member;
import io.serverbench.client.lib.obj.vote.listingSite.ListingSiteDisplay;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class VoteDisplay {

    public final Member member;
    public final List<ListingSiteDisplay> sites;
    public final @Nullable Instant primaryCompleted;
    public final @Nullable Instant primaryNext;
    public final @Nullable Instant secondaryCompleted;
    public final @Nullable Instant secondaryNext;

    VoteDisplay(Member member, List<ListingSiteDisplay> sites, @Nullable Instant primaryCompleted, @Nullable Instant primaryNext, @Nullable Instant secondaryCompleted, @Nullable Instant secondaryNext){
        this.member = member;
        this.sites = sites;
        this.primaryCompleted = primaryCompleted;
        this.primaryNext = primaryNext;
        this.secondaryCompleted = secondaryCompleted;
        this.secondaryNext = secondaryNext;
    }

    public VoteDisplay(JsonObject object){
        this.member = new Member(object.getAsJsonObject("member"));
        this.sites = new ArrayList<>();
        for (JsonElement element : object.get("sites").getAsJsonArray()) {
            this.sites.add(new ListingSiteDisplay(
                    element.getAsJsonObject()
            ));
        }
        this.primaryCompleted = Client.parseDate(object.get("primaryCompleted"));
        this.primaryNext = Client.parseDate(object.get("primaryNext"));
        this.secondaryCompleted = Client.parseDate(object.get("secondaryCompleted"));
        this.secondaryNext = Client.parseDate(object.get("secondaryNext"));
    }

    public boolean hasVoterStatus(){
        if(this.primaryNext==null){
            return false;
        }
        return this.primaryNext.toEpochMilli()>System.currentTimeMillis();
    }

}

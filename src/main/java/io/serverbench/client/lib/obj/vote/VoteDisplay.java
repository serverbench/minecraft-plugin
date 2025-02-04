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
    public final Instant created;
    public final @Nullable Instant primaryCompleted;
    public final @Nullable Instant primaryNext;
    public final @Nullable Instant secondaryCompleted;
    public final @Nullable Instant secondaryNext;

    public VoteDisplay(JsonObject object){
        this.member = new Member(object.getAsJsonObject("member"));
        this.sites = new ArrayList<>();
        for (JsonElement element : object.get("sites").getAsJsonArray()) {
            this.sites.add(new ListingSiteDisplay(
                    element.getAsJsonObject()
            ));
        }
        this.created = Instant.parse(object.get("created").getAsString());
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

    public boolean shouldBeRefreshed() {
        if(this.hasVoterStatus()){
            if(System.currentTimeMillis()-this.created.toEpochMilli()>3600*1000){
                // created more than an hour ago, with an active voter status
                return true;
            }
        }
        if(this.primaryCompleted==null){
            // the screen was never completed, so we just wait for future on-complete events
            return false;
        }
        // the screen was completed, and the voter status expired, we should request a fresh status
        return true;
    }

}

package io.serverbench.client.lib.obj.vote;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.serverbench.client.lib.obj.Member;
import io.serverbench.client.lib.obj.vote.listingSite.ListingSiteDisplay;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


public class VoteDisplay {

    public final Member member;
    public final List<ListingSiteDisplay> sites;
    public final @Nullable Long primaryCompleted;
    public final @Nullable Long primaryNext;
    public final @Nullable Long secondaryCompleted;
    public final @Nullable Long secondaryNext;

    VoteDisplay(Member member, List<ListingSiteDisplay> sites, @Nullable Long primaryCompleted, @Nullable Long primaryNext, @Nullable Long secondaryCompleted, @Nullable Long secondaryNext){
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
        this.primaryCompleted = object.get("primaryCompleted").isJsonNull() ? null : object.get("primaryCompleted").getAsLong();
        this.primaryNext = object.get("primaryNext").isJsonNull() ? null : object.get("primaryNext").getAsLong();
        this.secondaryCompleted = object.get("secondaryCompleted").isJsonNull() ? null : object.get("secondaryCompleted").getAsLong();
        this.secondaryNext = object.get("secondaryNext").isJsonNull() ? null : object.get("secondaryNext").getAsLong();
    }

}

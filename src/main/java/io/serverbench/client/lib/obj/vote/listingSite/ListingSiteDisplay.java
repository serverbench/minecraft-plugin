package io.serverbench.client.lib.obj.vote.listingSite;

import com.google.gson.JsonObject;
import io.serverbench.client.lib.obj.vote.Vote;

import javax.annotation.Nullable;

public class ListingSiteDisplay {

    public final ListingSiteSetup site;
    public final int index;
    public final @Nullable Vote last;
    public final @Nullable Long next;
    public final boolean primary;
    public final boolean secondary;

    ListingSiteDisplay(ListingSiteSetup site, int index, @Nullable Vote last, @Nullable Long next, boolean primary, boolean secondary) {
        this.site = site;
        this.index = index;
        this.last = last;
        this.next = next;
        this.primary = primary;
        this.secondary = secondary;
    }

    public ListingSiteDisplay(JsonObject object) {
        this.site = new ListingSiteSetup(
                object.get("setup").getAsJsonObject()
        );
        this.index = object.get("index").getAsInt();
        JsonObject lastVote = object.getAsJsonObject("last");
        if(lastVote!=null) {
            this.last = new Vote(lastVote);
        } else {
            this.last = null;
        }
        this.next = object.get("next").isJsonNull() ? null : object.get("next").getAsLong();
        this.primary = object.get("primary").getAsBoolean();
        this.secondary = object.get("secondary").getAsBoolean();
    }

}

package io.serverbench.client.lib.obj.vote.listingSite;

import com.google.gson.JsonObject;
import io.serverbench.client.lib.Client;
import io.serverbench.client.lib.obj.vote.Vote;

import javax.annotation.Nullable;
import java.time.Instant;

public class ListingSiteDisplay {

    public final ListingSiteSetup site;
    public final int index;
    public final @Nullable Vote last;
    public final @Nullable Instant next;
    public final boolean primary;
    public final boolean secondary;

    ListingSiteDisplay(ListingSiteSetup site, int index, @Nullable Vote last, @Nullable Instant next, boolean primary, boolean secondary) {
        this.site = site;
        this.index = index;
        this.last = last;
        this.next = next;
        this.primary = primary;
        this.secondary = secondary;
    }

    public ListingSiteDisplay(JsonObject object) {
        this.site = new ListingSiteSetup(
                object.get("site").getAsJsonObject()
        );
        this.index = object.get("index").getAsInt();
        if(!object.get("last").isJsonNull()) {
            this.last = new Vote(object.get("last").getAsJsonObject());
        } else {
            this.last = null;
        }
        this.next = Client.parseDate(object.get("next"));
        this.primary = object.get("primary").getAsBoolean();
        this.secondary = object.get("secondary").getAsBoolean();
    }

}

package io.serverbench.client.lib.obj.vote;

import com.google.gson.JsonObject;
import io.serverbench.client.lib.Client;

import javax.annotation.Nullable;
import java.time.Instant;

public class Vote {

    public final @Nullable Instant completed;

    Vote(@Nullable final Instant completed) {
        this.completed = completed;
    }

    public Vote(JsonObject object){
        this.completed = Client.parseDate(object.get("completed"));
    }

}

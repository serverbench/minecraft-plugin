package io.serverbench.client.lib.obj.vote;

import com.google.gson.JsonObject;

import javax.annotation.Nullable;

public class Vote {

    public final @Nullable Long completed;

    Vote(@Nullable final Long completed) {
        this.completed = completed;
    }

    public Vote(JsonObject object){
        this.completed = object.get("completed").getAsLong();
    }

}

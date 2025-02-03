package io.serverbench.client.lib.obj.vote.listingSite;

import com.google.gson.JsonObject;

public class ListingSite {

    public final String id;
    public final String domain;

    ListingSite(String id, String domain) {
        this.id = id;
        this.domain = domain;
    }

    ListingSite(JsonObject object){
        this.id = object.get("id").getAsString();
        this.domain = object.get("domain").getAsString();
    }

}

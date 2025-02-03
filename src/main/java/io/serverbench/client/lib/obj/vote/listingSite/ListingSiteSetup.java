package io.serverbench.client.lib.obj.vote.listingSite;

import com.google.gson.JsonObject;

public class ListingSiteSetup {

    public final ListingSite site;
    public final String url;

    ListingSiteSetup(ListingSite site, String url) {
        this.site = site;
        this.url = url;
    }

    ListingSiteSetup(JsonObject object) {
        this.site = new ListingSite(object.get("site").getAsJsonObject());
        this.url = object.get("url").getAsString();
    }

}

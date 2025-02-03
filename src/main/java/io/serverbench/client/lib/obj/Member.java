package io.serverbench.client.lib.obj;

import com.google.gson.JsonObject;

public class Member {

    public final String eid;
    public final String name;
    public final String id;

    public Member(String eid, String name, String id) {
        this.eid = eid;
        this.name = name;
        this.id = id;
    }

    public Member(JsonObject object){
        this.eid = object.get("eid").getAsString();
        this.name = object.get("name").getAsString();
        this.id = object.get("id").getAsString();
    }

}

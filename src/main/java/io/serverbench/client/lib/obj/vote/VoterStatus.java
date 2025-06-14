package io.serverbench.client.lib.obj.vote;

import com.google.gson.JsonObject;
import io.serverbench.client.lib.Client;
import io.serverbench.client.lib.obj.Member;

import javax.annotation.Nullable;
import java.time.Instant;

public class VoterStatus {

    public final Member member;
    public final Instant created;
    @Nullable
    public final Instant until;

    public VoterStatus(JsonObject object) {
        this.member = new Member(object.get("member").getAsJsonObject());
        this.until = object.get("until").isJsonNull() ? null : Client.parseDate(object.get("until"));
        this.created = Instant.now();
    }

    public boolean hasVoterStatus(){
        if(this.until==null){
            return false;
        }
        return this.until.toEpochMilli()>System.currentTimeMillis();
    }

    public boolean shouldBeRefreshed() {
        if (this.hasVoterStatus() && System.currentTimeMillis() - this.created.toEpochMilli() > 3600000L) {
            return true;
        }
        return this.until != null;
    }

}

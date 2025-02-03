package io.serverbench.client.lib;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class Action {

    public final String rid;
    public final String action;
    private final Map<String, String> args;
    private final boolean failInstantly;

    private Consumer<JsonElement> runnable;
    private Consumer<Exception> capture;
    private Runnable end;

    private static final Gson gson = new Gson();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> timeoutFuture;

    public Action(final String action, boolean failInstantly) {
        this.action = action;
        this.args = new HashMap<>();
        this.rid = UUID.randomUUID().toString();
        this.failInstantly = failInstantly;
    }

    public static Gson getGson() {
        return gson;
    }

    public Action addArg(final String key, @Nullable final String value) {
        this.args.put(key, value);
        return this;
    }

    protected String parse() {
        JsonObject object = new JsonObject();
        object.addProperty("rid", this.rid);
        object.addProperty("action", this.action);
        JsonObject params = new JsonObject();
        for (Map.Entry<String, String> entry : this.args.entrySet()) {
            params.addProperty(entry.getKey(), entry.getValue());
        }
        object.add("params", params);
        return Action.gson.toJson(object);
    }

    protected synchronized void satisfy(JsonElement obj) {
        try {
            if (timeoutFuture != null) {
                timeoutFuture.cancel(false); // Cancel the timeout task if satisfied on time
            }
            if (this.runnable != null) {
                this.runnable.accept(obj);
            }
            if(this.end != null) {
                this.end.run();
            }
        } catch (Exception e) {
            this.fail(e);
        }
    }

    public void fail(Exception e) {
        if (timeoutFuture != null) {
            timeoutFuture.cancel(false); // Cancel the timeout task if failed for any reason
        }
        if (this.capture != null) {
            this.capture.accept(e);
        }
        if (this.end != null) {
            this.end.run();
        }
    }

    public Action send() {
        try {
            if (failInstantly){
                throw new InstantlyFailedException();
            }
            Client.getInstance().sendMessage(this);

            // Schedule a timeout task
            timeoutFuture = scheduler.schedule(() -> {
                this.fail(new TimeoutException("Action timed out after 30 seconds."));
            }, 10, TimeUnit.SECONDS);

        } catch (Exception e) {
            this.fail(e);
        }
        return this;
    }

    public Action then(Consumer<JsonElement> action) {
        this.runnable = action;
        return this;
    }

    public Action capture(Consumer<Exception> action) {
        this.capture = action;
        return this;
    }

    public Action end(Runnable action) {
        this.end = action;
        return this;
    }
}

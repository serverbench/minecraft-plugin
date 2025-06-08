package io.serverbench.client.lib;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.serverbench.client.lib.id.ExactIdentifiers;
import io.serverbench.client.lib.id.FriendlyIdentifiers;
import io.serverbench.client.lib.obj.Command;
import io.serverbench.client.lib.obj.vote.VoteDisplay;
import io.serverbench.client.lib.obj.vote.listingSite.ListingSiteDisplay;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class Client {

    protected final FriendlyIdentifiers friendlyIdentifiers;
    private final EventHandler eventHandler;
    @Nullable
    private ExactIdentifiers exactIdentifiers;
    private static Client instance;
    private WebSocketClient ws;
    private final AtomicInteger tryCount = new AtomicInteger(0);
    private boolean expectClosed = false;
    protected Logger logger;
    private final String endpoint;
    private final ScheduledExecutorService reconnectScheduler = Executors.newSingleThreadScheduledExecutor();
    private final ReentrantLock wsLock = new ReentrantLock();

    private Map<String, Action> actions = new HashMap<>();

    private Client(@Nonnull String endpoint, @Nonnull final FriendlyIdentifiers friendlyIdentifiers, EventHandler eventHandler, Logger logger) {
        this.endpoint = endpoint;
        this.friendlyIdentifiers = friendlyIdentifiers;
        this.eventHandler = eventHandler;
        this.logger = logger;
    }

    public static Client getInstance() {
        return instance;
    }

    @Nullable
    public ExactIdentifiers getExactIdentifiers() {
        return exactIdentifiers;
    }

    public void close(){
        if(ws.isOpen()){
            expectClosed = true;
            ws.close();
        }
    }

    protected void sendMessage(Action action) throws NotReadyException {
        if(Client.instance.ws.isOpen() && (this.exactIdentifiers != null || action.action.equals("community.session"))){
            this.actions.put(action.rid, action);
            Client.instance.ws.send(action.parse());
        } else {
            throw new NotReadyException();
        }
    }

    public Action session(String action){
        return instance("session."+ (exactIdentifiers != null ? exactIdentifiers.sessionId() : '?') +"."+action);
    }

    public Action instance(String action){
        return server("instance."+(exactIdentifiers != null ? exactIdentifiers.instanceId() : '?')+"."+action);
    }

    public Action server(String action){
        return community("server."+(exactIdentifiers != null ? exactIdentifiers.serverId() : '?')+"."+action);
    }

    public Action community(String action){
        return new Action("community."+(exactIdentifiers != null ? exactIdentifiers.communityId() : '?')+"."+action, exactIdentifiers==null);
    }

    private static String getEnvOrDefault(String envKey, String defaultValue) {
        String val = System.getenv(envKey);
        return (val != null && !val.trim().isEmpty()) ? val : defaultValue;
    }

    public static void initialize(@Nonnull String endpoint, @Nonnull EventHandler eventHandler, @Nonnull Logger logger, @Nonnull String key, @Nonnull String name, @Nullable String instance) {
        Client.instance = new Client(endpoint, new FriendlyIdentifiers(
                getEnvOrDefault("SERVERBENCH_SK", key),
                getEnvOrDefault("SERVERBENCH_SERVER", name),
                getEnvOrDefault("SERVERBENCH_INSTANCE", instance)
        ), eventHandler, logger);
        Client.instance.expectClosed = false;
        Client.instance.connectWebSocket();
    }

    private void requestCommands() throws NotReadyException {
        Client.instance.session("cmd").send().then((o) -> {
            JsonArray arr = o.getAsJsonArray();
            logger.info("Processing " + arr.size() + " commands");
            List<Command> commands = new ArrayList<>();
            for (JsonElement elem : arr) {
                JsonObject cmd = elem.getAsJsonObject();
                commands.add(new Command(cmd.get("id").getAsString(), cmd.get("cmd").getAsString()));
            }
            eventHandler.cmd().accept(commands);
        });
    }

    public void requestVoters() throws NotReadyException {
        Client.instance.session("voters").send().then((o) -> {
            JsonArray arr = o.getAsJsonArray();
            logger.info("Processing " + arr.size() + " voters");
            List<VoteDisplay> displays = new ArrayList<>();
            for (JsonElement elem : arr) {
                VoteDisplay voteDisplay = new VoteDisplay(
                        elem.getAsJsonObject()
                );
                displays.add(voteDisplay);
            }
            this.eventHandler.voteDisplay().accept(displays);
        }).capture(e -> e.printStackTrace());
    }

    private void connectWebSocket() {
        if(this.ws!=null) return;
        wsLock.lock();
        try {
            if (this.expectClosed) {
                return;
            }
            logger.info("Connecting...");
            this.ws = new WebSocketClient(new URI(this.endpoint + "?key=" + this.friendlyIdentifiers.key())) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    logger.info("Authenticating...");
                    if (exactIdentifiers != null) {
                        logger.warning("Duplicate authentication detected!");
                        return; // Prevent multiple authentication attempts
                    }

                    new Action("community.session", false)
                            .addArg("key", Client.instance.friendlyIdentifiers.key())
                            .addArg("server", Client.instance.friendlyIdentifiers.name())
                            .addArg("instance", Client.instance.friendlyIdentifiers.instance())
                            .then((el) -> {
                                JsonObject obj = el.getAsJsonObject();
                                JsonObject instance = obj.get("instance").getAsJsonObject();
                                JsonObject server = instance.get("server").getAsJsonObject();
                                JsonObject community = server.get("community").getAsJsonObject();
                                Client.instance.exactIdentifiers = new ExactIdentifiers(
                                        community.get("id").getAsString(),
                                        server.get("id").getAsString(),
                                        instance.get("id").getAsString(),
                                        obj.get("id").getAsString()
                                );
                                Client.instance.tryCount.set(0);
                                logger.info("Authenticated, initializing command listener");
                                eventHandler.open().run();
                                try {
                                    requestCommands();
                                    requestVoters();
                                } catch (NotReadyException e) {
                                    logger.warning("Error while request commands");
                                }
                            })
                            .send()
                            .capture((e)->{
                                logger.warning("Error while authenticating: " + e.getMessage());
                                ws.close();
                            });
                }

                @Override
                public void onMessage(String s) {
                    JsonObject obj = Action.getGson().fromJson(s, JsonObject.class);
                    if (obj.has("rid")) {
                        String rid = obj.get("rid").getAsString();
                        if (Client.instance.actions.containsKey(rid)) {
                            Client.instance.actions.get(rid).satisfy(obj.get("result"));
                            Client.instance.actions.remove(rid);
                        }
                    } else {
                        try {
                            handleUnrequestedEvent(obj);
                        } catch (NotReadyException e) {
                            logger.warning("Error while handling serverbench request");
                        }
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Client.instance.scheduleReconnect();
                }

                @Override
                public void onError(Exception e) {
                    logger.severe("WebSocket Error: " + e.getMessage());
                }
            };
            this.ws.connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } finally {
            wsLock.unlock();
        }
    }

    public static Instant parseDate(JsonElement element){
        if(element.isJsonNull()){
            return null;
        } else if (element.getAsJsonPrimitive().isNumber()){
            return Instant.ofEpochMilli(element.getAsJsonPrimitive().getAsLong());
        } else if (element.getAsJsonPrimitive().isString()){
            return Instant.parse(element.getAsString());
        }
        return null;
    }

    private void handleUnrequestedEvent(JsonObject obj) throws NotReadyException {
        String realm = obj.get("realm").getAsString();
        String action = obj.get("action").getAsString();
        switch (realm) {
            case "session":
                if ("cmd".equals(action)) {
                    requestCommands();
                } else if ("voters".equals(action)) {
                    requestVoters();
                }
                break;
            case "instance":
                instance(action);
                break;
            case "community":
                community(action);
                break;
            default:
                logger.warning("Unrecognized realm: " + realm);
        }
        logger.info("Received unrequested event: " + obj);
    }

    private void scheduleReconnect() {
        if (this.ws != null) {
            if(this.ws.isOpen()){
                this.ws.close();
                return; // onClose will call us back when needed
            }
            this.ws = null;
        }
        if(Client.instance.exactIdentifiers!=null){
            Client.instance.exactIdentifiers = null;
            Client.instance.eventHandler.close().run();
            logger.warning("Connection lost...");
        }
        if (expectClosed) return;
        int currentTryCount = tryCount.incrementAndGet(); // Atomically increment and get the value
        int delay = Math.min(currentTryCount, 10); // Cap the delay at 10 seconds
        reconnectScheduler.schedule(this::connectWebSocket, delay, TimeUnit.SECONDS);
        logger.info("Scheduled reconnection attempt #" + currentTryCount + " in " + delay + " seconds");
    }
}
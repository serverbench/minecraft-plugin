package io.serverbench.client.common;

import io.serverbench.client.common.strayWorker.ConnectedMember;
import io.serverbench.client.lib.Client;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class ConnectionManager {
    private static ConnectionManager instance;

    private final Map<UUID, Boolean> connected = new ConcurrentHashMap<>(); // connected members and their idle state
    private final Map<UUID, ConnectedMember> connectedInfo = new ConcurrentHashMap<>();
    private final Map<UUID, String> domains = new ConcurrentHashMap<>();
    private final Set<UUID> connecting = new HashSet<>();

    @Nullable
    private Logger logger = null;

    private ConnectionManager() { }

    public static ConnectionManager getInstance() {
        if (ConnectionManager.instance == null) {
            ConnectionManager.instance = new ConnectionManager();
        }
        return ConnectionManager.instance;
    }

    public boolean isConnectedAsIdle(UUID id){
        return connected.get(id);
    }

    public void setLogger(@Nullable Logger logger) {
        this.logger = logger;
    }

    public void registerHostname(UUID id, String hostname) {
        this.domains.put(id, hostname);
    }

    public void unsetHostname(UUID id) {
        this.domains.remove(id);
    }

    public void openConnection(UUID id, String name, String platform, @Nullable String proxy, @Nullable InetSocketAddress address, boolean idle) {
        if(connecting.contains(id)) {
            return;
        }
        connecting.add(id);
        try {
            String domain = domains.get(id);
            Client.getInstance().session("connect")
                    .addArg("id", id.toString())
                    .addArg("name", name)
                    .addArg("platform", platform)
                    .addArg("address", address == null ? null : address.getAddress().getHostAddress())
                    .addArg("entrypoint", domain)
                    .addArg("proxy", proxy)
                    .addArg("idle", idle ? "true" : "false")
                    .then((connection) -> {
                        String connectionId = connection.getAsJsonObject().get("id").getAsString();
                        connected.put(id, idle);
                        connectedInfo.put(id, new ConnectedMember(
                                id,
                                name,
                                address,
                                platform
                        ));
                        assert logger != null;
                        logger.info(id + " (" + name + ") connected #" + connectionId);
                    })
                    .capture((e) -> {
                        assert logger != null;
                        logger.warning(id + " (" + name + ") not connected: " + e.getMessage());
                    })
                    .end(()-> connecting.remove(id))
                    .send();
        } catch (Exception e) {
            e.printStackTrace();
            connected.remove(id);
        }
    }

    public void closeConnection(UUID id, String name, String platform) {
        Client
                .getInstance()
                .session("disconnect")
                .addArg("id", id.toString())
                .addArg("name", name)
                .addArg("platform", platform)
                .then((connection) -> {
                    this.connected.remove(id);
                    this.connectedInfo.remove(id);
                    assert logger != null;
                    logger.info(id + " (" + name + ") disconnected");
                })
                .capture((e)->{
                    assert logger != null;
                    logger.warning(id + " (" + name + ") not disconnected: " + e.getMessage());
                })
                .send();
    }

    public boolean isConnected(UUID id, boolean idleState) {
        return connected.containsKey(id) && idleState == connected.get(id);
    }

    public Map<UUID, ConnectedMember> getConnected() {
        return new HashMap<>(connectedInfo);
    }

    public void clearConnections() {
        connected.clear();
        connectedInfo.clear();
        connecting.clear();
        assert logger != null;
        logger.info("Cleared all connections and action queues.");
    }
}
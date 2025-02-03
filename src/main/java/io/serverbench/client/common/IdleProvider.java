package io.serverbench.client.common;

import java.net.InetSocketAddress;
import java.util.UUID;

public abstract class IdleProvider {

    private final boolean slave;
    private final ProxyMessaging proxyMessaging;;

    public IdleProvider(boolean slave, ProxyMessaging proxyMessaging) {
        this.slave=slave;
        this.proxyMessaging = proxyMessaging;
    }

    public abstract boolean isIdle(UUID id);

    public void setIdle(boolean idle, UUID id, String name, String platform, InetSocketAddress address) {
        // if the player is not connected, don't do anything, it will be connected by the stray worker
        if (!ConnectionManager.getInstance().getConnected().containsKey(id)) {
            return;
        }
        // if the connection state matches the target idle state, don't do anything
        if(idle == ConnectionManager.getInstance().isConnectedAsIdle(id)) {
            return;
        }
        if(slave){
            proxyMessaging.syncRequest(id);
        } else {
            ConnectionManager.getInstance().openConnection(
                    id,
                    name,
                    platform,
                    null,
                    address,
                    idle
            );
        }
    }

    public abstract void register();

}

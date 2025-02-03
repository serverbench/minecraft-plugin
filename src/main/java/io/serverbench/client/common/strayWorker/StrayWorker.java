package io.serverbench.client.common.strayWorker;

import io.serverbench.client.common.ConnectionManager;
import io.serverbench.client.common.IdleProvider;
import io.serverbench.client.common.ProxyMessaging;

import javax.annotation.Nullable;
import java.util.*;

public abstract class StrayWorker implements Runnable {

    boolean slave;
    @Nullable
    ProxyMessaging messaging;
    @Nullable
    IdleProvider idleProvider;

    public StrayWorker(boolean slave, @Nullable ProxyMessaging messaging, @Nullable IdleProvider idleProvider) {
        this.slave=slave;
        this.messaging=messaging;
        this.idleProvider=idleProvider;
    }

    @Override
    public void run() {
        Map<UUID, ConnectedMember> orphans = ConnectionManager.getInstance().getConnected();
        for (ConnectedMember connectedMember : this.getOnlineMembers()) {
            orphans.remove(connectedMember.id());
            boolean idle = idleProvider != null && idleProvider.isIdle(connectedMember.id());
            if(!ConnectionManager.getInstance().isConnected(connectedMember.id(), idle)) {
                if(slave){
                    assert messaging != null;
                    messaging.syncRequest(connectedMember.id());
                } else {
                    ConnectionManager.getInstance().openConnection(
                            connectedMember.id(),
                            connectedMember.name(),
                            connectedMember.platform(),
                            null,
                            connectedMember.address(),
                            idle
                    );
                }
            }
        }
        for (ConnectedMember orphan : orphans.values()){
            ConnectionManager.getInstance().closeConnection(
                    orphan.id(),
                    orphan.name(),
                    orphan.platform()
            );
        }
    }

    protected abstract Set<ConnectedMember> getOnlineMembers();

}

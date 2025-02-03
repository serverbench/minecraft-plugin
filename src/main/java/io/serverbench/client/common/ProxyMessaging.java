package io.serverbench.client.common;

import java.util.UUID;

public abstract class ProxyMessaging {

    public abstract void syncRequest(UUID id);
    public abstract void register();

}

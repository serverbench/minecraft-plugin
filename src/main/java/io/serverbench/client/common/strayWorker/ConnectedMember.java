package io.serverbench.client.common.strayWorker;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.UUID;

public record ConnectedMember(UUID id, String name, @Nullable InetSocketAddress address, String platform) {
}

package io.serverbench.client.velocity;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import io.serverbench.client.lib.Client;
import io.serverbench.client.lib.id.ExactIdentifiers;

import java.util.UUID;

public class JoinListener {

    private final Main main;

    public JoinListener(Main main) {
        this.main = main;
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        main.getLogger().info("forwarding connection for {}", event.getPlayer().getUsername());
        event.getPlayer().getCurrentServer().ifPresent(this::sendConnection);
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(Main.CHANNEL)) return;
        if (!(event.getSource() instanceof ServerConnection)) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String subChannel = in.readUTF();
        if ("sync".equals(subChannel)) {
            String uuidStr = in.readUTF();
            String name = in.readUTF();

            Player player = null;
            try {
                player = main.getServer().getPlayer(UUID.fromString(uuidStr)).orElse(null);
            } catch (IllegalArgumentException ignored) {
                // not a valid UUID, fall through to name lookup
            }
            if (player == null) {
                player = main.getServer().getPlayer(name).orElse(null);
            }
            if (player != null) {
                main.getLogger().info("forwarding sync for {}", player.getUsername());
                player.getCurrentServer().ifPresent(this::sendConnection);
            }
        }
    }

    private void sendConnection(ServerConnection connection) {
        ExactIdentifiers identifiers = Client.getInstance().getExactIdentifiers();
        if (identifiers == null) {
            return;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("connect");
        out.writeUTF(identifiers.sessionId());
        connection.sendPluginMessage(Main.CHANNEL, out.toByteArray());
    }
}



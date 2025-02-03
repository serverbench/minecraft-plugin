package io.serverbench.client.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.serverbench.client.lib.Client;
import io.serverbench.client.lib.id.ExactIdentifiers;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

public class JoinListener implements Listener {

    Plugin plugin;
    public JoinListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(ServerConnectedEvent event) {
        plugin.getLogger().info("forwarding connection for "+event.getPlayer().getName());
        sendConnection(event.getPlayer());
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event){
        if (!event.getTag().equals("serverbench:connection")) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String subChannel = in.readUTF();
        if ("sync".equals(subChannel)) {
            ProxiedPlayer player = plugin.getProxy().getPlayer(UUID.fromString(in.readUTF()));
            if(player==null){
                player = plugin.getProxy().getPlayer(in.readUTF());
            }
            if(player!=null){
                plugin.getLogger().info("forwarding sync for "+player.getName());
                sendConnection(player);
            }
        }
    }

    private void sendConnection(ProxiedPlayer player){
        ExactIdentifiers identifiers = Client.getInstance().getExactIdentifiers();
        if(identifiers==null){
            return;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("connect");
        out.writeUTF(identifiers.sessionId());
        player.getServer().sendData("serverbench:connection", out.toByteArray());
    }

}

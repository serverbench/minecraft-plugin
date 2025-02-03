package io.serverbench.client.spigot;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.serverbench.client.common.ConnectionManager;
import io.serverbench.client.common.IdleProvider;
import io.serverbench.client.common.ProxyMessaging;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import javax.annotation.Nullable;
import java.util.UUID;

public class SpigotMessaging extends ProxyMessaging implements PluginMessageListener {

    Plugin plugin;
    @Nullable
    IdleProvider idleProvider;

    SpigotMessaging(Plugin plugin) {
        this.plugin = plugin;
        this.idleProvider = null;
    }

    public void setIdleProvider(@Nullable IdleProvider idleProvider) {
        this.idleProvider = idleProvider;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if (!channel.equals("serverbench:connection")) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        String subchannel = in.readUTF();
        if(subchannel.equals("connect")) {
            String proxy = in.readUTF();
            ConnectionManager.getInstance().openConnection(
                    player.getUniqueId(),
                    player.getName(),
                    "minecraft/java",
                    proxy,
                    player.getAddress(),
                    idleProvider != null && idleProvider.isIdle(player.getUniqueId())
            );
        }
    }

    @Override
    public void syncRequest(UUID id) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("sync");
        Player player = Bukkit.getPlayer(id);
        assert player != null;
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(player.getName());
        player.sendPluginMessage(
                plugin,
                "serverbench:connection",
                out.toByteArray()
        );
    }

    @Override
    public void register() {
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "serverbench:connection", this);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "serverbench:connection");
    }
}

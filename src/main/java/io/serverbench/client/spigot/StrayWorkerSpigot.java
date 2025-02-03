package io.serverbench.client.spigot;

import io.serverbench.client.common.IdleProvider;
import io.serverbench.client.common.ProxyMessaging;
import io.serverbench.client.common.strayWorker.ConnectedMember;
import io.serverbench.client.common.strayWorker.StrayWorker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class StrayWorkerSpigot extends StrayWorker {

    public StrayWorkerSpigot(boolean slave, ProxyMessaging messaging, @Nullable IdleProvider idleProvider) {
        super(slave, messaging, idleProvider);
    }

    @Override
    protected Set<ConnectedMember> getOnlineMembers() {
        Set<ConnectedMember> members = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()){
            members.add(new ConnectedMember(
                    player.getUniqueId(),
                    player.getName(),
                    player.getAddress(),
                    "minecraft/java"
            ));
        }
        return members;
    }


}

package io.serverbench.client.spigot;

import io.serverbench.client.common.ConnectionManager;
import io.serverbench.client.common.IdleProvider;
import io.serverbench.client.common.VoteManager;
import io.serverbench.client.lib.Client;
import io.serverbench.client.lib.EventHandler;
import io.serverbench.client.lib.NotReadyException;
import io.serverbench.client.lib.obj.Command;
import io.serverbench.client.lib.obj.vote.VoteDisplay;
import io.serverbench.client.spigot.idleProvider.AfkPlusIdleProvider;
import io.serverbench.client.spigot.idleProvider.EssentialsIdleProvider;
import io.serverbench.client.spigot.placeholderProvider.PapiProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Main extends JavaPlugin {

    boolean isSlave = false;

    @Override
    public void onEnable() {
        super.onEnable();
        saveDefaultConfig();
        ConnectionManager.getInstance().setLogger(this.getLogger());

        // connection logic flow
        isSlave = getServer().spigot().getConfig().getBoolean("settings.bungeecord") && !getConfig().getBoolean("disable_bungee");
        SpigotMessaging messaging = null;
        if(isSlave){
            messaging = new SpigotMessaging(this);
            messaging.register();
        }

        // idle providers
        IdleProvider idleProvider = null;
        if(hasPlugin("AFKPlus")){
            idleProvider = new AfkPlusIdleProvider(this, isSlave, messaging);
        } else if (hasPlugin("Essentials")){
            idleProvider = new EssentialsIdleProvider(this, isSlave, messaging);
        }
        if(idleProvider!=null){
            idleProvider.register();
            if (messaging!=null){
                messaging.setIdleProvider(idleProvider);
            }
        }

        // placeholder providers
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("hooked into PlaceholderAPI");
            new PapiProvider().register();
        }

        // session event input
        getServer().getPluginManager().registerEvents(new ConnectionListener(
                isSlave,
                this,
                idleProvider
        ), this);
        StrayWorkerSpigot strayWorkerSpigot = new StrayWorkerSpigot(
                isSlave,
                messaging,
                idleProvider
        );
        // chat events
        getServer().getPluginManager().registerEvents(new ChatListener(
                this
        ), this);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, strayWorkerSpigot, 0, 20*5);

        // voting event input
        if(getServer().getPluginManager().getPlugin("Votifier")!=null){
            getServer().getPluginManager().registerEvents(new VoteListener(this), this);
        }
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, ()->{
            try {
                VoteManager.getInstance().requestCacheIfDue();
            } catch (NotReadyException e) {
                getLogger().warning("unable to update voter cache: "+e.getMessage());
            }
        }, 0, 20L*60);

        // initialization
        if(getConfig().get("key") == null || getConfig().get("name") == null) {
            getLogger().severe("serverbench is not setup, please, specify 'key', 'name', and (optionally) 'instance' on plugins/serverbench/config.yml");
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(this, ()-> Client.initialize(
                    Objects.requireNonNull(getConfig().getString("endpoint")),
                    new EventHandler(
                            (cmds) -> Bukkit.getScheduler().runTask(this, () -> {
                                for (Command cmd : cmds) {
                                    getServer().dispatchCommand(getServer().getConsoleSender(), cmd.cmd);
                                }
                            }),
                            (voters) -> {
                                VoteManager.getInstance().refreshCache(voters);
                            },
                            () -> {
                                // the client reconnected, reconnect stray players
                                strayWorkerSpigot.run();
                            },
                            () -> {
                                // the client disconnected, which means we should close all the active connections,
                                // as they should have been closed by serverbench upon disconnecting the client
                                ConnectionManager.getInstance().clearConnections();
                                VoteManager.getInstance().clearVotes();
                            }
                    ),
                    getLogger(),
                    Objects.requireNonNull(getConfig().getString("key")),
                    Objects.requireNonNull(getConfig().getString("name")),
                    getConfig().getString("instance")
            ));
        }
    }

    private boolean hasPlugin(String name){
        return getServer().getPluginManager().getPlugin(name) != null;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Client.getInstance().close();
        if(isSlave){
            this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
            this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        }
    }

}
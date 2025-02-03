package io.serverbench.client.bungee;

import io.serverbench.client.lib.Client;
import io.serverbench.client.lib.EventHandler;
import io.serverbench.client.lib.obj.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Main extends Plugin {

    @Override
    public void onEnable() {
        super.onEnable();
        getProxy().registerChannel("serverbench:connection");
        try {
            if (!getDataFolder().exists()) {
                getLogger().info("Created config folder: " + getDataFolder().mkdir());
            }
            File configFile = new File(getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                FileOutputStream outputStream = new FileOutputStream(configFile);
                InputStream in = getResourceAsStream("config.yml");
                in.transferTo(outputStream);
            }
            Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            if(configuration.getString("key") == null || configuration.getString("name") == null) {
                getLogger().severe("serverbench is not setup, please, specify 'key', 'name', and (optionally) 'instance' on plugins/serverbench/config.yml");
            } else {
                Client.initialize(
                        configuration.getString("endpoint"),
                        new EventHandler(
                            (cmds)->{
                                for (Command cmd : cmds) {
                                    getProxy().getPluginManager().dispatchCommand(getProxy().getConsole(), cmd.cmd);
                                }
                            },
                            (voters) -> {

                            },
                            () -> { },
                            () -> { }
                        ),
                        getLogger(),
                        configuration.getString("key"),
                        configuration.getString("name"),
                        configuration.getString("instance")
                );
            }
        } catch (IOException e) {
            getLogger().severe("serverbench was unable to start: "+e.getMessage());
        }
        getProxy().getPluginManager().registerListener(this, new JoinListener(this));
        getProxy().registerChannel("serverbench:connection");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Client.getInstance().close();
    }
}

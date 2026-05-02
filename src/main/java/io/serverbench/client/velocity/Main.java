package io.serverbench.client.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import io.serverbench.client.lib.Client;
import io.serverbench.client.lib.EventHandler;
import io.serverbench.client.lib.obj.Command;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@Plugin(id = "serverbench", name = "serverbench-beta", authors = {"serverbench"})
public class Main {

    public static final MinecraftChannelIdentifier CHANNEL = MinecraftChannelIdentifier.from("serverbench:connection");

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final java.util.logging.Logger julLogger;

    @Inject
    public Main(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.julLogger = java.util.logging.Logger.getLogger("serverbench");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getChannelRegistrar().register(CHANNEL);
        try {
            File dataFolder = dataDirectory.toFile();
            if (!dataFolder.exists()) {
                logger.info("Created config folder: {}", dataFolder.mkdir());
            }
            File configFile = new File(dataFolder, "config.yml");
            if (!configFile.exists()) {
                try (FileOutputStream outputStream = new FileOutputStream(configFile);
                     InputStream in = getClass().getResourceAsStream("/config.yml")) {
                    if (in != null) {
                        in.transferTo(outputStream);
                    }
                }
            }

            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .path(configFile.toPath())
                    .build();
            ConfigurationNode config = loader.load();

            String key = config.node("key").getString();
            String name = config.node("name").getString();
            String instance = config.node("instance").getString();

            if (key == null || name == null) {
                logger.error("serverbench is not setup, please specify 'key', 'name', and (optionally) 'instance' on plugins/serverbench/config.yml");
            } else {
                Client.initialize(
                        config.node("endpoint").getString("wss://stream.beta.serverbench.io"),
                        new EventHandler(
                                (cmds) -> {
                                    for (Command cmd : cmds) {
                                        server.getCommandManager().executeImmediatelyAsync(server.getConsoleCommandSource(), cmd.cmd);
                                    }
                                },
                                (voters) -> {},
                                () -> {},
                                () -> {}
                        ),
                        julLogger,
                        key,
                        name,
                        instance
                );
            }
        } catch (IOException e) {
            logger.error("serverbench was unable to start: {}", e.getMessage());
        }

        server.getEventManager().register(this, new JoinListener(this));
        server.getPluginManager().getPlugin("nuvotifier").ifPresent(plugin ->
                server.getEventManager().register(this, new VoteListener(this))
        );
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (Client.getInstance() != null) {
            Client.getInstance().close();
        }
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }
}


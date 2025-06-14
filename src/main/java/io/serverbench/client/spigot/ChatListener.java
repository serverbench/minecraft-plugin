package io.serverbench.client.spigot;

import io.serverbench.client.lib.Client;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.logging.Logger;

public class ChatListener implements Listener {

    Plugin plugin;
    Logger logger;
    HashMap<UUID, UUID> lastChatterIds = new HashMap<>();
    Set<String> dmCommands = new HashSet<>();
    Set<String> replyCommands = new HashSet<>();

    ChatListener(Plugin plugin) {
        this.logger = plugin.getLogger();
        this.plugin = plugin;
        dmCommands.add("msg");
        dmCommands.add("tell");
        dmCommands.add("w");
        replyCommands.add("r");
        replyCommands.add("reply");
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Client.getInstance().session("chat.send")
            .addArg("fromEid", event.getPlayer().getUniqueId().toString())
            .addArg("message", event.getMessage())
            .capture((e) -> {
                logger.warning("chat message not forwarded: "+e.getMessage());
            })
            .send();
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        this.lastChatterIds.remove(event.getPlayer().getUniqueId());
        Set<UUID> strayStarters = new HashSet<>();
        for (UUID starter : this.lastChatterIds.keySet()) {
            if (this.lastChatterIds.get(starter).equals(event.getPlayer().getUniqueId())) {
                strayStarters.add(starter);
            }
        }
        for (UUID starter : strayStarters) {
            this.lastChatterIds.remove(starter);
        }
    }

    @EventHandler
    public void onAnvilRename(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof AnvilInventory)) return;
        if (event.getSlotType() != InventoryType.SlotType.RESULT) return;

        AnvilInventory anvil = (AnvilInventory) event.getInventory();
        ItemStack input = anvil.getItem(0);
        ItemStack result = event.getCurrentItem();

        if (input == null || result == null) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        ItemMeta inputMeta = input.getItemMeta();
        ItemMeta resultMeta = result.getItemMeta();
        if (inputMeta == null || resultMeta == null) return;

        String oldName = inputMeta.hasDisplayName() ? inputMeta.getDisplayName() : input.getType().name();
        String newName = resultMeta.hasDisplayName() ? resultMeta.getDisplayName() : result.getType().name();

        if (!oldName.equals(newName)) {
            Player player = (Player) event.getWhoClicked();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Client.getInstance().session("chat.send")
                        .addArg("fromEid", player.getUniqueId().toString())
                        .addArg("message", newName)
                        .addArg("channel", "rename")
                        .capture((e) -> {
                            logger.warning("item rename not forwarded: " + e.getMessage());
                        })
                        .send();
            });
        }
    }

    @EventHandler
    public void onBookWrite(PlayerEditBookEvent event) {
        List<String> oldPages = event.getPreviousBookMeta().getPages();
        String oldContent = String.join("\n", oldPages);
        List<String> pages = event.getNewBookMeta().getPages();
        String content = String.join("\n", pages);

        if (!oldContent.equals(content)) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Client.getInstance().session("chat.send")
                        .addArg("fromEid", event.getPlayer().getUniqueId().toString())
                        .addArg("message", content)
                        .addArg("channel", "book")
                        .capture((e) -> {
                            logger.warning("book write not forwarded: " + e.getMessage());
                        })
                        .send();
            });
        }
    }

    @EventHandler
    public void onPrivateMessage(PlayerCommandPreprocessEvent event) {
        String[] parts = event.getMessage().split("\\s+");
        if (parts.length >= 2) {
            UUID sender = event.getPlayer().getUniqueId();
            UUID receiver = null;
            String content = null;
            boolean found = false;
            if (parts.length >= 3) {
                for (String command : dmCommands) {
                    if (parts[0].toLowerCase().equals("/" + command)) {
                        String recipient = parts[1];
                        Player recipientPlayer = Bukkit.getPlayer(recipient);
                        if (recipientPlayer != null) {
                            // set recipient /r to us
                            lastChatterIds.put(recipientPlayer.getUniqueId(), event.getPlayer().getUniqueId());
                            receiver = recipientPlayer.getUniqueId();
                            content = String.join(" ", Arrays.stream(parts).toList().subList(2, parts.length));
                        }
                        found = true;
                        break;
                    }
                }
            }
            if(!found) {
                for (String command : replyCommands) {
                    if (parts[0].toLowerCase().equals("/" + command)) {
                        if (lastChatterIds.containsKey(event.getPlayer().getUniqueId())) {
                            // set original sender /r to us
                            lastChatterIds.put(lastChatterIds.get(event.getPlayer().getUniqueId()), event.getPlayer().getUniqueId());
                            receiver = lastChatterIds.get(event.getPlayer().getUniqueId());
                            content = String.join(" ", Arrays.stream(parts).toList().subList(1, parts.length));
                        }
                        break;
                    }
                }
            }
            if (receiver != null ) {
                UUID finalReceiver = receiver;
                String finalContent = content;
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    Client.getInstance().session("chat.send")
                            .addArg("fromEid", sender.toString())
                            .addArg("toEid", finalReceiver.toString())
                            .addArg("message", finalContent)
                            .capture((e) -> {
                                logger.warning("chat message not forwarded: " + e.getMessage());
                            })
                            .send();
                });
            }
        }
    }

}

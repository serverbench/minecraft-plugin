package io.serverbench.client.spigot.placeholderProvider;

import io.serverbench.client.common.VoteManager;
import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PapiProvider extends PlaceholderExpansion {

    @Override
    public @NotNull String getAuthor() {
        return "serverbench";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "serverbench";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if(params.equalsIgnoreCase("voted")) {
            return VoteManager.getInstance().hasVoted(player.getUniqueId()) ? "yes" : "no";
        }
        return null;
    }
}
package io.github.tanguygab.advancedbungeeexpansion.spigot;

import io.github.tanguygab.advancedbungeeexpansion.ServerInfo;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Taskable;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import javax.annotation.Nonnull;
import java.util.*;


public class AdvancedBungeeExpansion extends PlaceholderExpansion implements Taskable {

    protected final String CHANNEL = "advancedbungee:channel";
    protected final Map<String, ServerInfo> servers = new HashMap<>();
    protected ServerInfo currentServer;
    protected boolean loaded;
    @Getter private final String identifier = "advancedbungee";
    @Getter private final String author = "Tanguygab";
    @Getter private final String version = "1.0.2";

    @Getter @Accessors(fluent = true) private final boolean canRegister = Bukkit.getServer().spigot().getConfig().getBoolean("settings.bungeecord",false);

    @Override
    public void start() {
        loaded = false;
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(getPlaceholderAPI(), CHANNEL,new SpigotListener(this));
    }

    @Override
    public void stop() {
        loaded = false;
        Bukkit.getServer().getMessenger().unregisterIncomingPluginChannel(getPlaceholderAPI(), CHANNEL);
        servers.clear();
    }

    @Override
    public String onRequest(OfflinePlayer player, @Nonnull String params) {
        if (!loaded) return "Loading data...";

        String[] args = params.split("_");
        String[] serverArgs = args[0].split(":");
        String category = serverArgs[0];
        String serverName = serverArgs.length > 1 ? serverArgs[1] : null;
        String result = args.length > 1 ? args[1] : "name";

        return switch (category) {
            case "servers" -> switch (result) {
                case "name" -> String.join(", ", servers.keySet());
                case "count" -> String.valueOf(servers.size());
                case "playercount" -> {
                    int i = 0;
                    for (ServerInfo server : servers.values()) i+=server.getPlayerCount();
                    yield String.valueOf(i);
                }
                case "players" -> {
                    List<String> players = new ArrayList<>();
                    servers.values().forEach(server->players.addAll(server.getPlayers()));
                    yield String.join(", ",players);
                }
                default -> null;
            };
            case "current" -> getServerInfo(currentServer,result);
            case "server" -> getServerInfo(servers.get(serverName),result);
            default -> null;
        };
    }

    private String getServerInfo(ServerInfo server, String result) {
        if (server == null) return "Unknown server";
        return switch (result) {
            case "playercount" -> String.valueOf(server.getPlayerCount());
            case "players" -> String.join(", ",server.getPlayers());
            case "status" -> ChatColor.COLOR_CHAR + (server.isStatus() ? "aOnline" : "cOffline");
            case "motd" -> server.getMotd();
            default -> server.getName();
        };
    }
}

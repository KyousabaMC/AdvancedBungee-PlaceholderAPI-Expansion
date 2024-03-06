package io.github.tanguygab.advancedbungeeexpansion.spigot;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import io.github.tanguygab.advancedbungeeexpansion.ServerInfo;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import javax.annotation.Nonnull;
import java.util.List;

public record SpigotListener(AdvancedBungeeExpansion expansion) implements PluginMessageListener {

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void onPluginMessageReceived(@Nonnull String channel, @Nonnull Player player, @Nonnull byte[] message) {
        if (!expansion.CHANNEL.equals(channel)) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        switch (subChannel) {
            case "Load" -> {
                String currentServer = in.readUTF();
                while (true) {
                    String line = in.readUTF();
                    if (line.equals("End")) break;
                    String[] args = line.split("\\|");
                    String server = args[0];
                    boolean status = Boolean.parseBoolean(args[1]);
                    String motd = args[2];
                    List<String> players = args.length > 3 ? List.of(args[3].split(",")) : List.of();
                    expansion.servers.put(server,new ServerInfo(server,status,motd,players));
                }
                expansion.currentServer = expansion.servers.get(currentServer);
                expansion.loaded = true;
            }
            case "Unload" -> expansion.loaded = false;
            case "Players" -> {
                ServerInfo info = expansion.servers.get(in.readUTF());
                if (info == null) return;
                info.setPlayers(List.of(in.readUTF().split(",")));
            }
            case "Status" -> {
                ServerInfo info = expansion.servers.get(in.readUTF());
                if (info != null) info.setStatus(in.readBoolean());
            }
            case "MOTD" -> {
                ServerInfo info = expansion.servers.get(in.readUTF());
                if (info != null) info.setMotd(in.readUTF());
            }
        }

    }
}

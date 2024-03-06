package io.github.tanguygab.advancedbungeeexpansion.spigot

import com.google.common.io.ByteStreams
import io.github.tanguygab.advancedbungeeexpansion.ServerInfo
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import java.util.List
import javax.annotation.Nonnull

class SpigotListener(private val expansion: AdvancedBungeeExpansion) : PluginMessageListener {
    override fun onPluginMessageReceived(
        @Nonnull channel: String,
        @Nonnull player: Player,
        @Nonnull message: ByteArray
    ) {
        if (expansion.CHANNEL != channel) return
        val `in` = ByteStreams.newDataInput(message)
        val subChannel = `in`.readUTF()
        when (subChannel) {
            "Load" -> {
                val currentServer = `in`.readUTF()
                while (true) {
                    val line = `in`.readUTF()
                    if (line == "End") break
                    val args = line.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val server = args[0]
                    val status = args[1].toBoolean()
                    val motd = args[2]
                    val players = if (args.size > 3) List.of(*args[3].split(",".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()) else listOf()
                    expansion.servers[server] = ServerInfo(server, status, motd, players)
                }
                expansion.currentServer = expansion.servers[currentServer]
                expansion.loaded = true
            }

            "Unload" -> {
                expansion.loaded = false
            }
            "Players" -> {
                val info = expansion.servers[`in`.readUTF()] ?: return
                info.players = List.of(*`in`.readUTF().split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray())
            }

            "Status" -> {
                val info = expansion.servers[`in`.readUTF()]
                if (info != null) info.status = `in`.readBoolean()
            }

            "MOTD" -> {
                val info = expansion.servers[`in`.readUTF()]
                if (info != null) info.motd = `in`.readUTF()
            }
        }
    }
}

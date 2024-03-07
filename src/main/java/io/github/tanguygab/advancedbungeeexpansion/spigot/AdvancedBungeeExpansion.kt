package io.github.tanguygab.advancedbungeeexpansion.spigot

import io.github.tanguygab.advancedbungeeexpansion.ServerInfo
import lombok.Getter
import lombok.experimental.Accessors
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.clip.placeholderapi.expansion.Taskable
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.function.Consumer
import javax.annotation.Nonnull

class AdvancedBungeeExpansion : PlaceholderExpansion(), Taskable {
    @JvmField
    val CHANNEL: String = "advancedbungeejp:channel"
    @JvmField
    val servers: MutableMap<String?, ServerInfo> = HashMap()
    @JvmField
    var currentServer: ServerInfo? = null
    @JvmField
    var loaded: Boolean = false

    override fun getIdentifier() = "advancedbungee"

    override fun getAuthor() = "Tanguygab"

    override fun getVersion() = "1.0.2"

    @Getter
    @Accessors(fluent = true)
    private val canRegister = Bukkit.getServer().spigot().config.getBoolean("settings.bungeecord", false)

    override fun start() {
        loaded = false
        Bukkit.getServer().messenger.registerIncomingPluginChannel(placeholderAPI, CHANNEL, SpigotListener(this))
    }

    override fun stop() {
        loaded = false
        Bukkit.getServer().messenger.unregisterIncomingPluginChannel(placeholderAPI, CHANNEL)
        servers.clear()
    }

    override fun onRequest(player: OfflinePlayer, @Nonnull params: String): String? {
        if (!loaded) return "ロード中..."

        val args = params.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val serverArgs = args[0].split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val category = serverArgs[0]
        val serverName = if (serverArgs.size > 1) serverArgs[1] else null
        val result = if (args.size > 1) args[1] else "name"

        return when (category) {
            "servers" -> when (result) {
                "name" -> java.lang.String.join(", ", servers.keys)
                "count" -> servers.size.toString()
                "playercount" -> {
                    var i = 0
                    for (server in servers.values) i += server.playerCount
                    i.toString()
                }

                "players" -> {
                    val players: MutableList<String> = ArrayList()
                    servers.values.forEach(Consumer { server: ServerInfo -> players.addAll(server.players ?: listOf()) })
                    java.lang.String.join(", ", players)
                }

                else -> null
            }!!

            "current" -> getServerInfo(currentServer, result)
            "server" -> getServerInfo(servers[serverName], result)
            else -> null
        }
    }

    private fun getServerInfo(server: ServerInfo?, result: String): String {
        if (server == null) return "不明"
        return when (result) {
            "playercount" -> server.playerCount.toString()
            "players" -> java.lang.String.join(", ", server.players)
            "status" -> ChatColor.COLOR_CHAR.toString() + (if (server.status) "aオンライン" else "cオフライン")
            "motd" -> server.motd ?: ""
            else -> server.name
        }
    }
}

package io.github.tanguygab.advancedbungeeexpansion.bungee

import com.google.common.io.ByteStreams
import io.github.tanguygab.advancedbungeeexpansion.ServerInfo
import net.md_5.bungee.api.ServerPing
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Plugin
import java.util.concurrent.TimeUnit

class AdvancedBungeeExpansionBridge : Plugin() {
    protected val CHANNEL: String = "advancedbungeejp:channel"
    private val loadedServers: MutableList<String> = ArrayList()
    private val servers: MutableMap<String, ServerInfo> = HashMap()
    private var listener: BungeeListener? = null

    override fun onEnable() {
        proxy.servers.forEach { (server: String, info: net.md_5.bungee.api.config.ServerInfo) ->
            val serverInfo = ServerInfo(server, playersGetNames(info.players))
            info.ping() { result: ServerPing, error: Throwable? ->
                serverInfo.status = (error == null)
                if (error != null) serverInfo.motd = result.descriptionComponent.toPlainText()
            }
            servers[server] = serverInfo
        }
        proxy.registerChannel(CHANNEL)
        proxy.servers.forEach { (server: String?, info: net.md_5.bungee.api.config.ServerInfo) -> loadServer(info) }

        proxy.pluginManager.registerListener(this, BungeeListener(this).also { listener = it })
        proxy.scheduler.schedule(
            this, {
                proxy.servers.forEach { (server: String, info: net.md_5.bungee.api.config.ServerInfo) ->
                    if (!servers.containsKey(server)) servers[server] =
                        ServerInfo(server, playersGetNames(info.players))
                    info.ping { result: ServerPing, error: Throwable? ->
                        updateStatus(server, error == null)
                        if (error != null) updateMotd(server, result.descriptionComponent.toPlainText())
                    }
                }
            },
            0, 10, TimeUnit.SECONDS
        )
    }

    override fun onDisable() {
        val out = ByteStreams.newDataOutput()
        out.writeUTF("Unload")
        proxy.servers.forEach { (server: String?, info: net.md_5.bungee.api.config.ServerInfo) ->
            info.sendData(
                CHANNEL,
                out.toByteArray()
            )
        }
        proxy.unregisterChannel(CHANNEL)
        proxy.pluginManager.unregisterListener(listener)
        proxy.scheduler.cancel(this)
        servers.clear()
        loadedServers.clear()
    }

    private fun playersGetNames(collection: Collection<ProxiedPlayer>): List<String> {
        return collection.stream().map { obj: ProxiedPlayer -> obj.name }.toList()
    }

    protected fun loadServer(server: net.md_5.bungee.api.config.ServerInfo) {
        val out = ByteStreams.newDataOutput()
        out.writeUTF("Load")
        out.writeUTF(server.name)
        servers.forEach { (target: String, info: ServerInfo) ->
            out.writeUTF(
                target + "|" + info.status + "|" + info.motd + "|" + java.lang.String.join(
                    ",",
                    info.players
                )
            )
        }
        out.writeUTF("End")
        val loaded = server.sendData(CHANNEL, out.toByteArray(), false)
        if (loaded) loadedServers.add(server.name)
    }

    fun updatePlayers(server: net.md_5.bungee.api.config.ServerInfo?) {
        if (server == null) return

        if (server.players.isNotEmpty() && !loadedServers.contains(server.name)) updateStatus(server.name, true)

        val info = servers[server.name]
        info!!.players = playersGetNames(server.players)

        val out = ByteStreams.newDataOutput()
        out.writeUTF("Players")
        out.writeUTF(info.name)
        out.writeUTF(java.lang.String.join(",", info.players))
        proxy.servers.forEach { (target: String?, info0: net.md_5.bungee.api.config.ServerInfo) ->
            info0.sendData(
                CHANNEL,
                out.toByteArray()
            )
        }
    }

    private fun updateStatus(server: String, status: Boolean) {
        if (status) loadServer(proxy.getServerInfo(server))
        else loadedServers.remove(server)

        val info = servers[server]
        if (info!!.status == status) return
        info.status = status

        val out = ByteStreams.newDataOutput()
        out.writeUTF("Status")
        out.writeUTF(server)
        out.writeBoolean(status)
        proxy.servers.forEach { (target: String?, info0: net.md_5.bungee.api.config.ServerInfo) ->
            info0.sendData(
                CHANNEL,
                out.toByteArray()
            )
        }
    }

    private fun updateMotd(server: String, motd: String) {
        val info = servers[server]
        if (info == null || info.motd == motd) return
        info.motd = motd

        val out = ByteStreams.newDataOutput()
        out.writeUTF("MOTD")
        out.writeUTF(server)
        out.writeUTF(motd)
        proxy.servers.forEach { (target: String?, info0: net.md_5.bungee.api.config.ServerInfo) ->
            info0.sendData(
                CHANNEL,
                out.toByteArray()
            )
        }
    }
}

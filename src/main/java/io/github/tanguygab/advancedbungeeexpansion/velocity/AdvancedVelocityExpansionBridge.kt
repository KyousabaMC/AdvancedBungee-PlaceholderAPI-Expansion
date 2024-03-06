package io.github.tanguygab.advancedbungeeexpansion.velocity

import com.google.common.io.ByteStreams
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.api.proxy.server.ServerPing
import com.velocitypowered.api.scheduler.ScheduledTask
import io.github.tanguygab.advancedbungeeexpansion.ServerInfo
import org.slf4j.Logger
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

@Plugin(
    id = "advancedvelocityexpansionbridge",
    name = "AdvancedVelocityExpansionBridge",
    version = "1.0.2",
    url = "https://kyousaba.net",
    description = "Bridge forwarding server status to Spigot servers",
    authors = ["KyousabaMC"]
)
class AdvancedVelocityExpansionBridge @Inject constructor(
    private val proxy: ProxyServer,
    private val logger: Logger
) {
    private val CHANNEL: MinecraftChannelIdentifier = MinecraftChannelIdentifier.from("advancedbungeejp:channel")
    private val loadedServers: MutableList<String> = ArrayList()
    private val servers: MutableMap<String, ServerInfo> = HashMap()
    private var listener: VelocityListener? = null
    private var repeatTask: ScheduledTask? = null

    @Subscribe
    fun onInitialize(event: ProxyInitializeEvent?) {
        proxy.allServers.forEach(Consumer { sv: RegisteredServer ->
            val serverInfo = ServerInfo(sv.serverInfo.name, playersGetNames(sv.playersConnected))
            var result: ServerPing? = null
            try {
                result = sv.ping().get()
            } catch (ignore: ExecutionException) {
            } catch (ignore: InterruptedException) {
            }

            serverInfo.status = (result == null)
            if (result != null) {
                val motd: String = result.descriptionComponent.insertion() ?: ""
                updateMotd(sv.serverInfo.name, motd)
            }
            servers[sv.serverInfo.name] = serverInfo
        })

        proxy.channelRegistrar.register(CHANNEL)

        proxy.allServers.forEach { loadServer(it) }

        listener = VelocityListener(this)
        proxy.eventManager.register(this, listener)
        repeatTask = proxy.scheduler.buildTask(this, Runnable {
            proxy.allServers.forEach(
                Consumer { sv: RegisteredServer ->
                    val serverInfo = ServerInfo(sv.serverInfo.name, playersGetNames(sv.playersConnected))
                    if (!servers.containsKey(serverInfo.name)) servers[serverInfo.name ?: ""] =
                        ServerInfo(serverInfo.name, playersGetNames(sv.playersConnected))

                    var result: ServerPing? = null
                    try {
                        result = sv.ping().get()
                    } catch (ignore: ExecutionException) {
                    } catch (ignore: InterruptedException) {
                    }

                    serverInfo.status = (result != null)
                    if (result != null) {
                        val motd: String = result!!.descriptionComponent.insertion() ?: ""
                        loadServer(sv)
                        updateMotd(sv.serverInfo.name, motd)
                    }
                    servers[sv.serverInfo.name] = serverInfo
                })
        })
            .repeat(10, TimeUnit.SECONDS)
            .schedule()

        logger.info("Hello there! I made my first plugin with Velocity.")
    }

    fun onDisable() {
        val out = ByteStreams.newDataOutput()
        out.writeUTF("Unload")
        proxy.allServers.forEach(Consumer { server: RegisteredServer ->
            server.sendPluginMessage(
                CHANNEL,
                out.toByteArray()
            )
        })
        proxy.channelRegistrar.unregister(CHANNEL)
        proxy.eventManager.unregisterListener(this, listener)
        repeatTask!!.cancel()

        servers.clear()
        loadedServers.clear()
    }

    private fun playersGetNames(collection: Collection<Player>): List<String> {
        return collection.stream().map { obj: Player -> obj.username }.toList()
    }

    private fun loadServer(serverName: String) {
        val server = proxy.getServer(serverName)
        if (server.isEmpty) return
        loadServer(server.get())
    }

    private fun loadServer(server: RegisteredServer) {
        val out = ByteStreams.newDataOutput()
        out.writeUTF("Load")
        out.writeUTF(server.serverInfo.name)
        servers.forEach { (target: String, info: ServerInfo) ->
            out.writeUTF(
                target + "|" + info.status + "|" + info.motd + "|" + java.lang.String.join(
                    ",",
                    info.players
                )
            )
        }
        out.writeUTF("End")
        val loaded = server.sendPluginMessage(CHANNEL, out.toByteArray())
        if (loaded) loadedServers.add(server.serverInfo.name)
    }

    fun updatePlayers(server: RegisteredServer?) {
        if (server == null) return

        if (!server.playersConnected.isEmpty() && !loadedServers.contains(server.serverInfo.name)) updateStatus(
            server.serverInfo.name,
            true
        )

        val info = servers[server.serverInfo.name]
        info!!.players = playersGetNames(server.playersConnected)

        val out = ByteStreams.newDataOutput()
        out.writeUTF("Players")
        out.writeUTF(info.name ?: "")
        out.writeUTF(java.lang.String.join(",", info.players))
        proxy.allServers.forEach(Consumer { target: RegisteredServer ->
            target.sendPluginMessage(
                CHANNEL,
                out.toByteArray()
            )
        })
    }

    private fun updateStatus(serverName: String, status: Boolean) {
        val server = proxy.getServer(serverName)
        if (server.isEmpty) return

        if (status) loadServer(server.get())
        else loadedServers.remove(serverName)

        val info = servers[serverName]
        if (info!!.status == status) return
        info.status = status

        val out = ByteStreams.newDataOutput()
        out.writeUTF("Status")
        out.writeUTF(serverName)
        out.writeBoolean(status)
        proxy.allServers.forEach(Consumer { target: RegisteredServer ->
            target.sendPluginMessage(
                CHANNEL,
                out.toByteArray()
            )
        })
    }

    private fun updateMotd(server: String, motd: String) {
        val info = servers[server]
        if (info == null || info.motd == motd) return
        info.motd = motd

        val out = ByteStreams.newDataOutput()
        out.writeUTF("MOTD")
        out.writeUTF(server)
        out.writeUTF(motd)
        proxy.allServers.forEach(Consumer { target: RegisteredServer ->
            target.sendPluginMessage(
                CHANNEL,
                out.toByteArray()
            )
        })
    }
}

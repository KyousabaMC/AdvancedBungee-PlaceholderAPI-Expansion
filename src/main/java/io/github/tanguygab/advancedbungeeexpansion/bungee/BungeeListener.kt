package io.github.tanguygab.advancedbungeeexpansion.bungee

import net.md_5.bungee.api.event.ServerDisconnectEvent
import net.md_5.bungee.api.event.ServerSwitchEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class BungeeListener(private val plugin: AdvancedBungeeExpansionBridge) : Listener {
    @EventHandler
    fun on(e: ServerSwitchEvent) {
        plugin.updatePlayers(e.player.server.info)
    }

    @EventHandler
    fun on(e: ServerDisconnectEvent) {
        plugin.updatePlayers(e.target)
    }
}

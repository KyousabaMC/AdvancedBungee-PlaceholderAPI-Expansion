package io.github.tanguygab.advancedbungeeexpansion.velocity

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent

class VelocityListener(private val instance: AdvancedVelocityExpansionBridge) {
    @Subscribe
    fun onServerConnected(e: ServerConnectedEvent) {
        instance.updatePlayers(e.server)

        if (e.previousServer.isPresent) {
            instance.updatePlayers(e.previousServer.get())
        }
    }

    @Subscribe
    fun onProxyShutdown(e: ProxyShutdownEvent?) {
        instance.onDisable()
    }
}

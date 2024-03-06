package io.github.tanguygab.advancedbungeeexpansion.bungee;

import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public record BungeeListener(AdvancedBungeeExpansionBridge plugin) implements Listener {

    @EventHandler
    public void on(ServerSwitchEvent e) {
        plugin.updatePlayers(e.getPlayer().getServer().getInfo());
    }

    @EventHandler
    public void on(ServerDisconnectEvent e) {
        plugin.updatePlayers(e.getTarget());
    }

}

package io.github.tanguygab.advancedbungeeexpansion.bungee;

import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeListener implements Listener {

    private final AdvancedBungeeExpansionBridge plugin;

    public BungeeListener(AdvancedBungeeExpansionBridge plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void on(ServerSwitchEvent e) {
        plugin.updatePlayers(e.getPlayer().getServer().getInfo());
    }

    @EventHandler
    public void on(ServerDisconnectEvent e) {
        plugin.updatePlayers(e.getTarget());
    }

}

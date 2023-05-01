package io.github.tanguygab.advancedbungeeexpansion;

import java.util.List;

public class ServerInfo {

    private final String name;
    private boolean status;
    private List<String> players;

    public ServerInfo(String name, List<String> players) {
        this(name,false,players);
    }
    public ServerInfo(String name, boolean status, List<String> players) {
        this.name = name;
        this.status = status;
        this.players = players;
    }

    public String getName() {
        return name;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }

    public int getPlayerCount() {
        return players.size();
    }
}

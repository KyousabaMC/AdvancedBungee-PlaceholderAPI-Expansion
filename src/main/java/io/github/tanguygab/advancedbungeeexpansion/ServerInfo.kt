package io.github.tanguygab.advancedbungeeexpansion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
public class ServerInfo {

    @Getter private final String name;
    @Getter @Setter private boolean status;
    @Getter @Setter private String motd;
    @Getter @Setter private List<String> players;

    public ServerInfo(String name, List<String> players) {
        this(name,false,"No MOTD",players);
    }

    public int getPlayerCount() {
        return players.size();
    }
}

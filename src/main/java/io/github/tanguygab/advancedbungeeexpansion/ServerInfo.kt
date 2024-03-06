package io.github.tanguygab.advancedbungeeexpansion

import lombok.AllArgsConstructor
import lombok.Getter
import lombok.Setter

@Getter
@AllArgsConstructor
class ServerInfo {
    var name: String = ""
    var status = false
    var motd: String? = null
    var players: List<String>? = null

    constructor(name: String, status: Boolean, motd: String, players: List<String>) {
        this.name = name
        this.status = status
        this.motd = motd
        this.players = players
    }

    constructor(name: String, players: List<String>) {
        this.name = name ?: ""
        this.status = false
        this.motd = "No MOTD"
        this.players = players

    }

    val playerCount: Int
        get() = players!!.size
}

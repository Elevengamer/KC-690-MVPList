package de.elevengamer.utils

import de.elevengamer.classes.Player
import java.io.File

object CsvUtil {
    fun getPlayers(): List<Player> {
        val players = mutableListOf<Player>()
        val file = File("src/main/resources/csv/players.csv")
        if (file.exists()) {
            file.readLines().forEach {
                if (!it.startsWith("id")) { // skip header
                    val parts = it.split(",")
                    if (parts.size == 2) {
                        players.add(Player(parts[0], parts[1]))
                    }
                }
            }
        }
        return players
    }
}

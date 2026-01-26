package de.elevengamer.utils

import de.elevengamer.classes.Player
import java.io.File

object CsvUtil {
    fun getPlayers(): List<Player> {
        val players = mutableListOf<Player>()
        val file = File("src/main/resources/csv/players.csv")
        if (file.exists()) {
            file.readLines().forEach {
                if (it != "name") { // skip header
                    players.add(Player(it))
                }
            }
        }
        return players
    }
}

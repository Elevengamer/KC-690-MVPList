package de.elevengamer.classes

import com.google.gson.Gson
import java.io.File
import java.io.Serializable

class MvpRun(
    val runId: Int,
    val players: MutableList<PlayerMvpState>
): Serializable{
    companion object {
        fun create(runId: Int, playerList: List<Player>): MvpRun {
            val playerMvpStates = playerList.map { PlayerMvpState(it.name) }.toMutableList()
            return MvpRun(runId, playerMvpStates)
        }

        fun loadMvpRuns(filePath: String): MutableList<MvpRun> {
            val file = File(filePath)
            return if (file.exists()) {
                val json = file.readText()
                Gson().fromJson(json, Array<MvpRun>::class.java).toMutableList()
            } else {
                mutableListOf()
            }
        }

        fun saveMvpRuns(filePath: String, mvpRuns: List<MvpRun>) {
            val json = Gson().toJson(mvpRuns)
            File(filePath).writeText(json)
        }
    }

    fun assignMvp(playerName: String, event: Event, rank: Int, eventDate: String){
        players.forEach { p ->
            if(p.playerName==playerName && p.hasMvp==false && p.isMvpBanned==false){
                p.event = event
                p.hasMvp = true
                p.rank = rank
                p.eventDate = eventDate
            }else if (p.playerName==playerName && p.hasMvp){
                throw IllegalStateException("Player already has an MVP ${p.event!!.name} with Rank: ${p.rank}")
            }else if (p.playerName==playerName && p.isMvpBanned){
                throw IllegalStateException("Player is currently banned from MVPs. Reason: ${p.reasonForBan}")
            }
        }
    }
    fun setPlayerBanned(playerName: String, reasonForBan: String){
        players.forEach { p ->
            if(p.isMvpBanned){
                throw IllegalStateException("Player already is MVP Banned")
            }

            if (p.playerName == playerName){
                p.isMvpBanned = true
                p.reasonForBan = reasonForBan
            }
        }
    }
    fun removeBan(playerName: String){
        players.forEach { p ->
            if (p.playerName == playerName){
                p.isMvpBanned=false
                p.reasonForBan=null
            }
        }
    }

    fun removeMvp(playerName: String) {
        players.find { it.playerName == playerName }?.let {
            if (it.hasMvp) {
                it.hasMvp = false
                it.event = null
                it.rank = null
                it.eventDate = null
            } else {
                throw IllegalStateException("Player does not have an MVP to remove.")
            }
        }
    }

}



data class PlayerMvpState(
    val playerName: String,
    var event: Event ? = null,
    var rank: Int ? = null,
    var eventDate: String ? = null,
    var hasMvp: Boolean = false,
    var isMvpBanned:Boolean = false,
    var reasonForBan: String ?=null
): Serializable
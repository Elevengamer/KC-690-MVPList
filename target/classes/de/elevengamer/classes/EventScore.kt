package de.elevengamer.classes;

import java.util.ArrayList

class EventScore (
    val event:Event,
    val date: String,
    ){
    data class ps(val playername:String, val score:String, val rank:Int)
    val scores: MutableList<ps> = ArrayList<ps>()

    fun addScore(score:ps){
        scores.add(score)
    }
    fun removeScore(playername: String){
        scores.forEach { score ->
            if(score.playername == playername){
                scores.remove(score)
            }
        }
    }
}
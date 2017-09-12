package com.example.android.genkishiken

import java.io.Serializable

/**
 * Created by Nathan Henninger on 2017.08.23.
 * https://github.com/nhenninger
 * nathanhenninger@u.boisestate.edu
 */
data class Question(val mCorrectCard: Card,
                    val mIncorrectCards: ArrayList<Card>,
                    val mShuffledCards: ArrayList<Card>) : Serializable {
}
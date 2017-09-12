package com.example.android.genkishiken

import android.support.v4.app.Fragment
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView

/**
 * Created by Nathan Henninger on 2017.08.23.
 * https://github.com/nhenninger
 * nathanhenninger@u.boisestate.edu
 */
@Deprecated("I don't seem to need this") class QuestionFragment : Fragment() {
    companion object {
        private const val TAG = "QuestionFragment"
    }

    lateinit private var mQuestion: Question
    lateinit private var mTvCharacter: TextView
    lateinit private var mRadioGroup: RadioGroup
    lateinit private var mUserSelection: RadioButton

}
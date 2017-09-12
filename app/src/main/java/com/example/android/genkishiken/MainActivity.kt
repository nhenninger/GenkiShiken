package com.example.android.genkishiken

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment

/**
 * Created by Nathan Henninger on 2017.08.05.
 * https://github.com/nhenninger
 * nathanhenninger@u.boisestate.edu
 */
class MainActivity : SingleFragmentActivity() {
    override fun createFragment(): Fragment = GenkiShikenFragment.newInstance()
}

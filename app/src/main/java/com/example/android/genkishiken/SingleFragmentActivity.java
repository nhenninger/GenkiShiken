package com.example.android.genkishiken;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Nathan Henninger on 2016.11.28.
 * https://github.com/nhenninger
 * nathanhenninger@u.boisestate.edu
 */

/**
 * Abstraction of repeated code.
 *
 * @see <a href="https://www.bignerdranch.com/we-write/android-programming/">
 * Android Programming: The Big Nerd Ranch Guide (2nd Edition)</a>
 * @since Chapter 23.
 */
public abstract class SingleFragmentActivity extends AppCompatActivity {

    /**
     * @return A new Fragment.
     * @since Chapter 23.
     */
    protected abstract Fragment createFragment();

    /**
     * @since Chapter 23.
     */
    @LayoutRes
    int getLayoutResId() {
        return R.layout.activity_single_fragment;
    }

    /**
     * {@inheritDoc}
     *
     * @since Chapter 23.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.single_fragment_container);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.single_fragment_container, fragment)
                    .commit();
        }
    }
}

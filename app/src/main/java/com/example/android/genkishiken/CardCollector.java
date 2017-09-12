package com.example.android.genkishiken;

/**
 * Created by Nathan Henninger on 2017.07.19.
 * https://github.com/nhenninger
 * nathanhenninger@u.boisestate.edu
 */

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages Lesson assets.
 * <p>
 * Analagous to BeatBox in BeatBox and FlickrFetchr in PhotoGallery.
 */
public class CardCollector {
    private static final String TAG = "CardCollector";
    private static final String LESSONS_FOLDER = "lessons";

    private AssetManager mAssetManager;
    private List<Lesson> mLessons = new ArrayList<>();

    public CardCollector(Context context) {
        mAssetManager = context.getAssets();
        loadLessons();
    }

    /**
     * Loads all the lesson data from files in the AssetManager into an
     * ArrayList.
     */
    private void loadLessons() {
        String[] lessonNames;
        try {
            lessonNames = mAssetManager.list(LESSONS_FOLDER);
            Log.i(TAG, "Found " + lessonNames.length + " lessons");
        } catch (IOException ioe) {
            Log.e(TAG, "loadLessons: Could not list assets", ioe);
            return;
        }

        for (String filename : lessonNames) {
            String assetPath = LESSONS_FOLDER + "/" + filename;
            try {
                InputStream inputStream = mAssetManager.open(assetPath);
                BufferedReader buff = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder sb = new StringBuilder();
                String line = buff.readLine();

                while (line != null) {
                    sb.append(line);
                    line = buff.readLine();
                }

                Lesson lesson = new Lesson(assetPath, sb.toString());
                mLessons.add(lesson);
//                Log.i(TAG, "loadLessons: " + lesson.getRawJson());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Lesson> getLessons() {
        return mLessons;
    }

    /**
     * Returns the lesson specified by the lessonNum parameter.
     *
     * @param lessonNum The number of the lesson, between 1 and 23 inclusive.
     * @return The Lesson object.
     * @throws IllegalArgumentException If lesson not between 1 and 23 inclusive.
     */
    public Lesson getLesson(int lessonNum) {
        if (lessonNum <= 0 || lessonNum > 23) {
            throw new IllegalArgumentException("Lessons must be between 1 and 23");
        }
        for (Lesson l : mLessons) {
            if (l.getName().equals("lesson" + lessonNum)) {
                return l;
            }
        }
        // Should never get here.
        return null;
    }
}

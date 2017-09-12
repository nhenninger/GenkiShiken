package com.example.android.genkishiken;

/**
 * Created by Nathan Henninger on 2017.07.19.
 * https://github.com/nhenninger
 * nathanhenninger@u.boisestate.edu
 */

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Models a lesson.
 * Analagous to Sound in BeatBox.
 */
public class Lesson {
    private static final String TAG = "Lesson";
    private String mAssetPath;
    private String mName;
    private String mRawJson;
    private ArrayList<Card> mCards;

    public Lesson(String assetPath, String rawJson) {
        mAssetPath = assetPath;
        mRawJson = rawJson;
        String[] components = assetPath.split("/");
        mName = components[components.length - 1].replace(".json", "");
        parseItems(rawJson);
    }

    public String getAssetPath() {
        return mAssetPath;
    }

    public String getName() {
        return mName;
    }

    public String getRawJson() {
        return mRawJson;
    }

    @Override
    public String toString() {
        return mName + "," +
                mAssetPath + "," +
                mRawJson;
    }

    public void setCards(ArrayList<Card> cards) {
        mCards = cards;
    }

    public ArrayList<Card> getCards() {
        return mCards;
    }

    /**
     * Parses JSON to create a Lesson object.
     *
     * @param rawJson The data directly from file.
     */
    private void parseItems(String rawJson) {
        Gson gson = new GsonBuilder().create();
        try {
            JSONObject jsonObject = new JSONObject(rawJson);
            JSONArray jsonCardSet;
            if (jsonObject.has("syllabary")) { // Lessons 1 and 2
                jsonCardSet = jsonObject.getJSONArray("syllabary");
            } else { // Lesson 3 to 23
                jsonCardSet = jsonObject.getJSONArray("kanji");
            }
            mCards = new ArrayList<>(
                    Arrays.asList(gson.fromJson(jsonCardSet.toString(), Card[].class)));
//            for (Card card: mCards) {
//                Log.i(TAG, "parseItems: " + card.toString());
//            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }
}

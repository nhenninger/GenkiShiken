package com.example.android.genkishiken;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Nathan Henninger on 2017.05.04.
 * https://github.com/nhenninger
 * nathanhenninger@u.boisestate.edu
 */

/**
 * Models a memory game card.
 * <p>
 * Analagous to GalleryItem in PhotoGallery.
 */
public class Card {
    @SerializedName("kana")
    private String mKana;
    @SerializedName("character")
    private String mCharacter;
    @SerializedName("pronunciation")
    private String mPronunciation;
    @SerializedName("on_yomi")
    private String mOnYomi;
    @SerializedName("kun_yomi")
    private String mKunYomi;
    @SerializedName("meaning")
    private String mMeaning;

    public String getKana() {
        return mKana;
    }

    public String getCharacter() {
        return mCharacter;
    }

    public String getPronunciation() {
        return mPronunciation;
    }

    public String getOnYomi() {
        return mOnYomi;
    }

    public String getKunYomi() {
        return mKunYomi;
    }

    public String getMeaning() {
        return mMeaning;
    }

    @Override
    public String toString() {
        return "Kana: " + mKana + "\n" +
                "Character: " + mCharacter + "\n" +
                "Pronunciation: " + mPronunciation + "\n" +
                "On Yomi: " + mOnYomi + "\n" +
                "Kun Yomi: " + mKunYomi + "\n" +
                "Meaning: " + mMeaning + "\n";
    }
}

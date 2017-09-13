package net.nathanhenninger.android.genkishiken

import android.content.Context

/**
 * Created by Nathan Henninger on 2017.09.12.
 * https://github.com/nhenninger
 * nathanhenninger@u.boisestate.edu
 */
enum class LessonType(private val mResId: Int) {
    KANA(R.string.pref_lesson_type_kana),
    KANJI_MEANING(R.string.pref_lesson_type_meaning),
    KANJI_READING(R.string.pref_lesson_type_reading);

    companion object {
        fun fromString(string: String, context: Context) :LessonType{
            LessonType.values()
                    .filter { string == it.asString(context) }
                    .forEach { return it }
            throw IllegalArgumentException("No constant with text " + string + "found")
        }
    }

    fun asString(context: Context) = context.getString(mResId)
}
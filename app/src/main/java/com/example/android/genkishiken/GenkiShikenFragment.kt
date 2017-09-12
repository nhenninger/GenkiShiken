package com.example.android.genkishiken

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.preference.PreferenceManager
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.*
import android.widget.*
import java.util.*

/**
 * Created by Nathan Henninger on 2017.08.11.
 * https://github.com/nhenninger
 * nathanhenninger@u.boisestate.edu
 */
class GenkiShikenFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        private const val TAG = "GenkiShikenFragment"
        private const val FIRST_LESSON = 1
        private const val NUM_MULTI_CHOICE = 5
        private const val NUM_WRONG_CHOICES = NUM_MULTI_CHOICE - 1

        fun newInstance(): GenkiShikenFragment = GenkiShikenFragment()
    }

    private var mQuestions: ArrayList<Question> = ArrayList()
    private var mViewHolders: ArrayList<QuestionViewHolder> = ArrayList()
    private var mRecyclerView: RecyclerView? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private var mResultsCard: CardView? = null
    private var mPrevButton: Button? = null
    private var mNextButton: Button? = null
    private var mFinishButton: Button? = null
    private var mRestartButton: Button? = null
    private var mLessonFocusRadGroup: RadioGroup? = null
    private var mProgressBar: ProgressBar? = null
    private var mLessonNumber = FIRST_LESSON
    private var mLessonType = LessonType.KANA
    private var mOrientation = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setHasOptionsMenu(true)
        setupSharedPreferences()
        updateItems()
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_genki_shiken, container, false)

        mRecyclerView = view?.findViewById(R.id.fragment_genkishiken_recycler_view)
        mLayoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        mRecyclerView?.layoutManager = mLayoutManager
        mRecyclerView?.addOnScrollListener(
                object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {

                        val pos = (mLayoutManager as LinearLayoutManager)
                                .findLastVisibleItemPosition()
                        if (Math.abs(dx) > resources.getInteger(R.integer.scroll_threshold)) {
                            setProgressBar(pos.plus(1))
                        }
                        when (pos) {
                            RecyclerView.NO_POSITION -> return
                            0 -> mPrevButton?.isEnabled = false
                            mQuestions.size.minus(1) -> mNextButton?.isEnabled = false
                            else -> toggleNavigationButtons(true)
                        }
                    }
                }
        )

        mResultsCard = view?.findViewById(R.id.cv_results)
        val resultsTextView = view?.findViewById<TextView>(R.id.tv_results)

        mPrevButton = view?.findViewById(R.id.bt_prev)
        mPrevButton?.setOnClickListener({
            scrollByOne(getString(R.string.prev_key))
        })
        mNextButton = view?.findViewById(R.id.bt_next)
        mNextButton?.setOnClickListener({
            scrollByOne(getString(R.string.next_key))
        })
        mFinishButton = view?.findViewById(R.id.bt_finish)
        mFinishButton?.setOnClickListener({
            endQuiz(mRecyclerView, mResultsCard, resultsTextView, mQuestions.size)
        })
        mRestartButton = view?.findViewById(R.id.bt_restart)
        mRestartButton?.setOnClickListener({
            restartQuiz(mRecyclerView, mResultsCard)
        })

        mLessonFocusRadGroup = view?.findViewById(R.id.rg_lesson_focus)
        val meaning: RadioButton? = view?.findViewById(R.id.rb_meaning)
        toggleMeaningRadioButtons(mLessonType != LessonType.KANA)
        mLessonFocusRadGroup?.setOnCheckedChangeListener { _, _ ->
            mLessonType =
                    if (mLessonFocusRadGroup?.checkedRadioButtonId == meaning?.id) {
                        LessonType.KANJI_MEANING
                    } else {
                        LessonType.KANJI_READING
                    }
            toggleRadioButtonText()
        }

        mProgressBar = view?.findViewById(R.id.pb_question_progress)
        mProgressBar?.max = mQuestions.size

        setupRecyclerViewCacheSize(mRecyclerView, mQuestions.size)
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(mRecyclerView)
        setupOrientation(mOrientation)
        setupAdapter()
        return view
    }

    /**
     * Smoothly scrolls mRecyclerView by one in either direction.  Cannot scroll
     * past beginning or end of mRecyclerView.
     *
     * @param direction "next" to scroll forward.  "prev" to scroll backward.
     * Anything else is ignored.
     */
    private fun scrollByOne(direction: String) {
        val pos = (mLayoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        if (pos == RecyclerView.NO_POSITION) {
            return
        } else if (pos < mQuestions.size.minus(1) && direction == getString(R.string.next_key)) {
            mRecyclerView?.smoothScrollToPosition(pos + 1)
        } else if (pos > 0 && direction == getString(R.string.prev_key)) {
            mRecyclerView?.smoothScrollToPosition(pos - 1)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean =
            when (item!!.itemId) {
                R.id.action_reset -> {
                    restartQuiz(mRecyclerView, mResultsCard)
                    true
                }
                R.id.action_settings -> {
                    val i = Intent(activity, SettingsActivity::class.java)
                    startActivity(i)
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(activity)
                .unregisterOnSharedPreferenceChangeListener(this)
    }

    /**
     * Initiates the FetchLessonsTask with the current lesson.
     */
    private fun updateItems() {
        FetchLessonsTask(mLessonNumber).execute()
    }

    /**
     * Restart the quiz.  Toggles RecyclerView and ResultsCard visibility, and
     * prepares data and navigation for a new quiz of the same data.
     *
     * @param recyclerView The RecyclerView to make VISIBLE.
     * @param resultsCard The ResultsCard to make GONE.
     */
    private fun restartQuiz(recyclerView: View?, resultsCard: View?) {
        recyclerView?.visibility = View.VISIBLE
        resultsCard?.visibility = View.GONE
        toggleNavigationButtons(true)
        mViewHolders.clear()
        shuffleQuestions()
        setupAdapter()
        setProgressBar(1)
    }

    /**
     * Finishes the quiz.  Toggles RecyclerView and ResultsCard visibility,
     * disables navigation, and displays the final score to the user.
     *
     * @param recyclerView The RecyclerView to make GONE.
     * @param resultsCard The ResultsCard to make VISIBLE.
     * @param tv The TextView of where to display the score.
     * @param totalPossible The maximum possible points.
     */
    private fun endQuiz(recyclerView: View?,
                        resultsCard: View?,
                        tv: TextView?,
                        totalPossible: Int) {
        recyclerView?.visibility = View.GONE
        resultsCard?.visibility = View.VISIBLE
        toggleNavigationButtons(false)
        tv?.text = getString(R.string.results_text, finalScore(), totalPossible)
        setProgressBar(mProgressBar!!.max)
    }

    /**
     * Iterates through all stored user answers and returns the number of
     * correct responses.
     *
     * @return score The number of correct user responses.
     */
    private fun finalScore(): Int {
        return mViewHolders.indices.count {
            mViewHolders[it].getUserAnswer() ==
                    mViewHolders[it].getCorrectAnswer()
        }
    }

    /**
     * Directly sets the progress bar to the given value.  Animates to new
     * position if Build.VERSION is at least N.
     *
     * @param value The new progress bar value
     */
    private fun setProgressBar(value: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mProgressBar?.setProgress(value, true)
        } else {
            mProgressBar?.progress = value
        }
    }

    /**
     * Convenience method for Collections.shuffle on private data set.
     */
    private fun shuffleQuestions() {
        Collections.shuffle(mQuestions)
    }

    /**
     * Enables or disables the Next, Prev, and Finish buttons.
     *
     * @param toggle True to enable, false otherwise.
     */
    private fun toggleNavigationButtons(toggle: Boolean) {
        mNextButton?.isEnabled = toggle
        mPrevButton?.isEnabled = toggle
        mFinishButton?.isEnabled = toggle
    }

    /**
     * Enables or disables the RadioButtons in mLessonFocusRadGroup.
     *
     * @param toggle True to enable, false otherwise.
     */
    private fun toggleMeaningRadioButtons(toggle: Boolean) {
        for (i in 0..mLessonFocusRadGroup?.childCount!!) {
            mLessonFocusRadGroup?.getChildAt(i)?.isEnabled = toggle
        }
    }

    /**
     * Iterates through the quiz and toggles display of the meaning/reading text.
     */
    private fun toggleRadioButtonText() {
        for (item in mViewHolders) {
            item.toggleAllRadioButtonText()
        }
    }

    /**
     * Assigns the RecyclerView's adapter to a new QuestionAdapter.
     */
    private fun setupAdapter() {
        if (isAdded) {
            mRecyclerView?.adapter = QuestionAdapter(mQuestions)
        }
    }

    /**
     * Assigns the passed RecyclerView's cache size to the passed integer.
     *
     * @param rv   The RecyclerView to modify.
     * @param size The new size of the ItemViewCacheSize.
     */
    private fun setupRecyclerViewCacheSize(rv: RecyclerView?, size: Int) {
        rv?.setItemViewCacheSize(size)
    }

    /**
     * Initializes member variables to stored preference values and registers
     * this fragment as an OnSharedPreferenceChangeListener.
     */
    private fun setupSharedPreferences() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
        mLessonNumber = Integer.parseInt(preferences
                .getString(getString(R.string.pref_lesson_key),
                        getString(R.string.pref_lesson_default)))
        mLessonType =
                if (mLessonNumber == 1 || mLessonNumber == 2) {
                    LessonType.KANA
                } else if (mLessonNumber in 3..23) {
                    LessonType.KANJI_MEANING
                } else {
                    throw IllegalStateException("Lesson key must be in [1..23]")
                }
        mOrientation = preferences
                .getString(getString(R.string.pref_orientation_key),
                        getString(R.string.pref_orientation_default))
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    /**
     * Locks and unlocks the screen orientation to portrait or landscape.
     *
     * @param str Either "portrait" or "landscape" to lock the screen.  Anything
     * else will unlock it.
     */
    private fun setupOrientation(str: String) {
        when (str) {
            getString(R.string.pref_orientation_value_portrait) ->
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            getString(R.string.pref_orientation_value_landscape) ->
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else ->
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
    }

    override fun onSharedPreferenceChanged(sharedPrefs: SharedPreferences, s: String) {
        when (s) {
            getString(R.string.pref_lesson_key) -> {
                mLessonNumber = Integer.parseInt(sharedPrefs
                        .getString(getString(R.string.pref_lesson_key),
                                getString(R.string.pref_lesson_default)))
                mLessonType =
                        if (mLessonNumber == 1 || mLessonNumber == 2) {
                            LessonType.KANA
                        } else if (mLessonNumber in 3..23) {
                            LessonType.KANJI_MEANING
                        } else {
                            throw IllegalStateException("Lesson key must be in [1..23]")
                        }
                updateItems()
                toggleMeaningRadioButtons(mLessonType != LessonType.KANA)
            }
            getString(R.string.pref_orientation_key) -> {
                mOrientation = sharedPrefs.getString(
                        getString(R.string.pref_orientation_key),
                        getString(R.string.pref_orientation_default))
                setupOrientation(mOrientation)
            }
        }
    }

    /**
     * Analagous to PhotoHolder in PhotoGallery.
     */
    private inner class QuestionViewHolder(private val mItemView: View) :
            RecyclerView.ViewHolder(mItemView) {
        private var mQuestion: Question? = null
        private var mQuestionRadGroup: RadioGroup? = null
        private var mRadButtons: ArrayList<RadioButton> = ArrayList()
        private var mTvCharacter: TextView? = null

        init {
            mTvCharacter = mItemView.findViewById(R.id.tv_character)
        }

        fun bindQuestion(question: Question, position: Int) {
            mQuestion = question
            mTvCharacter?.text = if (mLessonType == LessonType.KANA) { // Lessons 1 and 2
                mQuestion?.mCorrectCard?.kana
            } else { // Lessons 3 to 23
                mQuestion?.mCorrectCard?.character
            }
            mQuestionRadGroup = mItemView.findViewById(R.id.rg_question_choices)
            while (mRadButtons.size < NUM_MULTI_CHOICE) {
                val radioButton = RadioButton(activity)
                radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, resources
                        .getInteger(R.integer.question_choice_font_size).toFloat())

                if (mLessonType == LessonType.KANA) { // Lessons 1 and 2
                    radioButton.text =
                            mQuestion?.mShuffledCards?.get(mRadButtons.size)?.pronunciation
                } else { // Lessons 3 to 23
                    toggleSingleRadioButtonText(radioButton, mRadButtons.size)
                }
                mRadButtons.add(radioButton)
                radioButton.id = View.generateViewId()
                mQuestionRadGroup?.addView(radioButton)
            }
            if (position < mViewHolders.size) {
                mViewHolders[position] = this
            } else {
                mViewHolders.add(this)
            }
            mQuestionRadGroup?.setOnCheckedChangeListener({ _: RadioGroup, _: Int ->
                scrollByOne(getString(R.string.next_key))
            })
        }

        /**
         * Toggles the text of a single RadioButton between showing the meaning
         * and the reading of a Question.
         *
         * @precondition The quiz is not testing kana, i.e. mLessonNumber is between
         * 3 and 23 inclusive.
         * @param rb The RadioButton to update text.
         * @param index The index into mShuffledCards.
         */
        fun toggleSingleRadioButtonText(rb: RadioButton, index: Int) {
            rb.text = if (mLessonType == LessonType.KANJI_MEANING) {
                mQuestion?.mShuffledCards?.get(index)?.meaning
            } else {
                mQuestion?.mShuffledCards?.get(index)?.onYomi +
                        " / " +
                        mQuestion?.mShuffledCards?.get(index)?.kunYomi
            }
        }

        /**
         * Iterates through all the RadioButtons in this ViewHolder to toggle
         * showing the meaning or reading.
         */
        fun toggleAllRadioButtonText() {
            for (i in mRadButtons.indices) {
                toggleSingleRadioButtonText(mRadButtons[i], i)
            }
        }

        /**
         * Returns the text of the user's selected RadioButton.
         *
         * @return The text of the user's selected RadioButton.
         */
        fun getUserAnswer(): String {
            val id = mQuestionRadGroup?.checkedRadioButtonId
            val button = mItemView.findViewById<RadioButton>(id!!)
            return button?.text?.toString() ?: getString(R.string.no_user_answer)
        }

        /**
         * Returns the text of the correct answer.  A string of kana, an English
         * meaning, or the reading.
         *
         * @return The text of the answer for this ViewHolder's Question.
         */
        fun getCorrectAnswer(): String {
            return if (mQuestion?.mCorrectCard?.kana != null) {
                mQuestion?.mCorrectCard?.pronunciation.toString()
            } else {
                if (mLessonType == LessonType.KANJI_MEANING) {
                    mQuestion?.mCorrectCard?.meaning.toString()
                } else {
                    mQuestion?.mCorrectCard?.onYomi +
                            " / " +
                            mQuestion?.mCorrectCard?.kunYomi
                }
            }
        }
    }

    /**
     * Analagous to PhotoAdapter in PhotoGallery.
     */
    private inner class QuestionAdapter(private val mAdapterQuestions: ArrayList<Question>)
        : RecyclerView.Adapter<QuestionViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int)
                : QuestionViewHolder {
            val layoutInflater = LayoutInflater.from(activity)
            val view = layoutInflater.inflate(R.layout.question_card, parent, false)
            return QuestionViewHolder(view)
        }

        override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
            holder.bindQuestion(mAdapterQuestions[position], position)
        }

        override fun getItemCount(): Int = mAdapterQuestions.size
    }

    /**
     * Analagous to FetchItemsTask in PhotoGallery.
     */
    private inner class FetchLessonsTask(private val mFetchNumber: Int)
        : AsyncTask<Unit, Unit, Lesson>() {

        override fun doInBackground(vararg p0: Unit?): Lesson =
                CardCollector(activity).getLesson(mFetchNumber)

        override fun onPostExecute(result: Lesson?) {
            for (i in result?.cards?.indices!!) {
                val correct = result.cards[i]
                val incorrect = ArrayList<Card>()
                // https://stackoverflow.com/questions/2380019/
                // generate-unique-random-numbers-between-1-and-100
                val rand = Random()
                do {
                    val randInt = rand.nextInt(result.cards.size)
                    var found = false
                    if (randInt == i) {
                        found = true
                    }
                    for (card in incorrect) {
                        if (card == result.cards[randInt]) {
                            found = true
                            break
                        }
                    }
                    if (!found) {
                        incorrect.add(result.cards[randInt])
                    }
                } while (incorrect.size < NUM_WRONG_CHOICES)
                val all = ArrayList<Card>()
                all.add(correct)
                all.addAll(incorrect)
                Collections.shuffle(all)
                mQuestions.add(Question(correct, incorrect, all))
            }
            setupRecyclerViewCacheSize(mRecyclerView, mQuestions.size)
            mProgressBar?.max = mQuestions.size
            shuffleQuestions()
            setupAdapter()
            toggleNavigationButtons(true)
        }
    }
}
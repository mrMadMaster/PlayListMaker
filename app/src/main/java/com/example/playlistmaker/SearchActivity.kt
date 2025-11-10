package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

    private var currentEditText: String = EDITTEXT_DEF

    private val retrofit = Retrofit.Builder()
        .baseUrl(ITUNES_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val itunesService = retrofit.create(ItunesApi::class.java)

    private lateinit var inputEditText: EditText
    private lateinit var nothingFoundLayout: LinearLayout
    private lateinit var noConnectionLayout: LinearLayout
    private lateinit var searchHistoryLayout: LinearLayout
    private lateinit var searchHistory: SearchHistory
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var recycler: RecyclerView
    private val historyList = mutableListOf<Track>()
    private val trackList = mutableListOf<Track>()
    private var isClickAllowed = true
    private val handler = Handler(Looper.getMainLooper())

    private val searchRunnable = Runnable { search() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val sharedPreferences = getSharedPreferences("track_history", MODE_PRIVATE)
        searchHistory = SearchHistory(sharedPreferences)

        val backButton = findViewById<ImageButton>(R.id.back)
        val clearButton = findViewById<ImageView>(R.id.clear)
        val clearHistoryButton = findViewById<Button>(R.id.clearHistoryButton)
        val refreshButton = findViewById<Button>(R.id.refreshButton)

        inputEditText = findViewById(R.id.searchText)
        nothingFoundLayout = findViewById(R.id.nothingFound)
        noConnectionLayout = findViewById(R.id.noConnection)
        searchHistoryLayout = findViewById(R.id.searchHistory)
        progressBar = findViewById(R.id.progressBar)

        val tracksHistoryRecycler = findViewById<RecyclerView>(R.id.tracksHistory)
        recycler = findViewById(R.id.tracks)

        trackAdapter = TrackAdapter(trackList) { track -> onTrackClick(track)}
        recycler.adapter = trackAdapter
        historyAdapter = TrackAdapter(historyList) { track -> onTrackClick(track)}
        tracksHistoryRecycler.adapter = historyAdapter

        backButton.setOnClickListener { finish() }

        clearButton.setOnClickListener {
            inputEditText.text.clear()
            clearErrors()
            trackList.clear()
            trackAdapter.notifyDataSetChanged()
            hideKeyboard()
            inputEditText.clearFocus()
        }

        clearHistoryButton.setOnClickListener {
            searchHistory.clearHistory()
            historyList.clear()
            historyAdapter.notifyDataSetChanged()
            searchHistoryLayout.visibility = View.GONE
        }

        refreshButton.setOnClickListener {
            search()
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                trackList.clear()
                trackAdapter.notifyDataSetChanged()
                search()
                true
            }
            false
        }

        history()

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // empty
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentEditText = s.toString()
                clearButton.visibility = clearButtonVisibility(s)
                searchHistoryLayout.visibility = if (historyList.isNotEmpty() && inputEditText.hasFocus() && s?.isEmpty() == true) View.VISIBLE else View.GONE
                clearErrors()
                searchDebounce()
                if (inputEditText.text.isEmpty()){
                    clearErrors()
                    trackList.clear()
                    trackAdapter.notifyDataSetChanged()
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        }
        inputEditText.addTextChangedListener(simpleTextWatcher)
        inputEditText.setOnFocusChangeListener{ _, hasFocus ->
            searchHistoryLayout.visibility = if (historyList.isNotEmpty() && hasFocus && inputEditText.text.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun search() {
        if (inputEditText.text.isNotEmpty()) {
            clearErrors()
            recycler.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            itunesService.findTrack(inputEditText.text.toString()).enqueue(object :
                Callback<TrackResponse> {
                override fun onResponse(
                    call: Call<TrackResponse>,
                    response: Response<TrackResponse>
                ) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        val results = response.body()?.results
                        if (results?.isNotEmpty() == true) {
                            trackList.clear()
                            clearErrors()
                            trackList.addAll(results)
                            trackAdapter.notifyDataSetChanged()
                            recycler.visibility = View.VISIBLE
                        } else {
                            nothingFound()
                        }
                    } else {
                        noConnectin()
                    }
                }

                override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    noConnectin()
                }
            })
        }
    }

    fun hideKeyboard(){
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    private fun noConnectin() {
        noConnectionLayout.visibility = View.VISIBLE
        nothingFoundLayout.visibility = View.GONE
        trackList.clear()
        trackAdapter.notifyDataSetChanged()
    }

    private fun nothingFound() {
        noConnectionLayout.visibility = View.GONE
        nothingFoundLayout.visibility = View.VISIBLE
        trackList.clear()
        trackAdapter.notifyDataSetChanged()
    }

    private fun clearErrors() {
        noConnectionLayout.visibility = View.GONE
        nothingFoundLayout.visibility = View.GONE
    }

    fun clearButtonVisibility(s: CharSequence?): Int {
        return if(s.isNullOrEmpty()){
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun history(){
        historyList.clear()
        historyList.addAll(searchHistory.readHistory())
        historyAdapter.notifyDataSetChanged()
    }

    private fun clickDebounce() : Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun onTrackClick(track: Track){
        if (clickDebounce()) {
            searchHistory.writeHistory(track)
            history()
            hideKeyboard()
            startActivity(
                Intent(this, AudioPlayerActivity::class.java).putExtra(
                    "TRACK",
                    track
                )
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EDITTEXT_KEY, currentEditText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentEditText = savedInstanceState.getString(EDITTEXT_KEY, currentEditText)
    }

    companion object {
        private const val EDITTEXT_KEY = "EDITTEXT_KEY"
        private const val EDITTEXT_DEF = ""
        private const val ITUNES_URL = "https://itunes.apple.com"
        private const val CLICK_DEBOUNCE_DELAY = 1000L
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}
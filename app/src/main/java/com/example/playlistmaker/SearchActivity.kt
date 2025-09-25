package com.example.playlistmaker

import android.os.Bundle
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
        .baseUrl("https://itunes.apple.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val itunesService = retrofit.create(ItunesApi::class.java)
    private lateinit var inputEditText: EditText
    private lateinit var nothingFound: LinearLayout
    private lateinit var noConnection: LinearLayout


    private val trackList = mutableListOf<Track>()
    val trackAdapter = TrackAdapter(trackList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        val backButton = findViewById<ImageButton>(R.id.back)
        val clearButton = findViewById<ImageView>(R.id.clear)
        inputEditText = findViewById(R.id.searchText)
        nothingFound = findViewById(R.id.nothingFound)
        noConnection = findViewById(R.id.noConnection)
        val refreshButton = findViewById<Button>(R.id.refreshButton)
        val recycler = findViewById<RecyclerView>(R.id.tracks)
        recycler.adapter = trackAdapter

        backButton.setOnClickListener { finish() }

        clearButton.setOnClickListener {
            inputEditText.text.clear()
            noConnection.visibility = View.GONE
            nothingFound.visibility = View.GONE
            trackList.clear()
            trackAdapter.notifyDataSetChanged()
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(inputEditText.windowToken, 0)
            inputEditText.clearFocus()
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

        refreshButton.setOnClickListener {
            search()
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // empty
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentEditText = s.toString()
                clearButton.visibility = clearButtonVisibility(s)
            }

            override fun afterTextChanged(s: Editable?) {

            }
        }
        inputEditText.addTextChangedListener(simpleTextWatcher)
    }

    private fun search() {
        if (inputEditText.text.isNotEmpty()) {
            itunesService.findTrack(inputEditText.text.toString()).enqueue(object :
                Callback<TrackResponse> {
                override fun onResponse(
                    call: Call<TrackResponse>,
                    response: Response<TrackResponse>
                ) {
                    if (response.isSuccessful) {
                        val results = response.body()?.results
                        if (results?.isNotEmpty() == true) {
                            noConnection.visibility = View.GONE
                            nothingFound.visibility = View.GONE
                            trackList.addAll(results)
                            trackAdapter.notifyDataSetChanged()
                        } else {
                            noConnection.visibility = View.GONE
                            nothingFound.visibility = View.VISIBLE
                            trackList.clear()
                            trackAdapter.notifyDataSetChanged()
                        }
                    } else {
                        noConnection.visibility = View.VISIBLE
                        nothingFound.visibility = View.GONE
                        trackList.clear()
                        trackAdapter.notifyDataSetChanged()
                    }
                }

                override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                    noConnection.visibility = View.VISIBLE
                    nothingFound.visibility = View.GONE
                    trackList.clear()
                    trackAdapter.notifyDataSetChanged()
                }
            })
        }
    }

    fun clearButtonVisibility(s: CharSequence?): Int {
        return if(s.isNullOrEmpty()){
            View.GONE
        } else {
            View.VISIBLE
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
    }
}
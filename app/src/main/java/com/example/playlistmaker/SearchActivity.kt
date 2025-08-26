package com.example.playlistmaker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SearchActivity : AppCompatActivity() {

    private var currentEditText: String = EDITTEXT_DEF

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val backButton = findViewById<ImageButton>(R.id.back)
        val clearButton = findViewById<ImageView>(R.id.clear)
        val inputEditText = findViewById<EditText>(R.id.searchText)

        backButton.setOnClickListener { finish() }

        clearButton.setOnClickListener {
            inputEditText.text.clear()
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(inputEditText.windowToken, 0)
            inputEditText.clearFocus()
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // empty
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = clearButtonVisibility(s)
            }

            override fun afterTextChanged(s: Editable?) {
                currentEditText = s.toString()
            }
        }
        inputEditText.addTextChangedListener(simpleTextWatcher)
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
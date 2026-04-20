package com.example.playlistmaker.player.ui.custom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.drawable.toBitmap
import com.example.playlistmaker.R

class PlaybackButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val STATE_PLAY = 0
        private const val STATE_PAUSE = 1
        private const val DEFAULT_SIZE_DP = 100
    }

    private var currentState = STATE_PLAY

    private var playBitmap: Bitmap? = null
    private var pauseBitmap: Bitmap? = null

    private var drawRect = RectF()

    private var listener: OnPlaybackClickListener? = null

    init {
        isClickable = true

        attrs?.let {
            context.withStyledAttributes(it, R.styleable.PlaybackButtonView) {
                val playIconRes = getResourceId(R.styleable.PlaybackButtonView_playIcon, 0)
                val pauseIconRes = getResourceId(R.styleable.PlaybackButtonView_pauseIcon, 0)

                if (playIconRes != 0) {
                    playBitmap = loadBitmapFromVector(playIconRes)
                }

                if (pauseIconRes != 0) {
                    pauseBitmap = loadBitmapFromVector(pauseIconRes)
                }
            }
        }
    }

    private fun loadBitmapFromVector(resId: Int): Bitmap? {
        return try {
            val drawable = AppCompatResources.getDrawable(context, resId)
            val width = (DEFAULT_SIZE_DP * resources.displayMetrics.density).toInt()
            val height = width
            drawable?.let {
                it.setBounds(0, 0, width, height)
                it.toBitmap(width, height, Bitmap.Config.ARGB_8888)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val defaultSize = (DEFAULT_SIZE_DP * resources.displayMetrics.density).toInt()

        val width = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(widthMeasureSpec)
            MeasureSpec.AT_MOST -> {
                val desiredSize = defaultSize
                minOf(desiredSize, MeasureSpec.getSize(widthMeasureSpec))
            }
            else -> defaultSize
        }

        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(heightMeasureSpec)
            MeasureSpec.AT_MOST -> {
                val desiredSize = defaultSize
                minOf(desiredSize, MeasureSpec.getSize(heightMeasureSpec))
            }
            else -> defaultSize
        }

        val size = minOf(width, height)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val size = minOf(w, h).toFloat()
        val left = (w - size) / 2
        val top = (h - size) / 2

        drawRect.set(left, top, left + size, top + size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val bitmapToDraw = when (currentState) {
            STATE_PLAY -> playBitmap
            STATE_PAUSE -> pauseBitmap
            else -> playBitmap
        }

        bitmapToDraw?.let { bitmap ->
            canvas.drawBitmap(bitmap, null, drawRect, null)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                return true
            }
            MotionEvent.ACTION_UP -> {
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        toggleState()
        listener?.onPlaybackClick()
        return true
    }

    fun setPlaybackState(isPlaying: Boolean) {
        currentState = if (isPlaying) STATE_PAUSE else STATE_PLAY
        invalidate()
    }

    fun toggleState() {
        currentState = if (currentState == STATE_PLAY) STATE_PAUSE else STATE_PLAY
        invalidate()
    }

    fun setOnPlaybackClickListener(listener: OnPlaybackClickListener) {
        this.listener = listener
    }

    interface OnPlaybackClickListener {
        fun onPlaybackClick()
    }
}
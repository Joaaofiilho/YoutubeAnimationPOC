package com.joaaofiilho.animationpoc

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.MediaController
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginTop
import kotlinx.android.synthetic.main.fragment_video.view.*

class YVideo @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
    ): ConstraintLayout(context, attrs, defStyleAttr) {

    var videoPath: String = ""
        set(value) {
            field = value
            video.setVideoPath(field)
        }

    var isExpanded: Boolean = true
        set(value) {
            field = value
            returnToOriginalPosition(field)
        }

    private var maxMarginStart: Float = 0F

    private var maxMarginEnd: Float = 0F

    private var maxMarginBottom: Float = 0F

    private var mediaController: MediaController

    private var layoutConfigured = false
    private var mediaControllerConfigured = false

    init {
        LayoutInflater.from(context).inflate(R.layout.fragment_video, this, true)

        mediaController = MediaController(context)

        video.setMediaController(mediaController)
        video.keepScreenOn = true
        video.start()

        video.setOnPreparedListener {
            if(!mediaControllerConfigured) {
                mediaController.setAnchorView(video)
                mediaControllerConfigured = true
            }
        }

        video.setOnTouchListener { v, event ->
            val parent = parent as ViewGroup
            val maxY = parent.height - video.height - maxMarginBottom.toInt()

            when(event.action) {
                MotionEvent.ACTION_MOVE -> {
                    mediaController.hide()
                    val startAt = 0
                    val payload = video.height / 2
                    val y = (video.marginTop + event.y - startAt).toInt()

                    Log.v("EVENT_Y", event.y.toString())

                    if(y in 1 until maxY && event.y > payload) {
                        val progress = y.toFloat() / maxY

                        val lp = video.layoutParams as LayoutParams

                        lp.setMargins(
                            (progress * maxMarginStart).toInt(),
                            (progress * maxY).toInt(),
                            (progress * maxMarginEnd).toInt(),
                            (progress * maxMarginBottom).toInt()
                        )
                        video.layoutParams = lp

                        videoDetails.alpha = 1F - progress * 1F
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val middle = parent.height / 2
                    val startAt = video.height / 2
                    val y = video.marginTop + startAt

                    isExpanded = y < middle

                    false
                }
                MotionEvent.ACTION_DOWN -> {
                    if(mediaController.isShowing) {
                        mediaController.hide()
                    } else if(isExpanded){
                        mediaController.show(0)
                    }
                    true
                }
                else -> false
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if(!layoutConfigured) {
            maxMarginStart = ((parent as ViewGroup).width / 2).toFloat()
            maxMarginEnd = 8 * resources.displayMetrics.density
            maxMarginBottom = 8 * resources.displayMetrics.density

            layoutConfigured = true
        }
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        if(!layoutConfigured) {
            mediaController.setAnchorView(video)
            layoutConfigured = true
        }
    }

    private fun returnToOriginalPosition(expand: Boolean) {
        val parent = parent as ViewGroup

        var y = video.marginTop
        val maxY = parent.height - video.height - maxMarginBottom.toInt()

        if(y > maxY) {
            y = maxY
        }

        val progress = y.toFloat() / maxY

        val endAt = if(expand) {
            0F
        } else {
            1F
        }

        val lp = video.layoutParams as LayoutParams

        ValueAnimator.ofFloat(progress, endAt).apply {
            addUpdateListener {
                val maxYY = parent.height - video.height - maxMarginBottom.toInt()
                val newProgress = it.animatedValue as Float

                lp.setMargins(
                    (newProgress * maxMarginStart).toInt(),
                    (newProgress * maxYY).toInt(),
                    (newProgress * maxMarginEnd).toInt(),
                    (newProgress * maxMarginBottom).toInt()
                )
                video.layoutParams = lp

                videoDetails.alpha = 1F - newProgress * 1F
            }

            duration = 200L

            start()
        }
    }
}
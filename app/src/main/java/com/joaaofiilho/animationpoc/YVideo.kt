package com.joaaofiilho.animationpoc

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import kotlinx.android.synthetic.main.fragment_video.view.*
import kotlin.math.absoluteValue

class YVideo @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
    ): ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val HORIZONTAL_SCROLL = 0
        private const val VERTICAL_SCROLL = 1
    }

    var videoPath: String = ""
        set(value) {
            field = value
            video.setVideoPath(field)
            show()
            video.start()
        }

    var isExpanded: Boolean = true
        set(value) {
            field = value
            returnToOriginalPosition(field)
        }

    private var startAtY: Float = 0F
    private var startAtX: Float = 0F

    /**
     * Used when the user scrolls horizontally*/
    private var shiftBy: Float = 0F

    private var newX: Float? = null
    private var oldX: Float? = null

    private var newY: Float? = null
    private var oldY: Float? = null

    private var maxMarginStart: Float = 0F

    private var maxMarginEnd: Float = 0F

    private var maxMarginBottom: Float = 0F

    private var mediaController: MediaController

    private var forceToInvertState: Float = 15F

    /**
     * The direction the scroll is going.
     * -1 - No scroll
     * 0 - Horizontal
     * 1 - Vertical */
    private var scrollDirection = -1

    private var layoutConfigured = false
    private var mediaControllerConfigured = false

    fun show() {
        isExpanded = true
        visibility = View.VISIBLE
    }

    fun dismiss() {
        video.stopPlayback()
        visibility = View.GONE
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.fragment_video, this, true)

        mediaController = MediaController(context)

        video.setMediaController(mediaController)
        video.keepScreenOn = true

        video.setOnPreparedListener {
            if(!mediaControllerConfigured) {
                mediaController.setAnchorView(video)
                mediaControllerConfigured = true
            }
        }

        video.setOnTouchListener { v, event ->
            val parent = parent as ViewGroup
            val maxX = parent.width - video.width - maxMarginEnd.toInt()
            val maxY = parent.height - video.height - maxMarginBottom.toInt()

            when(event.action) {
                MotionEvent.ACTION_MOVE -> {
                    mediaController.hide()
                    oldY = newY
                    newY = event.y

                    oldX = newX
                    newX = event.x

                    val x = (video.marginStart + event.x - startAtX).toInt()
                    val y = (video.marginTop + event.y - startAtY).toInt()

                    if(isExpanded) {
                        scrollDirection = 1
                    } else {

                        val mOldX = oldX
                        val mNewX = newX

                        val diffX = if (mOldX != null && mNewX != null) {
                            (mOldX - mNewX).absoluteValue
                        } else {
                            null
                        }

                        val mOldY = oldY
                        val mNewY = newY

                        val diffY = if (mOldY != null && mNewY != null) {
                            (mOldY - mNewY).absoluteValue
                        } else {
                            null
                        }

                        if (scrollDirection == -1) {
                            if (diffX != null && diffY != null) {
                                scrollDirection = when {
                                    diffX > diffY -> HORIZONTAL_SCROLL
                                    diffX < diffY -> VERTICAL_SCROLL
                                    else -> -1
                                }
                            }
                        }
                    }

                    //how much drag should be given to the video starts scrolling
                    var payload = if(scrollDirection == VERTICAL_SCROLL) {
                        video.height / 4
                    } else {
                        video.width / 4
                    }

                    if(!isExpanded) {
                        payload *= -1
                    }

                    Log.v("EVENT_X", "x = $x | initial = ${1 + payload} | final = $maxX")
                    Log.v("EQUAL_Y", "y = $x | maxY = $maxY")

                    if(scrollDirection == VERTICAL_SCROLL) {
                        if (y in 1 + payload until maxY + payload) {
                            val progress = (y.toFloat() - payload) / maxY

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
                    } else if(scrollDirection == HORIZONTAL_SCROLL) {
                        if (x in 1 + payload until maxX + payload) {
                            val progress = 1F - ((x.toFloat() - payload) / maxX)

                            Log.v("PROGRESS_X", progress.toString())


                            val lp = video.layoutParams as LayoutParams

                            shiftBy = progress * maxMarginStart

                            lp.setMargins(
                                (maxMarginStart - shiftBy).toInt(),
                                maxY,
                                (maxMarginEnd + shiftBy).toInt(),
                                maxMarginBottom.toInt()
                            )

                            video.layoutParams = lp

                        }
                    }

                    true
                }
                MotionEvent.ACTION_UP -> {
                    val startAt = video.height / 2

                    if(scrollDirection == VERTICAL_SCROLL) {
                        val middle = parent.height / 2
                        val y = video.marginTop + startAt

                        val mOldY = oldY
                        val mNewY = newY
                        val diff = if (mOldY != null && mNewY != null) {
                            mOldY - mNewY
                        } else {
                            null
                        }

                        isExpanded = if (diff != null && diff.absoluteValue > forceToInvertState) {
                            !isExpanded
                        } else {
                            y < middle
                        }
                    } else if(scrollDirection == HORIZONTAL_SCROLL) {
                        val middle = parent.width / 3
                        val x = video.marginStart + startAt

                        if(x < middle) {
                            dismiss()
                        } else {
                            isExpanded = false
                        }
                    }

                    oldX = null
                    oldY = null
                    oldY = null
                    newY = null
                    scrollDirection = -1
                    false
                }
                MotionEvent.ACTION_DOWN -> {
                    if(mediaController.isShowing) {
                        mediaController.hide()
                    } else if(isExpanded){
                        mediaController.show(4000)
                    }

                    startAtX = event.x
                    startAtY = event.y
                    true
                }
                else -> false
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        mediaController.setAnchorView(video)

        if(!layoutConfigured) {
            maxMarginStart = ((parent as ViewGroup).width / 2).toFloat()
            maxMarginEnd = 8 * resources.displayMetrics.density
            maxMarginBottom = 8 * resources.displayMetrics.density

            layoutConfigured = true
        }
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        if(!mediaControllerConfigured) {
            mediaController.setAnchorView(video)
            mediaControllerConfigured = true
        }
    }

    private fun returnToOriginalPosition(expand: Boolean) {
        val parent = parent as ViewGroup

        var progress = 0F
        var endAt = 0F
        val mScrollDirection = if(scrollDirection == -1) VERTICAL_SCROLL else scrollDirection

        if(mScrollDirection == VERTICAL_SCROLL) {

            var y = video.marginTop
            val maxY = parent.height - video.height - maxMarginBottom.toInt()

            if (y > maxY) {
                y = maxY
            }

            progress = y.toFloat() / maxY

            endAt = if (expand) {
                0F
            } else {
                1F
            }
        } else if(mScrollDirection == HORIZONTAL_SCROLL) {
            var x = maxMarginStart.toInt() - shiftBy.toInt()
            val maxX = parent.width - video.width - maxMarginEnd.toInt()

            if (x > maxX) {
                x = maxX
            }

            progress = x.toFloat() / maxX

            endAt = 1F
        }

        val lp = video.layoutParams as LayoutParams

        ValueAnimator.ofFloat(progress, endAt).apply {
            val maxX = parent.width - video.width - maxMarginEnd.toInt()
            val grow = shiftBy.toInt() + video.width / 4
            addUpdateListener {
                val maxY = parent.height - video.height - maxMarginBottom.toInt()
                val newProgress = it.animatedValue as Float

                if(mScrollDirection == VERTICAL_SCROLL) {
                    lp.setMargins(
                        (newProgress * maxMarginStart).toInt(),
                        (newProgress * maxY).toInt(),
                        (newProgress * maxMarginEnd).toInt(),
                        (newProgress * maxMarginBottom).toInt()
                    )
                } else if(mScrollDirection == HORIZONTAL_SCROLL) {
                    lp.setMargins(
                        (newProgress * maxX).toInt(),
                        maxY,
                        (grow + maxMarginEnd).toInt() - (newProgress * (grow) ).toInt(),
                        maxMarginBottom.toInt()
                    )
                }

                video.layoutParams = lp

                videoDetails.alpha = 1F - newProgress * 1F
            }

            if(!expand) {
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                        super.onAnimationEnd(animation, isReverse)
                        shiftBy = 0F
//                        mediaController = MediaController(video.context)
//                        mediaController.setAnchorView(video)
//                        video.setMediaController(mediaController)
                    }
                })
            }

            duration = 300L

            start()
        }
    }
}
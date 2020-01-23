package com.joaaofiilho.animationpoc

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
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
            animateToState(field)
        }

    /**
     *  How much the user dragged the video in a range of 0 to 1. */
    private var vProgress: Float = 0F
    private var hProgress: Float = 0F

    private var startAtY: Float = 0F
    private var startAtX: Float = 0F

    /***/
    private var maxX: Int = 0
    private var maxY: Int = 0

    /**
     * Used when the user scrolls horizontally*/
    private var shiftBy: Int = 0

    /**
     * Variables used to calculate the velocity the user dragged the video.
     * This is used to change the video position when the velocity is above certain value.*/
    private var newX: Float? = null
    private var oldX: Float? = null
    private var newY: Float? = null
    private var oldY: Float? = null

    /**
     * Style values to determinate where the video should end.*/
    private var endMarginEnd: Float = 0F

    private var endMarginBottom: Float = 0F

    private var mediaController: MediaController

    private var forceToInvertState: Float = 15F

    private var endVideoWidth: Int = 0
        set(value) {
            field = value
            endVideoHeight = (field * (9 / 16F)).toInt()
        }

    private var endVideoHeight: Int = 0

    /**
     * The direction the scroll is going.
     * -1 - No scroll
     * 0 - Horizontal
     * 1 - Vertical */
    private var scrollDirection = -1

    private var mediaControllerConfigured = false

    /**
     * Listeners */
    private var _trackVertical = { _: Float -> }
    private var _trackHorizontal = { _: Float -> }
    private var _onSwipeUp = {}
    private var _onSwipeDown = {}
    private var _onReleased = { _: Boolean -> }

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

        endMarginEnd = 8 * resources.displayMetrics.density
        endMarginBottom = 8 * resources.displayMetrics.density

        mediaController = MediaController(context)

        video.setMediaController(mediaController)
        video.keepScreenOn = true

        video.setOnPreparedListener {
            if (!mediaControllerConfigured) {
                mediaController.setAnchorView(video)
                mediaControllerConfigured = true
            }
        }

        trackVertical { progress ->
            val lp = video.layoutParams as LayoutParams

            lp.setMargins(
                (progress * maxX).toInt(),
                (progress * maxY).toInt(),
                (progress * endMarginEnd).toInt(),
                (progress * endMarginBottom).toInt()
            )
            video.layoutParams = lp

            videoDetails.alpha = 1F - progress * 1F
        }

        trackHorizontal { progress ->
            val lp = video.layoutParams as LayoutParams

            shiftBy = (progress * maxX).toInt()

            lp.setMargins(
                maxX - shiftBy,
                maxY,
                (endMarginEnd + shiftBy).toInt(),
                endMarginBottom.toInt()
            )

            video.layoutParams = lp
        }

        onSwipeUp {
            isExpanded = true
        }

        onSwipeDown {
            isExpanded = false
        }

        onReleased { shouldExpand ->
            isExpanded = shouldExpand
        }

        video.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    mediaController.hide()
                    oldY = newY
                    newY = event.y

                    oldX = newX
                    newX = event.x

                    val x = (video.marginStart + event.x - startAtX).toInt()
                    val y = (video.marginTop + event.y - startAtY).toInt()

                    //Discovering in which direction the user is scrolling
                    if (isExpanded) {
                        scrollDirection = VERTICAL_SCROLL
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
                    var payload = if (scrollDirection == VERTICAL_SCROLL) {
                        video.height / 4
                    } else {
                        video.width / 4
                    }

                    if (!isExpanded) {
                        payload *= -1
                    }

                    if (scrollDirection == VERTICAL_SCROLL) {
                        if (y in 1 + payload until maxY + payload) {
                            vProgress = (y.toFloat() - payload) / maxY
                            _trackVertical(vProgress)
                        }
                    } else if (scrollDirection == HORIZONTAL_SCROLL) {
                        if (x in 1 + payload until maxX + payload) {
                            hProgress = 1F - ((x.toFloat() - payload) / maxX)
                            _trackHorizontal(hProgress)
                        }
                    }

                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (scrollDirection == VERTICAL_SCROLL) {
                        val startAt = video.height / 2
                        val y = video.marginTop + startAt

                        val mOldY = oldY
                        val mNewY = newY
                        val diff = if (mOldY != null && mNewY != null) {
                            mOldY - mNewY
                        } else {
                            null
                        }

                        if (diff != null && diff.absoluteValue > forceToInvertState) {
                            if (isExpanded) {
                                _onSwipeDown()
                            } else {
                                _onSwipeUp()
                            }
                        } else {
                            val middle = containerVideo.height / 2
                            _onReleased(y <= middle)
                        }
                    } else if (scrollDirection == HORIZONTAL_SCROLL) {
                        val startAt = video.width / 2
                        val middle = containerVideo.width / 3
                        val x = video.marginStart + startAt

                        if (x < middle) {
                            dismiss()
                        } else {
                            _onReleased(false)
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
                    if (mediaController.isShowing) {
                        mediaController.hide()
                    } else if (isExpanded) {
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

        if (maxX <= 0) {
            endVideoWidth = containerVideo.width / 2
            maxX = containerVideo.width - endVideoWidth - endMarginEnd.toInt()
        }

        if (maxY <= 0) {
            endVideoWidth = containerVideo.width / 2
            maxY = containerVideo.height - endVideoHeight - endMarginBottom.toInt()
        }
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        if (!mediaControllerConfigured) {
            mediaController.setAnchorView(video)
            mediaControllerConfigured = true
        }
    }

    private fun trackVertical(event: (progress: Float) -> Unit) {
        _trackVertical = event
    }

    private fun trackHorizontal(event: (progress: Float) -> Unit) {
        _trackHorizontal = event
    }

    private fun onSwipeUp(event: () -> Unit) {
        _onSwipeUp = event
    }

    private fun onSwipeDown(event: () -> Unit) {
        _onSwipeDown = event
    }

    private fun onReleased(event: (shouldExpand: Boolean) -> Unit) {
        _onReleased = event
    }

    private fun animateToState(expand: Boolean) {
        val mScrollDirection = if (scrollDirection == -1) VERTICAL_SCROLL else scrollDirection
        val initialProgress = if (scrollDirection == VERTICAL_SCROLL) vProgress else 0F

        val endAt = if (expand) 0F else 1F

        val lp = video.layoutParams as LayoutParams

        ValueAnimator.ofFloat(initialProgress, endAt).apply {
            addUpdateListener {
                val progress = it.animatedValue as Float

                if (mScrollDirection == VERTICAL_SCROLL) {

                    lp.setMargins(
                        (progress * maxX).toInt(),
                        (progress * maxY).toInt(),
                        (progress * endMarginEnd).toInt(),
                        (progress * endMarginBottom).toInt()
                    )

                    videoDetails.alpha = 1F - progress * 1F
                } else {

                    lp.setMargins(
                        (maxX - shiftBy) + (progress * shiftBy).toInt(),
                        maxY,
                        (shiftBy + endMarginEnd).toInt() - (progress * shiftBy).toInt(),
                        endMarginBottom.toInt()
                    )
                }

                video.layoutParams = lp
            }

            duration = 300L

            start()
        }
    }
}
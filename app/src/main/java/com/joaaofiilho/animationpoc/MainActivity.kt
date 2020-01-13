package com.joaaofiilho.animationpoc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import kotlinx.android.synthetic.main.activity_main.*
import android.R.attr.data
import android.animation.ValueAnimator
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        video.setOnTouchListener { v, event ->
            val parent = container

            val maxMarginStart = parent.width / 2
            val maxMarginEnd = 8 * resources.displayMetrics.density
            val maxMarginBottom = 8 * resources.displayMetrics.density

            val maxY = parent.height - video.height
            val lp = video.layoutParams as ConstraintLayout.LayoutParams

            when(event.action) {
                MotionEvent.ACTION_MOVE -> {
                    var y = (video.marginTop + event.y - video.height/2).toInt()

                    if(y > maxY) {
                        y = maxY
                    }

                    val progress = y.toFloat() / maxY

                    lp.setMargins(
                        (progress * maxMarginStart).toInt(),
                        (progress * maxY).toInt(),
                        (progress * maxMarginEnd).toInt(),
                        (progress * maxMarginBottom).toInt()
                    )
                    videoDetails.alpha = 1F - progress * 1F

                    video.layoutParams = lp

                    true
                }
                MotionEvent.ACTION_UP -> {
                    val middle = parent.height / 2

                    var y = (video.marginTop + event.y - video.height/2).toInt()

                    if(y > maxY) {
                        y = maxY
                    }

                    val progress = y.toFloat() / maxY

                    val endAt = if(y + video.height / 2 > middle) {
                        1F
                    } else {
                        0F
                    }

                    ValueAnimator.ofFloat(progress, endAt).apply {
                        addUpdateListener {
                            val newProgress = it.animatedValue as Float

                            lp.setMargins(
                                (newProgress * maxMarginStart).toInt(),
                                (newProgress * maxY).toInt(),
                                (newProgress * maxMarginEnd).toInt(),
                                (newProgress * maxMarginBottom).toInt()
                            )
                            video.layoutParams = lp

                            videoDetails.alpha = 1F - newProgress * 1F
                        }

                        duration = 200L

                        start()
                    }

//                    if(video.marginTop > middle) {
//                        ConstraintSet().apply {
//                            clone(container)
//
//                            connect(video.id, ConstraintSet.TOP, container.id, ConstraintSet.TOP, 0)
//                            connect(video.id, ConstraintSet.END, container.id, ConstraintSet.END, 0)
//                            connect(video.id, ConstraintSet.START, container.id, ConstraintSet.START, 0)
//                            connect(videoDetails.id, ConstraintSet.TOP, video.id, ConstraintSet.BOTTOM, 0)
//
//                            applyTo(container)
//                        }
//                    } else {
//                        ConstraintSet().apply {
//                            clone(container)
//
//                            connect(video.id, ConstraintSet.END, container.id, ConstraintSet.END, 8)
//                            connect(video.id, ConstraintSet.BOTTOM, container.id, ConstraintSet.BOTTOM, 8)
//                            clear(video.id, ConstraintSet.START)
//                            clear(videoDetails.id, ConstraintSet.TOP)
//
//                            applyTo(container)
//                        }
//                    }
                    false
                }
                else -> true
            }
        }
//        var enabled = false
//        val previousPosition = ConstraintSet().apply { clone(container) }
//        val nextPosition = ConstraintSet().apply {
//            clone(container)
//
//            clear(video.id, ConstraintSet.BOTTOM)
//            connect(video.id, ConstraintSet.START, container.id, ConstraintSet.START, 0)
//            connect(video.id, ConstraintSet.END, container.id, ConstraintSet.END, 0)
//
//            connect(videoDetails.id, ConstraintSet.TOP, video.id, ConstraintSet.BOTTOM)
//        }
//
//        video.setOnClickListener {
//            TransitionManager.beginDelayedTransition(container)
//            if(!enabled) {
//                nextPosition.applyTo(container)
//            } else {
//                previousPosition.applyTo(container)
//            }
//        }
//
//        video.setOnDragListener { v, event ->
//            true
//        }
    }

    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun getActionBarHeight(): Int {
        val tv = TypedValue()
        return if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
        } else {
            0
        }
    }
}

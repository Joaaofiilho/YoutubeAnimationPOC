package com.joaaofiilho.animationpoc

import android.animation.ValueAnimator
import android.app.SharedElementCallback
import android.net.Uri
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.TransitionSet
import android.transition.TransitionSet.ORDERING_TOGETHER
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_video.*

class VideoFragment : Fragment() {

    companion object {
        const val EXTRA_VIDEO_PATH = "extraVideoPath"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = TransitionSet().apply {
            ordering = ORDERING_TOGETHER
            addTransition(ChangeBounds())
            addTransition(ChangeTransform())
            addTransition(ChangeImageTransform())
        }

        sharedElementReturnTransition = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val path = arguments?.getString(EXTRA_VIDEO_PATH)
        path?.let { video.setVideoURI(Uri.parse(it)) }
        video.setMediaController(MediaController(video.context))
        video.keepScreenOn = true
        video.start()

        video.setOnTouchListener { v, event ->
            video.onTouchEvent(event)
            val parent = containerVideo

            val maxMarginStart = parent.width / 2
            val maxMarginEnd = 8 * resources.displayMetrics.density
            val maxMarginBottom = 8 * resources.displayMetrics.density
            val maxY = parent.height - video.height - maxMarginBottom.toInt()

            when(event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val startAt = video.height / 2
                    var y = (video.marginTop + event.y - startAt).toInt()

                    if(y > maxY) {
                        y = maxY
                    }

                    val progress = y.toFloat() / maxY

                    val lp = video.layoutParams as ConstraintLayout.LayoutParams

                    lp.setMargins(
                        (progress * maxMarginStart).toInt(),
                        (progress * maxY).toInt(),
                        (progress * maxMarginEnd).toInt(),
                        (progress * maxMarginBottom).toInt()
                    )
                    video.layoutParams = lp

                    videoDetails.alpha = 1F - progress * 1F
                    Log.v("ACTION_MOVE", y.toString())
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val middle = parent.height / 2

                    val startAt = video.height/2
                    var y = (video.marginTop + event.y - startAt).toInt()

                    if(y > maxY) {
                        y = maxY
                    }

                    val progress = y.toFloat() / maxY

                    val endAt = if(y + video.height / 2 > middle) {
                        1F
                    } else {
                        0F
                    }

                    val lp = video.layoutParams as ConstraintLayout.LayoutParams

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
                    false
                }
                MotionEvent.ACTION_DOWN -> {
                    true
                }
                else -> false
            }
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_video, container, false)
//        view.setOnTouchListener { v, event ->
//            event.action == MotionEvent.ACTION_DOWN
//        }
        return view
    }
}

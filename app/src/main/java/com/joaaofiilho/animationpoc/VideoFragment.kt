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

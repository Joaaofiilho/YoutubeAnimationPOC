package com.joaaofiilho.animationpoc

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.joaaofiilho.animationpoc.VideoFragment.Companion.EXTRA_VIDEO_PATH
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var videosAdapter: VideosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val videoPath = "android.resource://" + packageName + "/" + R.raw.material_design

        // Pega o FragmentManager
        val fm = supportFragmentManager

        videosAdapter = VideosAdapter(
            onItemClicked = {
                // Abre uma transação e adiciona
                videoLayout.videoPath = videoPath
                videoLayout.visibility = View.VISIBLE
//                fm.beginTransaction()
//                .addSharedElement(it, "imagem_video")
//                .replace(R.id.fragmentVideo, VideoFragment().apply { arguments = Bundle().apply { putString(EXTRA_VIDEO_PATH, videoPath) } })
//                .commit()
            }
        )

        listVideos.adapter = videosAdapter
        videosAdapter.update(listOf("MATERIAL DESIGN", "MATERIAL DESIGN", "MATERIAL DESIGN", "MATERIAL DESIGN", "MATERIAL DESIGN", "MATERIAL DESIGN", "MATERIAL DESIGN", "MATERIAL DESIGN"))
    }
}

package com.tobesttwitter.videoshare

import android.net.Uri
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.tobesttwitter.videoshare.databinding.ActivityMainBinding

@UnstableApi
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var player: ExoPlayer? = null

    private var currentVideoSize: VideoSize = VideoSize.UNKNOWN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playerView.addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            val width = right - left
            val height = bottom - top
            val oldWidth = oldRight - oldLeft
            val oldHeight = oldBottom - oldTop
            if (width != oldWidth || height != oldHeight) {
                applyFraming()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun initializePlayer() {
        if (player == null) {
            val exoPlayer = ExoPlayer.Builder(this).build()
            binding.playerView.player = exoPlayer

            // Video asset path: 1-byte placeholder at res/raw/sample.mp4.
            // Note: The real video will be dropped in later.
            val rawUri = Uri.parse("android.resource://$packageName/${R.raw.sample}")
            val mediaItem = MediaItem.fromUri(rawUri)

            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.playWhenReady = true
            exoPlayer.prepare()

            DebugReadout.setup(exoPlayer, binding.debugTextView)

            exoPlayer.addListener(object : Player.Listener {
                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    currentVideoSize = videoSize
                    applyFraming()
                }
            })

            player = exoPlayer
            currentVideoSize = exoPlayer.videoSize
            applyFraming()
        }
    }

    private fun applyFraming() {
        val containerWidth = binding.playerView.width
        val containerHeight = binding.playerView.height

        val renderSize = FramingCalculator.calculateRenderSize(
            sourceWidth = currentVideoSize.width,
            sourceHeight = currentVideoSize.height,
            rotationDegrees = currentVideoSize.unappliedRotationDegrees,
            containerWidth = containerWidth,
            containerHeight = containerHeight
        )

        val surfaceView = findSurfaceView(binding.playerView)
        if (surfaceView != null && renderSize.width > 0 && renderSize.height > 0) {
            val params = surfaceView.layoutParams
            if (params.width != renderSize.width || params.height != renderSize.height) {
                params.width = renderSize.width
                params.height = renderSize.height
                if (params is FrameLayout.LayoutParams) {
                    params.gravity = android.view.Gravity.CENTER
                }
                surfaceView.layoutParams = params
                surfaceView.requestLayout()
            }
        }

        DebugReadout.updateSourceSize(
            currentVideoSize.width,
            currentVideoSize.height,
            currentVideoSize.unappliedRotationDegrees
        )
        DebugReadout.updateDimensions(
            containerWidth,
            containerHeight,
            renderSize.width,
            renderSize.height
        )
    }

    private fun findSurfaceView(view: View): SurfaceView? {
        if (view is SurfaceView) {
            return view
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = findSurfaceView(view.getChildAt(i))
                if (child != null) return child
            }
        }
        return null
    }

    private fun releasePlayer() {
        player?.let {
            it.release()
            player = null
        }
    }
}

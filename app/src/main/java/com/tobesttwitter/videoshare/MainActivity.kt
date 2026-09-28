package com.tobesttwitter.videoshare

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.tobesttwitter.videoshare.databinding.ActivityMainBinding

@UnstableApi
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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

            // Video asset path (c): 1-byte placeholder at res/raw/sample.mp4.
            // Note: The real video will be dropped in later.
            val rawUri = Uri.parse("android.resource://$packageName/${R.raw.sample}")
            val mediaItem = MediaItem.fromUri(rawUri)

            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.playWhenReady = true
            exoPlayer.prepare()

            DebugReadout.setup(exoPlayer, binding.debugTextView)
            player = exoPlayer
        }
    }

    private fun releasePlayer() {
        player?.let {
            it.release()
            player = null
        }
    }
}

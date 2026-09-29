package com.tobesttwitter.videoshare

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.tobesttwitter.videoshare.databinding.ActivityMainBinding
import java.util.Locale
import kotlin.math.hypot

@UnstableApi
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var player: ExoPlayer? = null

    private var currentVideoSize: VideoSize = VideoSize.UNKNOWN

    private var callSeconds = 0
    private val handler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            val minutes = callSeconds / 60
            val secs = callSeconds % 60
            binding.callStateTextView.text = String.format(Locale.US, "Connected %02d:%02d", minutes, secs)
            callSeconds++
            handler.postDelayed(this, 1000)
        }
    }

    private var touchDownX = 0f
    private var touchDownY = 0f
    private var isLongPressActive = false
    private var touchSlop = 0f

    private val revealRunnable = Runnable {
        revealPrank()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        touchSlop = ViewConfiguration.get(this).scaledTouchSlop.toFloat()

        enterImmersiveMode()

        binding.btnRestartPrank.setOnClickListener {
            restartPrank()
        }

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
        if (binding.revealLayout.visibility != View.VISIBLE) {
            enterImmersiveMode()
            initializePlayer()
            startCallTimer()
        }
    }

    override fun onStop() {
        super.onStop()
        cancelLongPress()
        stopCallTimer()
        releasePlayer()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (binding.revealLayout.visibility == View.VISIBLE) {
            return super.dispatchTouchEvent(ev)
        }

        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchDownX = ev.x
                touchDownY = ev.y
                isLongPressActive = true
                handler.postDelayed(revealRunnable, REVEAL_HOLD_DURATION_MS)
                binding.holdToRevealTextView.animate().alpha(1f).setDuration(300).start()
            }
            MotionEvent.ACTION_MOVE -> {
                if (isLongPressActive) {
                    val dx = ev.x - touchDownX
                    val dy = ev.y - touchDownY
                    if (hypot(dx.toDouble(), dy.toDouble()) > touchSlop) {
                        cancelLongPress()
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isLongPressActive) {
                    cancelLongPress()
                }
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    private fun cancelLongPress() {
        isLongPressActive = false
        handler.removeCallbacks(revealRunnable)
        binding.holdToRevealTextView.animate().alpha(0f).setDuration(300).start()
    }

    private fun revealPrank() {
        cancelLongPress()
        player?.pause()
        stopCallTimer()
        exitImmersiveMode()
        binding.revealLayout.visibility = View.VISIBLE
    }

    private fun restartPrank() {
        binding.revealLayout.visibility = View.GONE
        enterImmersiveMode()
        callSeconds = 0
        startCallTimer()
        if (player == null) {
            initializePlayer()
        } else {
            player?.seekTo(0)
            player?.playWhenReady = true
            player?.prepare()
        }
    }

    private fun startCallTimer() {
        handler.removeCallbacks(timerRunnable)
        handler.post(timerRunnable)
    }

    private fun stopCallTimer() {
        handler.removeCallbacks(timerRunnable)
    }

    private fun enterImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun exitImmersiveMode() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.show(WindowInsetsCompat.Type.systemBars())
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
            exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
            exoPlayer.playWhenReady = true
            exoPlayer.prepare()

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

    companion object {
        private const val REVEAL_HOLD_DURATION_MS = 3000L
    }
}

package com.tobesttwitter.videoshare

import android.widget.TextView
import androidx.media3.common.Player
import androidx.media3.common.VideoSize

/**
 * Temporary debug readout helper for displaying media source width, height, and rotation.
 * Clearly marked file for temporary debug display.
 */
object DebugReadout {
    fun setup(player: Player, textView: TextView) {
        player.addListener(object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                val text = "Source: ${videoSize.width}x${videoSize.height}, Rotation: ${videoSize.unappliedRotationDegrees}°"
                textView.text = text
            }
        })

        // Initial readout text
        val initialSize = player.videoSize
        textView.text = "Source: ${initialSize.width}x${initialSize.height}, Rotation: ${initialSize.unappliedRotationDegrees}°"
    }
}

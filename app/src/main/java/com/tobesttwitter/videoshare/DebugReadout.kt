package com.tobesttwitter.videoshare

import android.widget.TextView
import androidx.media3.common.Player
import androidx.media3.common.VideoSize

/**
 * Temporary debug readout helper for displaying media source width, height, rotation, container dimensions, and render dimensions.
 * Format: source WxH | rot | container WxH | render WxH
 */
object DebugReadout {
    private var sourceWidth = 0
    private var sourceHeight = 0
    private var rotation = 0
    private var containerWidth = 0
    private var containerHeight = 0
    private var renderWidth = 0
    private var renderHeight = 0

    private var currentTextView: TextView? = null

    fun setup(player: Player, textView: TextView) {
        currentTextView = textView
        player.addListener(object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                updateSourceSize(videoSize.width, videoSize.height, videoSize.unappliedRotationDegrees)
            }
        })

        val initialSize = player.videoSize
        updateSourceSize(initialSize.width, initialSize.height, initialSize.unappliedRotationDegrees)
    }

    fun updateSourceSize(width: Int, height: Int, rot: Int) {
        sourceWidth = width
        sourceHeight = height
        rotation = rot
        updateText()
    }

    fun updateDimensions(cWidth: Int, cHeight: Int, rWidth: Int, rHeight: Int) {
        containerWidth = cWidth
        containerHeight = cHeight
        renderWidth = rWidth
        renderHeight = rHeight
        updateText()
    }

    private fun updateText() {
        val text = "${sourceWidth}x${sourceHeight} | $rotation | ${containerWidth}x${containerHeight} | ${renderWidth}x${renderHeight}"
        currentTextView?.text = text
    }
}

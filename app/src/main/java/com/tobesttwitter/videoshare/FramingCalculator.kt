package com.tobesttwitter.videoshare

import kotlin.math.roundToInt

data class RenderSize(val width: Int, val height: Int)

object FramingCalculator {
    /**
     * Calculates render size for fit-inside strategy based on source video dimensions, rotation flag, and container dimensions.
     */
    fun calculateRenderSize(
        sourceWidth: Int,
        sourceHeight: Int,
        rotationDegrees: Int,
        containerWidth: Int,
        containerHeight: Int
    ): RenderSize {
        if (sourceWidth <= 0 || sourceHeight <= 0 || containerWidth <= 0 || containerHeight <= 0) {
            return RenderSize(0, 0)
        }

        val (effWidth, effHeight) = if (rotationDegrees == 90 || rotationDegrees == 270) {
            sourceHeight to sourceWidth
        } else {
            sourceWidth to sourceHeight
        }

        val sourceAspect = effWidth.toDouble() / effHeight.toDouble()
        val containerAspect = containerWidth.toDouble() / containerHeight.toDouble()

        return if (sourceAspect >= containerAspect) {
            val renderWidth = containerWidth
            val renderHeight = (renderWidth / sourceAspect).roundToInt()
            RenderSize(renderWidth, renderHeight)
        } else {
            val renderHeight = containerHeight
            val renderWidth = (renderHeight * sourceAspect).roundToInt()
            RenderSize(renderWidth, renderHeight)
        }
    }
}

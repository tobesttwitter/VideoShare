package com.tobesttwitter.videoshare

import org.junit.Assert.assertEquals
import org.junit.Test

class FramingCalculatorTest {

    @Test
    fun testZeroRotation_WiderThanContainer_PillarboxLetterbox() {
        // 1920x1080 (16:9 = 1.777) in 1080x1920 (9:16 = 0.5625) container
        // sourceAspect >= containerAspect -> renderWidth = 1080, renderHeight = round(1080 / (16/9)) = 608
        val result = FramingCalculator.calculateRenderSize(
            sourceWidth = 1920,
            sourceHeight = 1080,
            rotationDegrees = 0,
            containerWidth = 1080,
            containerHeight = 1920
        )
        assertEquals(RenderSize(1080, 608), result)
    }

    @Test
    fun testRotation90_SwapsDimensions_TallerThanContainer() {
        // Source 1920x1080, rotation 90. Effective source dimensions: 1080x1920 (aspect = 0.5625).
        // Container: 1080x1080 (aspect = 1.0).
        // sourceAspect (0.5625) < containerAspect (1.0) -> renderHeight = 1080, renderWidth = round(1080 * 0.5625) = 608.
        val result = FramingCalculator.calculateRenderSize(
            sourceWidth = 1920,
            sourceHeight = 1080,
            rotationDegrees = 90,
            containerWidth = 1080,
            containerHeight = 1080
        )
        assertEquals(RenderSize(608, 1080), result)
    }

    @Test
    fun testRotation270_SwapsDimensions_EqualAspectRatio() {
        // Source 1920x1080, rotation 270. Effective source: 1080x1920.
        // Container: 1080x1920.
        val result = FramingCalculator.calculateRenderSize(
            sourceWidth = 1920,
            sourceHeight = 1080,
            rotationDegrees = 270,
            containerWidth = 1080,
            containerHeight = 1920
        )
        assertEquals(RenderSize(1080, 1920), result)
    }

    @Test
    fun testRotation180_NoDimensionSwap() {
        // Source 1920x1080, rotation 180. Effective source: 1920x1080 (1.777).
        // Container 1920x1080 (1.777).
        val result = FramingCalculator.calculateRenderSize(
            sourceWidth = 1920,
            sourceHeight = 1080,
            rotationDegrees = 180,
            containerWidth = 1920,
            containerHeight = 1080
        )
        assertEquals(RenderSize(1920, 1080), result)
    }

    @Test
    fun testZeroOrNegativeDimensions_ReturnsZeroSize() {
        val result = FramingCalculator.calculateRenderSize(
            sourceWidth = 0,
            sourceHeight = 1080,
            rotationDegrees = 0,
            containerWidth = 1080,
            containerHeight = 1920
        )
        assertEquals(RenderSize(0, 0), result)
    }
}

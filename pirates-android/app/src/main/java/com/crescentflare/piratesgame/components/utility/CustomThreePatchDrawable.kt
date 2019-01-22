package com.crescentflare.piratesgame.components.utility

import android.graphics.*
import android.graphics.drawable.Drawable

/**
 * A custom drawable for drawing of simplified three patches without needing a modified image
 */
class CustomThreePatchDrawable(private val bitmap: Bitmap, private val edgeInset: Int): Drawable() {

    // ---
    // Members
    // ---

    private val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG or Paint.ANTI_ALIAS_FLAG)
    private val bitmapWidth: Int
    private val bitmapHeight: Int


    // ---
    // Initialization
    // ---

    init {
        bitmapWidth = bitmap.width
        bitmapHeight = bitmap.height
    }


    // ---
    // Properties
    // ---

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    override fun getIntrinsicWidth(): Int {
        return bitmap.width
    }

    override fun getIntrinsicHeight(): Int {
        return bitmap.height
    }


    // ---
    // Drawing
    // ---

    override fun draw(canvas: Canvas) {
        // Do nothing for invalid insets
        if (edgeInset < 0) {
            return
        }

        // Calculate patch sizes
        val width = bounds.width()
        val height = bounds.height()
        val leftPatch = Math.min(width / 2, edgeInset)
        val rightPatch = Math.min((width + 1) / 2, edgeInset)
        val centerPatch = width - leftPatch - rightPatch

        // Draw patches
        if (leftPatch > 0) {
            canvas.drawBitmap(bitmap, Rect(0, 0, leftPatch, bitmapHeight), Rect(0, 0, leftPatch, height), paint)
        }
        if (rightPatch > 0) {
            canvas.drawBitmap(bitmap, Rect(bitmapWidth - rightPatch, 0, bitmapWidth, bitmapHeight), Rect(width - rightPatch, 0, width, height), paint)
        }
        if (centerPatch > 0) {
            canvas.drawBitmap(bitmap, Rect(edgeInset, 0, bitmapWidth - edgeInset, bitmapHeight), Rect(edgeInset, 0, width - edgeInset, height), paint)
        }
    }

}

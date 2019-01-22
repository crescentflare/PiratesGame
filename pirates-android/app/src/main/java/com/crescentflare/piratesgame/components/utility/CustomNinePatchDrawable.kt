package com.crescentflare.piratesgame.components.utility

import android.graphics.*
import android.graphics.drawable.Drawable

/**
 * A custom drawable for drawing of simplified nine patches without needing a modified image
 */
class CustomNinePatchDrawable(private val bitmap: Bitmap, private val horizontalInset: Int, private val verticalInset: Int): Drawable() {

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
        if (horizontalInset < 0 || verticalInset < 0) {
            return
        }

        // Calculate patch sizes
        val width = bounds.width()
        val height = bounds.height()
        val leftPatch = Math.min(width / 2, horizontalInset)
        val rightPatch = Math.min((width + 1) / 2, horizontalInset)
        val horizontalCenterPatch = width - leftPatch - rightPatch
        val topPatch = Math.min(width / 2, verticalInset)
        val bottomPatch = Math.min((width + 1) / 2, verticalInset)
        val verticalCenterPatch = width - topPatch - bottomPatch

        // Draw patches
        if (leftPatch > 0 && topPatch > 0) {
            canvas.drawBitmap(bitmap, Rect(0, 0, leftPatch, topPatch), Rect(0, 0, leftPatch, topPatch), paint)
        }
        if (leftPatch > 0 && bottomPatch > 0) {
            canvas.drawBitmap(bitmap, Rect(0, bitmapHeight - bottomPatch, leftPatch, bitmapHeight), Rect(0, height - bottomPatch, leftPatch, height), paint)
        }
        if (rightPatch > 0 && topPatch > 0) {
            canvas.drawBitmap(bitmap, Rect(bitmapWidth - rightPatch, 0, bitmapWidth, topPatch), Rect(width - rightPatch, 0, width, topPatch), paint)
        }
        if (rightPatch > 0 && bottomPatch > 0) {
            canvas.drawBitmap(bitmap, Rect(bitmapWidth - rightPatch, bitmapHeight - bottomPatch, bitmapWidth, bitmapHeight), Rect(width - rightPatch, height - bottomPatch, width, height), paint)
        }
        if (horizontalCenterPatch > 0 && topPatch > 0) {
            canvas.drawBitmap(bitmap, Rect(horizontalInset, 0, bitmapWidth - horizontalInset, topPatch), Rect(horizontalInset, 0, width - horizontalInset, topPatch), paint)
        }
        if (horizontalCenterPatch > 0 && bottomPatch > 0) {
            canvas.drawBitmap(bitmap, Rect(horizontalInset, bitmapHeight - bottomPatch, bitmapWidth - horizontalInset, bitmapHeight), Rect(horizontalInset, height - bottomPatch, width - horizontalInset, height), paint)
        }
        if (verticalCenterPatch > 0 && leftPatch > 0) {
            canvas.drawBitmap(bitmap, Rect(0, verticalInset, leftPatch, bitmapHeight - verticalInset), Rect(0, verticalInset, leftPatch, height - verticalInset), paint)
        }
        if (verticalCenterPatch > 0 && rightPatch > 0) {
            canvas.drawBitmap(bitmap, Rect(bitmapWidth - rightPatch, verticalInset, bitmapWidth, bitmapHeight - verticalInset), Rect(width - rightPatch, verticalInset, width, height - verticalInset), paint)
        }
        if (horizontalCenterPatch > 0 && verticalCenterPatch > 0) {
            canvas.drawBitmap(bitmap, Rect(horizontalInset, verticalInset, bitmapWidth - horizontalInset, bitmapHeight - verticalInset), Rect(horizontalInset, verticalInset, width - horizontalInset, height - verticalInset), paint)
        }
    }

}

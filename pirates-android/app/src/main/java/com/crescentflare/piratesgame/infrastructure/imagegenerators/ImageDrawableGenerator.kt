package com.crescentflare.piratesgame.infrastructure.imagegenerators

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.crescentflare.jsoninflator.utility.InflatorMapUtil
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.piratesgame.infrastructure.inflator.Inflators

/**
 * Image generators: a base class to generate a new image drawable dynamically
 */
abstract class ImageDrawableGenerator {

    // --
    // Generate from attributes abstract method
    // --

    abstract fun generate(context: Context, attributes: Map<String, Any>, onDrawable: Drawable?): Drawable?


    // --
    // Canvas handling
    // --

    protected fun beginImageDrawing(width: Int? = null, height: Int? = null, horizontalGravity: Float = 0.5f, verticalGravity: Float = 0.5f, forceImageWidth: Int? = null, forceImageHeight: Int? = null, onDrawable: Drawable? = null): ImageGeneratorDrawing? {
        // Return early for invalid sizes
        val wantWidth = width ?: onDrawable?.intrinsicWidth ?: 0
        val wantHeight = height ?: onDrawable?.intrinsicHeight ?: 0
        if (wantWidth <= 0 || wantHeight <= 0) {
            return null
        }

        // Create/use bitmap
        var bitmapWidth = wantWidth
        var bitmapHeight = wantHeight
        if (onDrawable != null) {
            bitmapWidth = onDrawable.intrinsicWidth
            bitmapHeight = onDrawable.intrinsicHeight
        } else {
            if (forceImageWidth != null) {
                bitmapWidth = forceImageWidth
            }
            if (forceImageHeight != null) {
                bitmapHeight = forceImageHeight
            }
        }
        val bitmap: Bitmap?
        if (onDrawable is BitmapDrawable) {
            if (onDrawable.bitmap.isMutable) {
                bitmap = onDrawable.bitmap
            } else {
                bitmap = onDrawable.bitmap.copy(Bitmap.Config.ARGB_8888, true)
            }
        } else {
            bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        }

        // Open canvas and return
        if (bitmap != null) {
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG or Paint.ANTI_ALIAS_FLAG)
            val x = (bitmapWidth - wantWidth) * horizontalGravity
            val y = (bitmapHeight - wantHeight) * verticalGravity
            val rect = RectF(x, y, x + wantWidth, y + wantHeight)
            return ImageGeneratorDrawing(canvas, paint, bitmap, rect, !(onDrawable is BitmapDrawable))
        }
        return null
    }

    protected fun endDrawingResult(context: Context, drawing: ImageGeneratorDrawing): Drawable {
        return BitmapDrawable(context.resources, drawing.bitmap)
    }


    // --
    // Attribute helpers
    // --

    protected fun widthFromAttributes(mapUtil: InflatorMapUtil, attributes: Map<String, Any>): Int? {
        val width = mapUtil.optionalDimension(attributes, "width", 0)
        if (width <= 0) {
            return null
        }
        return width
    }

    protected fun heightFromAttributes(mapUtil: InflatorMapUtil, attributes: Map<String, Any>): Int? {
        val height = mapUtil.optionalDimension(attributes, "height", 0)
        if (height <= 0) {
            return null
        }
        return height
    }

    protected fun imageWidthFromAttributes(mapUtil: InflatorMapUtil, attributes: Map<String, Any>): Int? {
        val width = mapUtil.optionalDimension(attributes, "imageWidth", 0)
        if (width <= 0) {
            return null
        }
        return width
    }

    protected fun imageHeightFromAttributes(mapUtil: InflatorMapUtil, attributes: Map<String, Any>): Int? {
        val height = mapUtil.optionalDimension(attributes, "imageHeight", 0)
        if (height <= 0) {
            return null
        }
        return height
    }

    protected fun gravityFromAttributes(mapUtil: InflatorMapUtil, attributes: Map<String, Any>): PointF {
        return PointF(ViewletUtil.optionalHorizontalGravity(mapUtil, attributes, 0.5f), ViewletUtil.optionalVerticalGravity(mapUtil, attributes, 0.5f))
    }


    // --
    // Drawing helper class
    // --

    class ImageGeneratorDrawing(val canvas: Canvas, val paint: Paint, val bitmap: Bitmap, val drawRect: RectF, val cleanStart: Boolean) {
        // No implementation
    }

}

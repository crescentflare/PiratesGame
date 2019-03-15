package com.crescentflare.piratesgame.infrastructure.imagegenerators

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.Drawable
import com.crescentflare.viewletcreator.utility.ViewletMapUtil

/**
 * Image generators: a base class to generate a new image drawable dynamically
 */
class FilledRectGenerator: ImageDrawableGenerator() {

    fun generate(context: Context, color: Int, width: Int? = null, height: Int? = null, cornerRadius: Int = 0, horizontalGravity: Float = 0.5f, verticalGravity: Float = 0.5f, forceImageWidth: Int? = null, forceImageHeight: Int? = null, onDrawable: Drawable? = null): Drawable? {
        val drawing = beginImageDrawing(width, height, horizontalGravity, verticalGravity, forceImageWidth, forceImageHeight, onDrawable)
        if (drawing != null) {
            // Cut out shape to allow transparent overdraw without blending
            val rect = drawing.drawRect
            if (!drawing.cleanStart && (color.toLong() and 0xff000000) != 0xff000000) {
                // Determine the rectangle, shrink shape if it's not on a pixel boundary to improve edge blending
                val cutRect = RectF(rect.left, rect.top, rect.right, rect.bottom)
                if (Math.abs(cutRect.left - Math.floor(cutRect.left.toDouble())) > 0.001f) {
                    cutRect.left += 0.5f
                }
                if (Math.abs(cutRect.top - Math.floor(cutRect.top.toDouble())) > 0.001f) {
                    cutRect.top += 0.5f
                }
                if (Math.abs(cutRect.right - Math.floor(cutRect.right.toDouble())) > 0.001f) {
                    cutRect.right += 0.5f
                }
                if (Math.abs(cutRect.bottom - Math.floor(cutRect.bottom.toDouble())) > 0.001f) {
                    cutRect.bottom += 0.5f
                }

                // Clear shape
                drawing.paint.color = Color.TRANSPARENT
                drawing.paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                if (cornerRadius > 0) {
                    drawing.canvas.drawRoundRect(cutRect, cornerRadius.toFloat(), cornerRadius.toFloat(), drawing.paint)
                } else {
                    drawing.canvas.drawRect(cutRect, drawing.paint)
                }
                drawing.paint.xfermode = null
            }

            // Draw shape
            drawing.paint.color = color
            if (cornerRadius > 0) {
                drawing.canvas.drawRoundRect(rect, cornerRadius.toFloat(), cornerRadius.toFloat(), drawing.paint)
            } else {
                drawing.canvas.drawRect(rect.left, rect.top, rect.right, rect.bottom, drawing.paint)
            }
            return endDrawingResult(context, drawing)
        }
        return null
    }

    override fun generate(context: Context, attributes: Map<String, Any>, onDrawable: Drawable?): Drawable? {
        val gravity = gravityFromAttributes(attributes)
        return generate(
            context,
            ViewletMapUtil.optionalColor(attributes, "color", Color.TRANSPARENT),
            widthFromAttributes(attributes),
            heightFromAttributes(attributes),
            ViewletMapUtil.optionalDimension(attributes, "cornerRadius", 0),
            gravity.x,
            gravity.y,
            imageWidthFromAttributes(attributes),
            imageHeightFromAttributes(attributes),
            onDrawable
        )
    }

}

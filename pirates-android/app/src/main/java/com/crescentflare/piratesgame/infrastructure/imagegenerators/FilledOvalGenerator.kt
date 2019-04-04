package com.crescentflare.piratesgame.infrastructure.imagegenerators

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.Drawable
import com.crescentflare.piratesgame.infrastructure.inflator.Inflators

/**
 * Image generators: generates an oval filled with a color
 */
class FilledOvalGenerator: ImageDrawableGenerator() {

    fun generate(context: Context, color: Int, width: Int? = null, height: Int? = null, horizontalGravity: Float = 0.5f, verticalGravity: Float = 0.5f, forceImageWidth: Int? = null, forceImageHeight: Int? = null, onDrawable: Drawable? = null): Drawable? {
        val drawing = beginImageDrawing(width, height, horizontalGravity, verticalGravity, forceImageWidth, forceImageHeight, onDrawable)
        if (drawing != null) {
            // Cut out shape to allow transparent overdraw without blending
            val rect = drawing.drawRect
            if (!drawing.cleanStart && (color.toLong() and 0xff000000) != 0xff000000) {
                drawing.paint.color = Color.TRANSPARENT
                drawing.paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                drawing.canvas.drawRect(RectF(rect.left + 0.5f, rect.top + 0.5f, rect.right - 0.5f, rect.bottom - 0.5f), drawing.paint)
                drawing.paint.xfermode = null
            }

            // Draw shape
            drawing.paint.color = color
            drawing.canvas.drawOval(RectF(rect.left, rect.top, rect.right, rect.bottom), drawing.paint)
            return endDrawingResult(context, drawing)
        }
        return null
    }

    override fun generate(context: Context, attributes: Map<String, Any>, onDrawable: Drawable?): Drawable? {
        val mapUtil = Inflators.viewlet.mapUtil
        val gravity = gravityFromAttributes(mapUtil, attributes)
        return generate(
            context,
            mapUtil.optionalColor(attributes, "color", Color.TRANSPARENT),
            widthFromAttributes(mapUtil, attributes), heightFromAttributes(mapUtil, attributes),
            gravity.x, gravity.y,
            imageWidthFromAttributes(mapUtil, attributes), imageHeightFromAttributes(mapUtil, attributes),
            onDrawable
        )
    }

}

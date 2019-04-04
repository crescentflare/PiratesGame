package com.crescentflare.piratesgame.infrastructure.imagegenerators

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import com.crescentflare.piratesgame.infrastructure.inflator.Inflators

/**
 * Image generators: generates a stroke following a path
 */
class StrokedPathGenerator: ImageDrawableGenerator() {

    fun generate(context: Context, color: Int, strokeSize: Int, points: List<Point> = emptyList(), horizontalGravity: Float = 0.5f, verticalGravity: Float = 0.5f, forceImageWidth: Int? = null, forceImageHeight: Int? = null, onDrawable: Drawable? = null): Drawable? {
        var minPointX = Int.MAX_VALUE
        var minPointY = Int.MAX_VALUE
        var maxPointX = Int.MIN_VALUE
        var maxPointY = Int.MIN_VALUE
        for (point in points) {
            minPointX = Math.min(minPointX, point.x - strokeSize)
            minPointY = Math.min(minPointY, point.y - strokeSize)
            maxPointX = Math.max(maxPointX, point.x + strokeSize)
            maxPointY = Math.max(maxPointY, point.y + strokeSize)
        }
        val pathRect = Rect(minPointX, minPointY, maxPointX, maxPointY)
        val drawing = beginImageDrawing(pathRect.width(), pathRect.height(), horizontalGravity, verticalGravity, forceImageWidth, forceImageHeight, onDrawable)
        if (drawing != null) {
            // Prepare path
            val rect = drawing.drawRect
            val path = Path()
            var firstPoint = true
            for (point in points) {
                val position = PointF(point.x.toFloat() - pathRect.left + rect.left, point.y.toFloat() - pathRect.top + rect.top)
                if (firstPoint) {
                    path.moveTo(position.x, position.y)
                    firstPoint = false
                } else {
                    path.lineTo(position.x, position.y)
                }
            }

            // Draw
            drawing.paint.strokeWidth = strokeSize.toFloat()
            drawing.paint.color = color
            drawing.paint.style = Paint.Style.STROKE
            drawing.canvas.drawPath(path, drawing.paint)
            return endDrawingResult(context, drawing)
        }
        return null
    }

    override fun generate(context: Context, attributes: Map<String, Any>, onDrawable: Drawable?): Drawable? {
        val mapUtil = Inflators.viewlet.mapUtil
        val gravity = gravityFromAttributes(mapUtil, attributes)
        val points = mutableListOf<Point>()
        val pointsAttribute = attributes["points"]
        if (pointsAttribute is String) {
            val pointSets = pointsAttribute.split(";")
            for (pointSet in pointSets) {
                val pointPair = pointSet.trim().split(",")
                if (pointPair.size == 2) {
                    val tmpMap = mapOf(Pair("x", pointPair[0].trim()), Pair("y", pointPair[1].trim()))
                    points.add(Point(mapUtil.optionalDimension(tmpMap, "x", 0), mapUtil.optionalDimension(tmpMap, "y", 0)))
                }
            }
        } else {
            val pointArray = mapUtil.optionalObjectList(attributes, "points")
            if (pointArray.size > 0 && pointArray[0] is List<*>) {
                for (pointObject in pointArray) {
                    if (pointObject is List<*>) {
                        val listPair = pointObject.toList()
                        if (listPair.size == 2) {
                            val tmpMap = mapOf(Pair("x", listPair[0]), Pair("y", listPair[1]))
                            points.add(Point(mapUtil.optionalDimension(tmpMap, "x", 0), mapUtil.optionalDimension(tmpMap, "y", 0)))
                        }
                    }
                }
            } else {
                val flatArray = mapUtil.optionalDimensionList(attributes, "points")
                for (i in flatArray.indices) {
                    if (i % 2 == 0 && i + 1 < flatArray.size) {
                        points.add(Point(flatArray[i], flatArray[i + 1]))
                    }
                }
            }
        }
        return generate(
            context,
            mapUtil.optionalColor(attributes, "color", Color.TRANSPARENT),
            mapUtil.optionalDimension(attributes, "strokeSize", 0),
            points,
            gravity.x, gravity.y,
            imageWidthFromAttributes(mapUtil, attributes), imageHeightFromAttributes(mapUtil, attributes),
            onDrawable
        )
    }

}

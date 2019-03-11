package com.crescentflare.piratesgame.components.utility

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.widget.ProgressBar
import com.crescentflare.piratesgame.infrastructure.coreextensions.urlDecode
import com.crescentflare.piratesgame.infrastructure.coreextensions.urlEncode
import com.crescentflare.viewletcreator.utility.ViewletMapUtil


/**
 * Component utility: defines the source of an image (an internal or external image)
 */
class ImageSource {

    // --
    // Statics
    // --

    companion object {

        // --
        // Static: factory method
        // --

        fun fromObject(value: Any?): ImageSource? {
            if (value is String) {
                return ImageSource(value)
            } else if (value is Map<*, *>) {
                val result: Map<String, Any>? = ViewletMapUtil.asStringObjectMap(value)
                if (result != null) {
                    return ImageSource(result)
                }
            }
            return null
        }


        // --
        // Static: image generators
        // --

        fun generateFilledRect(context: Context, color: Int, width: Int? = null, height: Int? = null, horizontalGravity: Float = 0.5f, verticalGravity: Float = 0.5f, forceImageWidth: Int? = null, forceImageHeight: Int? = null, onDrawable: Drawable? = null): Drawable? {
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

            // Open canvas and draw
            if (bitmap != null) {
                // Prepare canvas
                val canvas = Canvas(bitmap)
                val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG or Paint.ANTI_ALIAS_FLAG)
                val x = (bitmapWidth - wantWidth) * horizontalGravity
                val y = (bitmapHeight - wantHeight) * verticalGravity

                // Cut out shape first when drawing with opacity on an existing image
                if (onDrawable is BitmapDrawable && (color.toLong() and 0xff000000) != 0xff000000) {
                    // Determine the rectangle, shrink shape if it's not on a pixel boundary to improve edge blending
                    val cutRect = RectF(x + 0.5f, y + 0.5f, x + wantWidth - 1, y + wantHeight - 1)
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
                    paint.color = Color.TRANSPARENT
                    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                    canvas.drawRect(RectF(x + 0.5f, y + 0.5f, x + wantWidth - 1, y + wantHeight - 1), paint)
                    paint.xfermode = null
                }

                // Draw shape
                paint.color = color
                canvas.drawRect(x, y, x + wantWidth, y + wantHeight, paint)
            }

            // Return result
            return BitmapDrawable(context.resources, bitmap)
        }

        fun generateFilledOval(context: Context, color: Int, width: Int? = null, height: Int? = null, horizontalGravity: Float = 0.5f, verticalGravity: Float = 0.5f, forceImageWidth: Int? = null, forceImageHeight: Int? = null, onDrawable: Drawable? = null): Drawable? {
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

            // Open canvas and draw
            if (bitmap != null) {
                // Prepare canvas
                val canvas = Canvas(bitmap)
                val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG or Paint.ANTI_ALIAS_FLAG)
                val x = (bitmapWidth - wantWidth) * horizontalGravity
                val y = (bitmapHeight - wantHeight) * verticalGravity

                // Cut out shape first when drawing with opacity on an existing image (shrinking the draw area is intentional due to edge blending)
                if (onDrawable is BitmapDrawable && (color.toLong() and 0xff000000) != 0xff000000) {
                    paint.color = Color.TRANSPARENT
                    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                    canvas.drawOval(RectF(x + 0.5f, y + 0.5f, x + wantWidth - 0.5f, y + wantHeight - 0.5f), paint)
                    paint.xfermode = null
                }

                // Draw shape
                paint.color = color
                canvas.drawOval(RectF(x, y, x + wantWidth, y + wantHeight), paint)
            }

            // Return result
            return BitmapDrawable(context.resources, bitmap)
        }

    }


    // --
    // Members
    // --

    var type = Type.Unknown
    var parameters = mutableMapOf<String, Any>()
    var pathComponents = emptyList<String>()
    var otherSources = mutableListOf<ImageSource>()


    // --
    // Initialization
    // --

    constructor(value: Any?) {
        if (value is String) {
            initParse(value)
        } else if (value is Map<*, *>) {
            val result: Map<String, Any>? = ViewletMapUtil.asStringObjectMap(value)
            initParse(result ?: mapOf())
        }
    }

    constructor(string: String) {
        initParse(string)
    }

    constructor(map: Map<String, Any>) {
        initParse(map)
    }

    private fun initParse(string: String) {
        // Extract scheme
        var checkString = string
        val schemeMarker = checkString.indexOf("://")
        if (schemeMarker >= 0) {
            type = Type.fromString(checkString.substring(0, schemeMarker))
            checkString = checkString.substring(schemeMarker + 3)
        }

        // Extract parameters
        val paramMarker = checkString.indexOf('?')
        if (paramMarker >= 0) {
            // Get parameter string
            val paramString = checkString.substring(paramMarker + 1)
            checkString = checkString.substring(0, paramMarker)

            // Split into separate parameters and fill dictionary
            val paramItems = paramString.split("&")
            for (paramItem in paramItems) {
                val paramSet = paramItem.split("=")
                if (paramSet.size == 2) {
                    parameters[paramSet[0].urlDecode()] = paramSet[1].urlDecode()
                }
            }
        }

        // Finally set path to the remaining string
        pathComponents = checkString.split("/")
    }

    private fun initParse(map: Map<String, Any>) {
        type = Type.fromString(map["type"] as? String)
        val path = map["path"] ?: map["name"]
        if (path is String) {
            pathComponents = path.split("/")
        }
        val otherSources = ViewletMapUtil.optionalObjectList(map, "otherSources")
        for (otherSource in otherSources) {
            val imageSource = ImageSource.fromObject(otherSource)
            if (imageSource != null) {
                this.otherSources.add(imageSource)
            }
        }
        for (key in map.keys) {
            if (key != "type" && key != "path" && key != "name" && key != "otherSources") {
                val value = map[key]
                if (value != null) {
                    parameters[key] = value
                }
            }
        }
    }


    // --
    // Extract values
    // --

    val fullURI: String
        get() {
            var uri = "$type://$fullPath"
            if (parameters.isNotEmpty()) {
                var firstParam = true
                for (key in parameters.keys) {
                    val stringValue = ViewletMapUtil.optionalString(parameters, key, null)
                    if (stringValue != null) {
                        uri += if (firstParam) "?" else "&"
                        uri += key.urlEncode() + "=" + stringValue.urlEncode()
                        firstParam = false
                    }
                }
            }
            return uri
        }

    val fullPath: String
        get() = pathComponents.joinToString("/")

    val tintColor: Int
        get() = ViewletMapUtil.optionalColor(parameters.toMap(), "colorize", 0)

    val threePatch: Int
        get() = ViewletMapUtil.optionalDimension(parameters.toMap(), "threePatch", -1)

    val ninePatch: Int
        get() = ViewletMapUtil.optionalDimension(parameters.toMap(), "ninePatch", -1)

    val onlinePath: String?
        get() = if (type == Type.OnlineImage || type == Type.SecureOnlineImage) fullURI else null


    // --
    // Helper
    // --

    fun getDrawable(context: Context): Drawable? {
        var result: Drawable? = null
        if (type == Type.InternalImage) {
            val resourceId = context.resources.getIdentifier(fullPath, "drawable", context.packageName)
            if (resourceId > 0) {
                result = ContextCompat.getDrawable(context, resourceId)
            }
        } else if (type == Type.SystemImage) {
            when (fullPath) {
                "spinner" -> {
                    val drawable = ProgressBar(context).indeterminateDrawable.mutate()
                    if (drawable is Animatable) {
                        drawable.start()
                    }
                    return drawable
                }
                else -> return null
            }
        } else if (type == Type.Generate) {
            result = getGeneratedDrawable(context)
        }
        for (otherSource in otherSources) {
            val generatedImage = otherSource.getGeneratedDrawable(context, result)
            if (generatedImage != null) {
                result = generatedImage
            }
        }
        return result
    }

    private fun getGeneratedDrawable(context: Context, onDrawable: Drawable? = null): Drawable? {
        val generateType = GenerateType.fromString(fullPath)
        if (generateType != GenerateType.Unknown) {
            val width = ViewletMapUtil.optionalDimension(parameters, "width", 0)
            val height = ViewletMapUtil.optionalDimension(parameters, "height", 0)
            var forceWidth: Int? = ViewletMapUtil.optionalDimension(parameters, "imageWidth", 0)
            var forceHeight: Int? = ViewletMapUtil.optionalDimension(parameters, "imageHeight", 0)
            val color = ViewletMapUtil.optionalColor(parameters, "color", Color.TRANSPARENT)
            val horizontalGravity = ViewletUtil.optionalHorizontalGravity(parameters, 0.5f)
            val verticalGravity = ViewletUtil.optionalVerticalGravity(parameters, 0.5f)
            if (forceWidth ?: 0 <= 0) {
                forceWidth = null
            }
            if (forceHeight ?: 0 <= 0) {
                forceHeight = null
            }
            return when (generateType) {
                GenerateType.FilledRect -> generateFilledRect(context, color, width, height, horizontalGravity, verticalGravity, forceWidth, forceHeight, onDrawable)
                GenerateType.FilledOval -> generateFilledOval(context, color, width, height, horizontalGravity, verticalGravity, forceWidth, forceHeight, onDrawable)
                else -> null
            }
        }
        return null
    }


    // --
    // Type enums
    // --

    enum class Type(val value: String) {

        Unknown("unknown"),
        OnlineImage("http"),
        SecureOnlineImage("https"),
        InternalImage("app"),
        SystemImage("system"),
        Generate("generate");

        companion object {

            fun fromString(string: String?): Type {
                for (enum in Type.values()) {
                    if (enum.value == string) {
                        return enum
                    }
                }
                return Unknown
            }

        }

    }

    enum class GenerateType(val value: String) {

        Unknown("unknown"),
        FilledRect("filledRect"),
        FilledOval("filledOval");

        companion object {

            fun fromString(string: String?): GenerateType {
                for (enum in GenerateType.values()) {
                    if (enum.value == string) {
                        return enum
                    }
                }
                return Unknown
            }

        }

    }

}

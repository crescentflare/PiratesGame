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
import com.crescentflare.piratesgame.infrastructure.imagegenerators.FilledOvalGenerator
import com.crescentflare.piratesgame.infrastructure.imagegenerators.FilledRectGenerator
import com.crescentflare.viewletcreator.utility.ViewletMapUtil


/**
 * Component utility: defines the source of an image (an internal or external image)
 */
class ImageSource {

    // --
    // Static: factory method
    // --

    companion object {

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
            return when (generateType) {
                GenerateType.FilledRect -> FilledRectGenerator().generate(context, parameters, onDrawable)
                GenerateType.FilledOval -> FilledOvalGenerator().generate(context, parameters, onDrawable)
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

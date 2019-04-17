package com.crescentflare.piratesgame.components.utility

import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.widget.ProgressBar
import com.crescentflare.piratesgame.infrastructure.appconfig.CustomAppConfigManager
import com.crescentflare.piratesgame.infrastructure.coreextensions.urlDecode
import com.crescentflare.piratesgame.infrastructure.coreextensions.urlEncode
import com.crescentflare.piratesgame.infrastructure.imagegenerators.FilledOvalGenerator
import com.crescentflare.piratesgame.infrastructure.imagegenerators.FilledRectGenerator
import com.crescentflare.piratesgame.infrastructure.imagegenerators.StrokedPathGenerator
import com.crescentflare.piratesgame.infrastructure.inflator.Inflators


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
                val result: Map<String, Any>? = Inflators.viewlet.mapUtil.asStringObjectMap(value)
                if (result != null) {
                    return ImageSource(result)
                }
            }
            return null
        }


        // --
        // Static: cache
        // --

        private val cachedDrawables = mutableMapOf<String, Drawable>()

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
            val result: Map<String, Any>? = Inflators.viewlet.mapUtil.asStringObjectMap(value)
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
                    val key = paramSet[0].urlDecode()
                    if (key == "otherSources") {
                        val otherSourcesStringArray = paramSet[1].split(",")
                        for (otherSourcesString in otherSourcesStringArray) {
                            val otherSource = ImageSource(otherSourcesString.urlDecode())
                            otherSources.add(otherSource)
                        }
                    } else {
                        parameters[key] = paramSet[1].urlDecode()
                    }
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
        val otherSources = Inflators.viewlet.mapUtil.optionalObjectList(map, "otherSources")
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

    val fullPath: String
        get() = pathComponents.joinToString("/")

    val onlineUri: String?
        get() {
            if (type == Type.OnlineImage || type == Type.SecureOnlineImage || type == Type.DevServerImage) {
                var uri = "${type.value}://$fullPath"
                if (type == Type.DevServerImage) {
                    uri = CustomAppConfigManager.currentConfig().devServerUrl
                    if (!uri.startsWith("http")) {
                        uri = "http://$uri"
                    }
                    uri = "$uri/pageimages/$fullPath"
                }
                getParameterString(listOf("caching", "colorize", "threePatch", "ninePatch"), true)?.let {
                    uri += "?$it"
                }
                return uri
            }
            return null
        }

    val tintColor: Int
        get() = Inflators.viewlet.mapUtil.optionalColor(parameters.toMap(), "colorize", 0)

    val threePatch: Int
        get() = Inflators.viewlet.mapUtil.optionalDimension(parameters.toMap(), "threePatch", -1)

    val ninePatch: Int
        get() = Inflators.viewlet.mapUtil.optionalDimension(parameters.toMap(), "ninePatch", -1)


    // --
    // Caching
    // --

    val caching: Caching
        get() = Caching.fromString(Inflators.viewlet.mapUtil.optionalString(parameters.toMap(), "caching", ""))

    val cacheKey: String
        get() {
            var uri = "${type.value}://$fullPath"
            getParameterString(listOf("caching", "colorize", "threePatch", "ninePatch"))?.let {
                uri += "?$it"
            }
            return uri
        }


    // --
    // Conversion
    // --

    val uri: String
        get() {
            var uri = "${type.value}://$fullPath"
            getParameterString()?.let {
                uri += "?$it"
            }
            return uri
        }

    val map: Map<String, Any>
        get() {
            val map = mutableMapOf<String, Any>(Pair("type", type.value), Pair("path", fullPath))
            map.putAll(parameters)
            if (otherSources.isNotEmpty()) {
                map["otherSources"] = otherSources.map { it.map }
            }
            return map
        }


    // --
    // Helper
    // --

    fun getDrawable(context: Context): Drawable? {
        var result: Drawable? = null
        if (caching == Caching.Always) {
            val cachedDrawable = cachedDrawables.get(cacheKey)
            if (cachedDrawable != null) {
                return cachedDrawable
            }
        }
        if (type == Type.InternalImage) {
            val resourceId = context.resources.getIdentifier(fullPath, "drawable", context.packageName)
            if (resourceId > 0) {
                result = ContextCompat.getDrawable(context, resourceId)
            }
        } else if (type == Type.SystemImage) {
            return SystemImageSource.load(context, fullPath)
        } else if (type == Type.Generate) {
            result = getGeneratedDrawable(context)
        }
        for (otherSource in otherSources) {
            val generatedImage = otherSource.getGeneratedDrawable(context, result)
            if (generatedImage != null) {
                result = generatedImage
            }
        }
        if (caching == Caching.Always && result != null) {
            cachedDrawables.put(cacheKey, result)
        }
        return result
    }

    private fun getGeneratedDrawable(context: Context, onDrawable: Drawable? = null): Drawable? {
        val generateType = GenerateType.fromString(fullPath)
        if (generateType != GenerateType.Unknown) {
            return when (generateType) {
                GenerateType.FilledRect -> FilledRectGenerator().generate(context, parameters, onDrawable)
                GenerateType.FilledOval -> FilledOvalGenerator().generate(context, parameters, onDrawable)
                GenerateType.StrokedPath -> StrokedPathGenerator().generate(context, parameters, onDrawable)
                else -> null
            }
        }
        return null
    }

    private fun getParameterString(ignoreParams: List<String> = emptyList(), ignoreOtherSources: Boolean = false): String? {
        if (parameters.isNotEmpty()) {
            var parameterString = ""
            for (key in parameters.keys.sorted()) {
                var ignore = false
                for (ignoreParam in ignoreParams) {
                    if (key == ignoreParam) {
                        ignore = true
                        break
                    }
                }
                val stringValue = Inflators.viewlet.mapUtil.optionalString(parameters, key, null)
                if (!ignore && stringValue != null) {
                    if (parameterString.isNotEmpty()) {
                        parameterString += "&"
                    }
                    parameterString += key.urlEncode() + "=" + stringValue.urlEncode()
                }
            }
            if (otherSources.isNotEmpty() && !ignoreOtherSources) {
                var otherSourceString = ""
                for (otherSource in otherSources) {
                    val otherSourceURI = otherSource.uri
                    if (otherSourceString.isNotEmpty()) {
                        otherSourceString += ","
                    }
                    otherSourceString += otherSourceURI.urlEncode()
                }
                if (parameterString.isNotEmpty()) {
                    parameterString += "&"
                }
                parameterString += "otherSources=$otherSourceString"
            }
            if (parameterString.length > 0) {
                return parameterString
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
        DevServerImage("devserver"),
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
        FilledOval("filledOval"),
        StrokedPath("strokedPath");

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


    // --
    // Caching enum
    // --

    enum class Caching(val value: String) {

        Unknown("unknown"),
        Always("always"),
        Never("never");

        companion object {

            fun fromString(string: String?): Caching {
                for (enum in Caching.values()) {
                    if (enum.value == string) {
                        return enum
                    }
                }
                return Unknown
            }

        }

    }

}

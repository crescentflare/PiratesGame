package com.crescentflare.piratesgame.components.utility

import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.widget.ProgressBar


/**
 * Component utility: provides system image sources
 */
object SystemImageSource {

    fun load(context: Context, path: String): Drawable? {
        when (path) {
            "spinner" -> {
                val drawable = ProgressBar(context).indeterminateDrawable.mutate()
                if (drawable is Animatable) {
                    drawable.start()
                }
                return drawable
            }
            "navigate_back" -> {
                val attributes = mapOf(
                    Pair("type", "generate"),
                    Pair("name", "strokedPath"),
                    Pair("color", "#ffffff"),
                    Pair("strokeSize", 2),
                    Pair("imageWidth", 18),
                    Pair("imageHeight", 18),
                    Pair("gravity", "left"),
                    Pair("points", listOf(7, 0, 0, 7, 7, 14)),
                    Pair("caching", "always"),
                    Pair(
                        "otherSources", listOf(
                            mapOf(
                                Pair("type", "generate"),
                                Pair("name", "strokedPath"),
                                Pair("color", "#ffffff"),
                                Pair("strokeSize", 2),
                                Pair("gravity", "right"),
                                Pair("points", listOf(5, 8, 18, 8))
                            )
                        )
                    )
                )
                return ImageSource(attributes).getDrawable(context)
            }
            "navigate_close" -> {
                val attributes = mapOf(
                    Pair("type", "generate"),
                    Pair("name", "strokedPath"),
                    Pair("color", "#ffffff"),
                    Pair("strokeSize", 2),
                    Pair("points", listOf(0, 0, 14, 14)),
                    Pair("caching", "always"),
                    Pair(
                        "otherSources", listOf(
                            mapOf(
                                Pair("type", "generate"),
                                Pair("name", "strokedPath"),
                                Pair("color", "#ffffff"),
                                Pair("strokeSize", 2),
                                Pair("points", listOf(14, 0, 0, 14))
                            )
                        )
                    )
                )
                return ImageSource(attributes).getDrawable(context)
            }
            else -> return null
        }
    }

}

package com.crescentflare.piratesgame.components.styling

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface

import java.util.HashMap

/**
 * Styling: the fonts used in the app, available everywhere
 * Note: suppress static field leak warning, the context is set through the base application class
 */
@SuppressLint("StaticFieldLeak")
object AppFonts {

    // ---
    // Fonts
    // ---

    val normal = Font(null, Typeface.NORMAL)
    val italics = Font(null, Typeface.ITALIC)
    val bold = Font(null, Typeface.BOLD)
    val boldItalics = Font(null, Typeface.BOLD_ITALIC)
    val titleBold = Font("Primitive.ttf")


    // ---
    // Font lookup
    // ---

    val fontLookup = mapOf<String, Font>(
        Pair("normal", normal),
        Pair("italics", italics),
        Pair("bold", bold),
        Pair("boldItalics", boldItalics),
        Pair("titleNormal", titleBold),
        Pair("titleItalics", titleBold),
        Pair("titleBold", titleBold),
        Pair("titleBoldItalics", titleBold)
    )


    // ---
    // Set application context for loading typefaces
    // ---

    private var context: Context? = null

    fun setContext(context: Context) {
        this.context = context
    }


    // ---
    // Font helper class
    // ---

    class Font constructor(private val filename: String?, private val style: Int = Typeface.NORMAL) {

        val typeface: Typeface
            get() {
                // Return cached typeface if it's there
                val checkLoadedTypeface = loadedTypeface
                if (checkLoadedTypeface != null) {
                    return checkLoadedTypeface
                }

                // Load typeface when needed
                if (filename != null && context != null) {
                    val typeface = Typeface.createFromAsset(context?.assets, "fonts/" + filename)
                    if (typeface != null) {
                        loadedTypeface = typeface
                        return typeface
                    }
                }

                // Return default typeface
                val defaultTypeface = Typeface.create(Typeface.DEFAULT, style)
                loadedTypeface = defaultTypeface
                return defaultTypeface
            }

        private var loadedTypeface: Typeface? = null

        fun load() {
            val dummy = typeface
        }

    }


    // ---
    // Get a typeface
    // ---

    fun getTypeface(fontName: String?): Typeface {
        if (fontName != null) {
            val font = fontLookup[fontName]
            if (font != null) {
                return font.typeface
            }
        }
        return normal.typeface
    }


    // ---
    // Loading
    // ---

    fun loadAll() {
        for ((name, font) in fontLookup) {
            font.load()
        }
    }

}

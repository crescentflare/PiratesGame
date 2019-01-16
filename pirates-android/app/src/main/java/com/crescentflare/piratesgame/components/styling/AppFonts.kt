package com.crescentflare.piratesgame.components.styling

import android.content.Context
import android.graphics.Typeface

import java.util.HashMap

/**
 * Styling: the fonts used in the app, available everywhere
 */
class AppFonts private constructor() {

    // ---
    // Static: singleton instance
    // ---

    companion object {

        lateinit var instance: AppFonts

    }


    // ---
    // Members
    // ---

    private val loadedTypefaces = HashMap<Font, Typeface>()
    private var context: Context? = null


    // ---
    // Available font constants
    // ---

    enum class Font private constructor(private val id: Int, private val filename: String) {

        AndroidDefault(0, ""),
        TitleBold(1, "Primitive.ttf");

        fun toId(): Int {
            return id
        }

        fun toFilename(): String {
            return filename
        }

        companion object {

            fun fromId(id: Int): Font {
                for (e in values()) {
                    if (e.toId() == id) {
                        return e
                    }
                }
                return AndroidDefault
            }
        }

    }

    fun setContext(context: Context) {
        this.context = context
    }


    // ---
    // Get a typeface
    // ---

    fun getTypeface(font: Font?): Typeface {
        // Check if font is different than default
        val context = this.context
        if (font != null && font != Font.AndroidDefault && context != null) {
            val loadedTypeface = loadedTypefaces[font]
            if (loadedTypeface != null) {
                return loadedTypeface
            }
            val typeface = Typeface.createFromAsset(context.assets, "fonts/" + font.toFilename())
            if (typeface != null) {
                loadedTypefaces[font] = typeface
                return typeface
            }
        }

        // If title demi font could not load, fall back to the system bold
        var style = Typeface.NORMAL
        if (font == Font.TitleBold) {
            style = Typeface.BOLD
        }

        // Create typeface and return result
        return Typeface.create(Typeface.DEFAULT, style)
    }

    fun getTypeface(fontName: String?): Typeface {
        var font = Font.AndroidDefault
        if (fontName != null) {
            if (fontName == "normal" || fontName == "systemNormal" || fontName == "system") {
                return Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            } else if (fontName == "italics" || fontName == "systemItalics") {
                return Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            } else if (fontName == "bold" || fontName == "systemBold") {
                return Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            } else if (fontName == "boldItalics" || fontName == "systemBoldItalics") {
                return Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
            } else if (fontName == "titleNormal" || fontName == "titleItalics" || fontName == "titleBold" || fontName == "titleBoldItalics") {
                return getTypeface(Font.TitleBold)
            }
            for (checkFont in Font.values()) {
                if (checkFont.toFilename().replace(".otf", "").replace(".ttf", "") == fontName) {
                    font = checkFont
                }
            }
        }
        return getTypeface(font)
    }

}

package com.crescentflare.piratesgame.components.utility

import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan

import com.crescentflare.piratesgame.R
import com.crescentflare.piratesgame.components.styling.AppFonts
import com.crescentflare.simplemarkdownparser.core.MarkdownTag
import com.crescentflare.simplemarkdownparser.helper.AlignedListSpan
import com.crescentflare.simplemarkdownparser.helper.DefaultMarkdownSpanGenerator

/**
 * A utility to apply styles during markdown parsing
 */
class MarkdownGenerator @JvmOverloads constructor(private val applicationContext: Context, noColorization: Boolean = false, noBulletIndentation: Boolean = false) : DefaultMarkdownSpanGenerator() {

    // ---
    // Members
    // ---

    private var noColorization = false
    private var noBulletIndentation = false


    // ---
    // Initialization
    // ---

    init {
        this.noColorization = noColorization
        this.noBulletIndentation = noBulletIndentation
    }


    // ---
    // Implementation
    // ---

    override fun applySpan(builder: SpannableStringBuilder, type: MarkdownTag.Type, weight: Int, start: Int, end: Int, extra: String) {
        when (type) {
            MarkdownTag.Type.Header -> {
                if (!noColorization) {
                    builder.setSpan(
                        ForegroundColorSpan(
                            ContextCompat.getColor(
                                applicationContext,
                                R.color.colorPrimary
                            )
                        ), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                builder.setSpan(
                    CustomTypefaceSpan(AppFonts.getTypeface("titleBold")),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                if (weight == 1) {
                    builder.setSpan(
                        RelativeSizeSpan(
                            applicationContext.resources.getDimensionPixelSize(R.dimen.titleText).toFloat() / applicationContext.resources.getDimensionPixelSize(
                                R.dimen.text
                            ).toFloat()
                        ), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                } else if (weight == 2) {
                    builder.setSpan(
                        RelativeSizeSpan(
                            applicationContext.resources.getDimensionPixelSize(R.dimen.subTitleText).toFloat() / applicationContext.resources.getDimensionPixelSize(
                                R.dimen.text
                            ).toFloat()
                        ), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                return
            }
            MarkdownTag.Type.TextStyle -> if (weight != 3) {
                val typeface = typefaceForWeight(weight)
                if (typeface != null) {
                    builder.setSpan(CustomTypefaceSpan(typeface), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    return
                }
            }
            MarkdownTag.Type.OrderedList, MarkdownTag.Type.UnorderedList -> if (noBulletIndentation) {
                builder.setSpan(
                    AlignedListSpan(extra, 15 + (weight - 1) * 15, 5),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                return
            }
            else -> {}
        }
        super.applySpan(builder, type, weight, start, end, extra)
    }


    // ---
    // Helper
    // ---

    private fun typefaceForWeight(weight: Int): Typeface? {
        when (weight) {
            0 -> return AppFonts.getTypeface("normal")
            1 -> return AppFonts.getTypeface("italics")
            2 -> return AppFonts.getTypeface("bold")
        }
        return null
    }
}

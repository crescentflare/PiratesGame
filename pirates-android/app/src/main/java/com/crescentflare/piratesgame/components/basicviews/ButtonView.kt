package com.crescentflare.piratesgame.components.basicviews

import android.annotation.TargetApi
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import com.crescentflare.piratesgame.R
import com.crescentflare.piratesgame.components.utility.CustomThreePatchDrawable
import com.crescentflare.piratesgame.components.utility.ImageSource
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.infrastructure.events.AppEventLabeledSender
import com.crescentflare.piratesgame.infrastructure.events.AppEventObserver
import com.crescentflare.unilayout.views.UniButtonView
import com.crescentflare.jsoninflator.JsonInflatable
import com.crescentflare.jsoninflator.binder.InflatorBinder
import com.crescentflare.jsoninflator.utility.InflatorMapUtil
import java.lang.ref.WeakReference

/**
 * Basic view: a button
 */
class ButtonView : UniButtonView, AppEventLabeledSender {

    // --
    // Statics
    // --

    companion object {

        // --
        // Static: viewlet integration
        // --

        val viewlet: JsonInflatable = object : JsonInflatable {

            override fun create(context: Context): Any {
                return ButtonView(context)
            }

            override fun update(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>, parent: Any?, binder: InflatorBinder?): Boolean {
                if (obj is ButtonView) {
                    // Text
                    obj.text = ViewletUtil.localizedString(
                        obj.context,
                        mapUtil.optionalString(attributes, "localizedText", null),
                        mapUtil.optionalString(attributes, "text", null)
                    )

                    // Font
                    val defaultTextSize = obj.resources.getDimensionPixelSize(R.dimen.buttonText)
                    obj.setTextSize(
                        TypedValue.COMPLEX_UNIT_PX,
                        mapUtil.optionalDimension(attributes, "textSize", defaultTextSize).toFloat()
                    )

                    // Generic view properties
                    ViewletUtil.applyGenericViewAttributes(mapUtil, obj, attributes)

                    // Button background and text color based on color style
                    obj.setColorStyle(ColorStyle.fromString(mapUtil.optionalString(attributes, "colorStyle", "")))

                    // Event handling
                    obj.tapEvent = AppEvent.fromObject(attributes["tapEvent"])

                    // Forward event observer
                    if (parent is AppEventObserver) {
                        obj.eventObserver = parent
                    }
                    return true
                }
                return false
            }

            override fun canRecycle(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>): Boolean {
                return obj is ButtonView
            }

        }


        // --
        // Static: styles
        // --

        fun defaultStyle(): Map<String, Any> {
            return mapOf(
                Pair("padding", listOf(
                    "\$buttonHorizontalPadding",
                    "\$buttonVerticalPadding",
                    "\$buttonHorizontalPadding",
                    "\$buttonVerticalPadding"
                )),
                Pair("minHeight", "\$buttonHeight"),
                Pair("textSize", "\$buttonText")
            )
        }


        // --
        // Static: background generators
        // --

        private fun getPrimaryButtonDrawable(context: Context): Drawable? {
            return generateDrawableForButton(context, "\$secondary", "\$secondaryHighlight")
        }

        private fun getPrimaryHighlightButtonDrawable(context: Context): Drawable? {
            return generateDrawableForButton(context, "\$secondaryHighlight")
        }

        private fun getSecondaryButtonDrawable(context: Context): Drawable? {
            return generateDrawableForButton(context, "\$primary", "\$primaryHighlight")
        }

        private fun getSecondaryHighlightButtonDrawable(context: Context): Drawable? {
            return generateDrawableForButton(context, "\$primaryHighlight")
        }

        private fun getInvertedButtonDrawable(context: Context): Drawable? {
            return generateDrawableForButton(context, "\$inverted", "\$invertedHighlight")
        }

        private fun getInvertedHighlightButtonDrawable(context: Context): Drawable? {
            return generateDrawableForButton(context, "\$invertedHighlight")
        }

        private fun getDisabledButtonDrawable(context: Context): Drawable? {
            return generateDrawableForButton(context, "\$disabled", "\$disabledHighlight")
        }

        private fun getDisabledInvertedButtonDrawable(context: Context): Drawable? {
            return generateDrawableForButton(context, "\$disabledInverted", "\$disabledInvertedHighlight")
        }

        private fun generateDrawableForButton(context: Context, colorDefinition: String, edgeColorDefinition: String? = null): Drawable? {
            val buttonHeight = context.resources.getDimensionPixelSize(R.dimen.buttonHeight).toFloat() / Resources.getSystem().displayMetrics.density
            val drawable: Drawable?
            if (edgeColorDefinition != null) {
                val attributes = mapOf(
                    Pair("type", "generate"),
                    Pair("name", "filledOval"),
                    Pair("width", 64),
                    Pair("height", buttonHeight * 1.625),
                    Pair("imageWidth", 64),
                    Pair("imageHeight", buttonHeight),
                    Pair("color", edgeColorDefinition),
                    Pair("caching", "always"),
                    Pair("otherSources", listOf(mapOf(
                        Pair("type", "generate"),
                        Pair("name", "filledOval"),
                        Pair("width", 52),
                        Pair("height", buttonHeight * 2),
                        Pair("color", colorDefinition)
                    )))
                )
                drawable = ImageSource(attributes).getDrawable(context)
            } else {
                val attributes = mapOf(
                    Pair("type", "generate"),
                    Pair("name", "filledOval"),
                    Pair("width", 64),
                    Pair("height", buttonHeight * 1.625),
                    Pair("imageWidth", 64),
                    Pair("imageHeight", buttonHeight),
                    Pair("color", colorDefinition),
                    Pair("caching", "always")
                )
                drawable = ImageSource(attributes).getDrawable(context)
            }
            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                if (bitmap != null) {
                    return CustomThreePatchDrawable(bitmap, (Resources.getSystem().displayMetrics.density * 12).toInt())
                }
            }
            return null
        }

    }


    // --
    // Members
    // --

    private var eventObserverReference: WeakReference<AppEventObserver>? = null


    // --
    // Initialization
    // --

    @JvmOverloads
    constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = 0)
            : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
            context: Context,
            attrs: AttributeSet?,
            defStyleAttr: Int,
            defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        val buttonHorizontalPadding = resources.getDimensionPixelSize(R.dimen.buttonHorizontalPadding)
        val buttonVerticalPadding = resources.getDimensionPixelSize(R.dimen.buttonVerticalPadding)
        minHeight = 0
        minimumHeight = 0
        minWidth = 0
        minimumWidth = 0
        gravity = Gravity.CENTER
        isAllCaps = false
        setColorStyle(ColorStyle.Primary)
        setPadding(buttonHorizontalPadding, buttonVerticalPadding, buttonHorizontalPadding, buttonVerticalPadding)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.buttonText).toFloat())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            stateListAnimator = null
        }
    }


    // --
    // Configurable values
    // --

    var eventObserver: AppEventObserver?
        get() = eventObserverReference?.get()
        set(newValue) {
            eventObserverReference = if (newValue != null) {
                WeakReference(newValue)
            } else {
                null
            }
        }

    var tapEvent: AppEvent? = null
        set(tapEvent) {
            field = tapEvent
            if (hasOnClickListeners()) {
                setOnClickListener(null)
            }
            if (this.tapEvent != null) {
                setOnClickListener {
                    val currentTapEvent = this@ButtonView.tapEvent
                    if (currentTapEvent != null) {
                        eventObserver?.observedEvent(currentTapEvent, this@ButtonView)
                    }
                }
            }
        }

    fun setColorStyle(colorStyle: ColorStyle) {
        when (colorStyle) {
            ColorStyle.Primary -> {
                background = createStateBackground(
                    getPrimaryButtonDrawable(context),
                    getPrimaryHighlightButtonDrawable(context),
                    getDisabledButtonDrawable(context)
                )
                setTextColor(createStateTitleColor(
                    ContextCompat.getColor(context, R.color.textInverted),
                    ContextCompat.getColor(context, R.color.textInverted),
                    ContextCompat.getColor(context, R.color.textDisabled)
                ))
            }
            ColorStyle.PrimaryInverted -> {
                background = createStateBackground(
                    getInvertedButtonDrawable(context),
                    getInvertedHighlightButtonDrawable(context),
                    getDisabledInvertedButtonDrawable(context)
                )
                setTextColor(createStateTitleColor(
                    ContextCompat.getColor(context, R.color.primary),
                    ContextCompat.getColor(context, R.color.primary),
                    ContextCompat.getColor(context, R.color.primary)
                ))
            }
            ColorStyle.Secondary -> {
                background = createStateBackground(
                    getSecondaryButtonDrawable(context),
                    getSecondaryHighlightButtonDrawable(context),
                    getDisabledButtonDrawable(context)
                )
                setTextColor(createStateTitleColor(
                    ContextCompat.getColor(context, R.color.textInverted),
                    ContextCompat.getColor(context, R.color.textInverted),
                    ContextCompat.getColor(context, R.color.textDisabled)
                ))
            }
        }
    }


    // --
    // Interaction
    // --

    override val senderLabel: String?
        get() = text?.toString()


    // --
    // Helpers
    // --

    private fun createStateBackground(mainDrawable: Drawable?, highlightDrawable: Drawable?, disabledDrawable: Drawable?): Drawable? {
        val drawable = StateListDrawable()
        if (disabledDrawable != null) {
            drawable.addState(intArrayOf(-android.R.attr.state_enabled), disabledDrawable)
        }
        if (highlightDrawable != null) {
            drawable.addState(intArrayOf(android.R.attr.state_selected), highlightDrawable)
            drawable.addState(intArrayOf(android.R.attr.state_focused), highlightDrawable)
            drawable.addState(intArrayOf(android.R.attr.state_pressed), highlightDrawable)
        }
        if (mainDrawable != null) {
            drawable.addState(intArrayOf(), mainDrawable)
        }
        return drawable
    }

    private fun createStateTitleColor(mainColor: Int, highlightColor: Int, disabledColor: Int): ColorStateList {
        return ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_enabled, android.R.attr.state_selected),
                intArrayOf(android.R.attr.state_enabled, android.R.attr.state_focused),
                intArrayOf(android.R.attr.state_enabled, android.R.attr.state_pressed),
                intArrayOf(android.R.attr.state_enabled),
                intArrayOf()
            ),
            intArrayOf(highlightColor, highlightColor, highlightColor, mainColor, disabledColor)
        )
    }


    // --
    // Color style enum
    // --

    enum class ColorStyle(val value: String) {

        Primary("primary"),
        PrimaryInverted("primaryInverted"),
        Secondary("secondary");

        companion object {

            fun fromString(string: String?): ColorStyle {
                for (enum in ColorStyle.values()) {
                    if (enum.value == string) {
                        return enum
                    }
                }
                return Primary
            }

        }

    }

}

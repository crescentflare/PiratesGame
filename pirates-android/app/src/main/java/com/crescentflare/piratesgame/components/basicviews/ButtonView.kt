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
import android.view.View
import android.view.ViewGroup
import com.crescentflare.piratesgame.R
import com.crescentflare.piratesgame.components.utility.CustomThreePatchDrawable
import com.crescentflare.piratesgame.components.utility.ImageSource
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.infrastructure.events.AppEventLabeledSender
import com.crescentflare.piratesgame.infrastructure.events.AppEventObserver
import com.crescentflare.unilayout.views.UniButtonView
import com.crescentflare.viewletcreator.ViewletCreator
import com.crescentflare.viewletcreator.binder.ViewletBinder
import com.crescentflare.viewletcreator.utility.ViewletMapUtil
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

        val viewlet: ViewletCreator.Viewlet = object : ViewletCreator.Viewlet {

            override fun create(context: Context): View {
                return ButtonView(context)
            }

            override fun update(view: View, attributes: Map<String, Any>, parent: ViewGroup?, binder: ViewletBinder?): Boolean {
                if (view is ButtonView) {
                    // Text
                    view.text = ViewletUtil.localizedString(
                        view.context,
                        ViewletMapUtil.optionalString(attributes, "localizedText", null),
                        ViewletMapUtil.optionalString(attributes, "text", null)
                    )

                    // Font
                    val defaultTextSize = view.resources.getDimensionPixelSize(R.dimen.buttonText)
                    view.setTextSize(
                        TypedValue.COMPLEX_UNIT_PX,
                        ViewletMapUtil.optionalDimension(attributes, "textSize", defaultTextSize).toFloat()
                    )

                    // Generic view properties
                    ViewletUtil.applyGenericViewAttributes(view, attributes)

                    // Button background and text color based on color style
                    view.setColorStyle(ColorStyle.fromString(ViewletMapUtil.optionalString(attributes, "colorStyle", "")))

                    // Event handling
                    view.tapEvent = AppEvent.fromObject(attributes["tapEvent"])

                    // Forward event observer
                    if (parent is AppEventObserver) {
                        view.eventObserver = parent
                    }
                    return true
                }
                return false
            }

            override fun canRecycle(view: View, attributes: Map<String, Any>): Boolean {
                return view is ButtonView
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

        var cachedPrimaryButtonDrawable: Drawable? = null
        var cachedPrimaryHighlightButtonDrawable: Drawable? = null
        var cachedSecondaryButtonDrawable: Drawable? = null
        var cachedSecondaryHighlightButtonDrawable: Drawable? = null
        var cachedInvertedButtonDrawable: Drawable? = null
        var cachedInvertedHighlightButtonDrawable: Drawable? = null
        var cachedDisabledButtonDrawable: Drawable? = null
        var cachedDisabledInvertedButtonDrawable: Drawable? = null

        private fun getPrimaryButtonDrawable(context: Context): Drawable? {
            if (cachedPrimaryButtonDrawable != null) {
                return cachedPrimaryButtonDrawable
            }
            cachedPrimaryButtonDrawable = generateDrawableForButton(context, "\$secondary", "\$secondaryHighlight")
            return cachedPrimaryButtonDrawable
        }

        private fun getPrimaryHighlightButtonDrawable(context: Context): Drawable? {
            if (cachedPrimaryHighlightButtonDrawable != null) {
                return cachedPrimaryHighlightButtonDrawable
            }
            cachedPrimaryHighlightButtonDrawable = generateDrawableForButton(context, "\$secondaryHighlight")
            return cachedPrimaryHighlightButtonDrawable
        }

        private fun getSecondaryButtonDrawable(context: Context): Drawable? {
            if (cachedSecondaryButtonDrawable != null) {
                return cachedSecondaryButtonDrawable
            }
            cachedSecondaryButtonDrawable = generateDrawableForButton(context, "\$primary", "\$primaryHighlight")
            return cachedSecondaryButtonDrawable
        }

        private fun getSecondaryHighlightButtonDrawable(context: Context): Drawable? {
            if (cachedSecondaryHighlightButtonDrawable != null) {
                return cachedSecondaryHighlightButtonDrawable
            }
            cachedSecondaryHighlightButtonDrawable = generateDrawableForButton(context, "\$primaryHighlight")
            return cachedSecondaryHighlightButtonDrawable
        }

        private fun getInvertedButtonDrawable(context: Context): Drawable? {
            if (cachedInvertedButtonDrawable != null) {
                return cachedInvertedButtonDrawable
            }
            cachedInvertedButtonDrawable = generateDrawableForButton(context, "\$inverted", "\$invertedHighlight")
            return cachedInvertedButtonDrawable
        }

        private fun getInvertedHighlightButtonDrawable(context: Context): Drawable? {
            if (cachedInvertedHighlightButtonDrawable != null) {
                return cachedInvertedHighlightButtonDrawable
            }
            cachedInvertedHighlightButtonDrawable = generateDrawableForButton(context, "\$invertedHighlight")
            return cachedInvertedHighlightButtonDrawable
        }

        private fun getDisabledButtonDrawable(context: Context): Drawable? {
            if (cachedDisabledButtonDrawable != null) {
                return cachedDisabledButtonDrawable
            }
            cachedDisabledButtonDrawable = generateDrawableForButton(context, "\$disabled", "\$disabledHighlight")
            return cachedDisabledButtonDrawable
        }

        private fun getDisabledInvertedButtonDrawable(context: Context): Drawable? {
            if (cachedDisabledInvertedButtonDrawable != null) {
                return cachedDisabledInvertedButtonDrawable
            }
            cachedDisabledInvertedButtonDrawable = generateDrawableForButton(context, "\$disabledInverted", "\$disabledInvertedHighlight")
            return cachedDisabledInvertedButtonDrawable
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
                    Pair("color", colorDefinition)
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

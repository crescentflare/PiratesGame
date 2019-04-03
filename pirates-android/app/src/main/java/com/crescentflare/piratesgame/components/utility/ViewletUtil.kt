package com.crescentflare.piratesgame.components.utility


import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import com.crescentflare.piratesgame.BuildConfig
import com.crescentflare.piratesgame.infrastructure.coreextensions.localized

import com.crescentflare.unilayout.helpers.UniLayoutParams
import com.crescentflare.unilayout.views.UniView
import com.crescentflare.jsoninflator.JsonInflatable
import com.crescentflare.jsoninflator.binder.InflatableRef
import com.crescentflare.jsoninflator.binder.InflatorAnnotationBinder
import com.crescentflare.jsoninflator.binder.InflatorBinder
import com.crescentflare.jsoninflator.utility.InflatorMapUtil
import com.crescentflare.piratesgame.infrastructure.inflator.Inflators
import kotlinx.coroutines.*

import org.junit.Assert
import java.util.Arrays

/**
 * Component utility: shared utilities for viewlet integration, also contains the viewlet for a basic UniView
 */
object ViewletUtil {

    // --
    // Basic view viewlet
    // --

    val basicViewViewlet: JsonInflatable = object : JsonInflatable {

        override fun create(context: Context): Any {
            return UniView(context)
        }

        override fun update(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>, parent: Any?, binder: InflatorBinder?): Boolean {
            if (obj is View) {
                ViewletUtil.applyGenericViewAttributes(mapUtil, obj, attributes)
            }
            return true
        }

        override fun canRecycle(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>): Boolean {
            return obj is UniView
        }

    }


    // --
    // Inflate with assertion
    // --

    fun assertInflateOn(view: View, attributes: Map<String, Any>, binder: InflatorBinder) {
        assertInflateOn(view, attributes, null, binder)
    }

    fun assertInflateOn(view: View, attributes: Map<String, Any>?, parent: ViewGroup?, binder: InflatorBinder) {
        val inflateResult = Inflators.viewlet.inflateOn(view, attributes, parent, binder)
        if (BuildConfig.DEBUG) {
            // First check if attributes are not null
            Assert.assertNotNull("Attributes are null, load issue?", attributes)

            // Check viewlet name
            val viewletName = Inflators.viewlet.findInflatableNameInAttributes(attributes)
            Assert.assertNotNull("No viewlet found, JSON structure issue?", viewletName)

            // Check if the viewlet is registered
            Assert.assertNotNull("No viewlet implementation found, registration issue of $viewletName?", Inflators.viewlet.findInflatableInAttributes(attributes))

            // Check result of inflate
            Assert.assertTrue("Can't inflate viewlet, class doesn't match with $viewletName?", inflateResult)

            // Check if there are any referenced views that are null
            if (binder is InflatorAnnotationBinder) {
                checkViewletRefs(view)
            }
        }
    }

    private fun checkViewletRefs(view: View) {
        for (field in view.javaClass.declaredFields) {
            for (annotation in field.declaredAnnotations) {
                if (annotation is InflatableRef) {
                    var isNull = true
                    try {
                        field.isAccessible = true
                        if (field.get(view) != null) {
                            isNull = false
                        }
                    } catch (ignored: IllegalAccessException) {
                    }
                    Assert.assertFalse("Referenced view is null: " + annotation.value, isNull)
                }
            }
        }
    }


    // --
    // Subview creation
    // --

    fun createSubviews(mapUtil: InflatorMapUtil, container: ViewGroup, parent: ViewGroup, attributes: Map<String, Any>?, subviewItems: Any?, binder: InflatorBinder?) {
        // Check if children are the same before and after the update, then they can be updated instead of re-created
        var canRecycle = false
        val recycling = mapUtil.optionalBoolean(attributes, "recycling", false)
        val items = Inflators.viewlet.attributesForNestedInflatableList(subviewItems)
        if (recycling) {
            if (items.size == container.childCount) {
                canRecycle = true
                for (i in items.indices) {
                    if (!Inflators.viewlet.canRecycle(container.getChildAt(i), items[i])) {
                        canRecycle = false
                        break
                    }
                }
            }
        }

        // Update children
        if (canRecycle) {
            var childIndex = 0
            for (item in items) {
                if (childIndex < container.childCount) {
                    val child = container.getChildAt(childIndex)
                    Inflators.viewlet.inflateOn(child, item, parent, binder)
                    ViewletUtil.applyLayoutAttributes(mapUtil, child, item)
                    ViewletUtil.bindRef(mapUtil, child, item, binder)
                    childIndex++
                }
            }
        } else {
            // First remove all children (and remember the scroll position if one of them is a scrollview)
            val childCount = container.childCount
            var scrollPosition = 0
            for (i in 0 until childCount) {
                val child = container.getChildAt(i)
                if (child is ScrollView) {
                    scrollPosition = child.getScrollY()
                }
            }
            container.removeAllViews()

            // Add children
            var foundScrollView: ScrollView? = null
            for (item in items) {
                val result = Inflators.viewlet.inflate(container.context, item, parent, binder)
                if (result is View) {
                    container.addView(result)
                    ViewletUtil.applyLayoutAttributes(mapUtil, result, item)
                    if (scrollPosition != 0 && foundScrollView == null) {
                        if (result is ScrollView) {
                            foundScrollView = result
                        }
                    }
                    ViewletUtil.bindRef(mapUtil, result, item, binder)
                }
            }

            // Set back the scrollview position (if remembered)
            if (foundScrollView != null) {
                container.layout(container.x.toInt(), container.y.toInt(), container.x.toInt() + container.height, container.y.toInt() + container.height)
                foundScrollView.scrollY = scrollPosition
            }
        }
    }

    fun addChildAboveView(viewGroup: ViewGroup, child: View, referencedView: View) {
        val childCount = viewGroup.childCount
        for (i in 0 until childCount) {
            if (viewGroup.getChildAt(i) === referencedView) {
                if (i + 1 < childCount) {
                    viewGroup.addView(child, i + 1)
                } else {
                    viewGroup.addView(child)
                }
                break
            }
        }
    }

    fun addChildUnderView(viewGroup: ViewGroup, child: View, referencedView: View) {
        val childCount = viewGroup.childCount
        for (i in 0 until childCount) {
            if (viewGroup.getChildAt(i) === referencedView) {
                viewGroup.addView(child, i)
                break
            }
        }
    }


    // --
    // Easy reference binding
    // --

    fun bindRef(mapUtil: InflatorMapUtil, view: View?, attributes: Map<String, Any>, binder: InflatorBinder?) {
        if (binder != null && view != null) {
            val refId = mapUtil.optionalString(attributes, "refId", null)
            if (refId != null) {
                binder.onBind(refId, view)
            }
        }
    }


    // --
    // Easy localization with fallback
    // --

    fun localizedString(context: Context, localizedKey: String?, fallbackString: String?): String? {
        return localizedKey?.localized(context) ?: fallbackString
    }


    // --
    // Waiting for view helpers
    // --

    fun waitViewLayout(view: View?, completion: () -> Unit, timeout: () -> Unit, maxIterations: Int = 8) {
        // If no iterations are left, time out
        if (maxIterations == 0) {
            timeout()
            return
        }

        // Check view state, complete or try again
        if (view?.width ?: 0 == 0 || view?.height ?: 0 == 0) {
            GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
                delay(1)
                ViewletUtil.waitViewLayout(view, completion, timeout, maxIterations - 1)
            }
        } else {
            completion()
        }
    }


    // --
    // Shared generic view handling
    // --

    fun applyGenericViewAttributes(mapUtil: InflatorMapUtil, view: View, attributes: Map<String, Any>) {
        // Visibility
        val visibility = mapUtil.optionalString(attributes, "visibility", "")
        view.visibility = when(visibility) {
            "hidden" -> View.GONE
            "invisible" -> View.INVISIBLE
            else -> View.VISIBLE
        }
        view.isEnabled = !mapUtil.optionalBoolean(attributes, "disabled", false)

        // TODO: ignore background color for buttons and text entry
        view.setBackgroundColor(mapUtil.optionalColor(attributes, "backgroundColor", 0))

        // Padding
        var defaultPadding = Arrays.asList(0, 0, 0, 0)
        val paddingArray = mapUtil.optionalDimensionList(attributes, "padding")
        if (paddingArray.size == 4) {
            defaultPadding = paddingArray
        }
        // TODO: ignore padding for text entry
        view.setPadding(
            mapUtil.optionalDimension(attributes, "paddingLeft", defaultPadding[0]),
            mapUtil.optionalDimension(attributes, "paddingTop", defaultPadding[1]),
            mapUtil.optionalDimension(attributes, "paddingRight", defaultPadding[2]),
            mapUtil.optionalDimension(attributes, "paddingBottom", defaultPadding[3])
        )

        // Capture touch
        // TODO: ignore block touch for containers
        view.isClickable = mapUtil.optionalBoolean(attributes, "blockTouch", false)
    }


    // --
    // Shared layout parameters handling
    // --

    fun applyLayoutAttributes(mapUtil: InflatorMapUtil, view: View, attributes: Map<String, Any>) {
        // Margin
        val layoutParams = if (view.layoutParams is UniLayoutParams) {
            view.layoutParams as UniLayoutParams
        } else {
            UniLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        var defaultMargin = Arrays.asList(0, 0, 0, 0)
        val marginArray = mapUtil.optionalDimensionList(attributes, "margin")
        if (marginArray.size == 4) {
            defaultMargin = marginArray
        }
        layoutParams.leftMargin = mapUtil.optionalDimension(attributes, "marginLeft", defaultMargin[0])
        layoutParams.topMargin = mapUtil.optionalDimension(attributes, "marginTop", defaultMargin[1])
        layoutParams.rightMargin = mapUtil.optionalDimension(attributes, "marginRight", defaultMargin[2])
        layoutParams.bottomMargin = mapUtil.optionalDimension(attributes, "marginBottom", defaultMargin[3])
        layoutParams.spacingMargin = mapUtil.optionalDimension(attributes, "marginSpacing", 0)

        // Forced size or stretching
        val widthString = mapUtil.optionalString(attributes, "width", "")
        val heightString = mapUtil.optionalString(attributes, "height", "")
        layoutParams.width = when(widthString) {
            "stretchToParent" -> ViewGroup.LayoutParams.MATCH_PARENT
            "fitContent" -> ViewGroup.LayoutParams.WRAP_CONTENT
            else -> mapUtil.optionalDimension(attributes, "width", ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        layoutParams.height = when(heightString) {
            "stretchToParent" -> ViewGroup.LayoutParams.MATCH_PARENT
            "fitContent" -> ViewGroup.LayoutParams.WRAP_CONTENT
            else -> mapUtil.optionalDimension(attributes, "height", ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        // Size limits and weight
        layoutParams.minWidth = mapUtil.optionalDimension(attributes, "minWidth", 0)
        layoutParams.maxWidth = mapUtil.optionalDimension(attributes, "maxWidth", 0xFFFFFF)
        layoutParams.minHeight = mapUtil.optionalDimension(attributes, "minHeight", 0)
        layoutParams.maxHeight = mapUtil.optionalDimension(attributes, "maxHeight", 0xFFFFFF)
        layoutParams.weight = mapUtil.optionalFloat(attributes, "weight", 0f)

        // Gravity
        layoutParams.horizontalGravity = optionalHorizontalGravity(mapUtil, attributes, 0.0f)
        layoutParams.verticalGravity = optionalVerticalGravity(mapUtil, attributes, 0.0f)
        view.layoutParams = layoutParams
    }


    // --
    // Viewlet property helpers
    // --

    fun optionalHorizontalGravity(mapUtil: InflatorMapUtil, attributes: Map<String, Any>, defaultValue: Float): Float {
        // Extract horizontal gravity from shared horizontal/vertical string
        var gravityString: String? = null
        if (attributes["gravity"] is String) {
            gravityString = attributes["gravity"] as String
        }
        if (gravityString != null) {
            if (gravityString == "center" || gravityString == "centerHorizontal") {
                return 0.5f
            } else if (gravityString == "left") {
                return 0.0f
            } else if (gravityString == "right") {
                return 1.0f
            }
            return defaultValue
        }

        // Check horizontal gravity being specified separately
        var horizontalGravityString: String? = null
        if (attributes["horizontalGravity"] is String) {
            horizontalGravityString = attributes["horizontalGravity"] as String
        }
        if (horizontalGravityString != null) {
            return when (horizontalGravityString) {
                "center" -> 0.5f
                "left" -> 0.0f
                "right" -> 1.0f
                else -> defaultValue
            }
        }
        return mapUtil.optionalFloat(attributes, "horizontalGravity", defaultValue)
    }

    fun optionalVerticalGravity(mapUtil: InflatorMapUtil, attributes: Map<String, Any>, defaultValue: Float): Float {
        // Extract horizontal gravity from shared horizontal/vertical string
        var gravityString: String? = null
        if (attributes["gravity"] is String) {
            gravityString = attributes["gravity"] as String
        }
        if (gravityString != null) {
            if (gravityString == "center" || gravityString == "centerVertical") {
                return 0.5f
            } else if (gravityString == "top") {
                return 0.0f
            } else if (gravityString == "bottom") {
                return 1.0f
            }
            return defaultValue
        }

        // Check horizontal gravity being specified separately
        var verticalGravityString: String? = null
        if (attributes["verticalGravity"] is String) {
            verticalGravityString = attributes["verticalGravity"] as String
        }
        if (verticalGravityString != null) {
            return when (verticalGravityString) {
                "center" -> 0.5f
                "top" -> 0.0f
                "bottom" -> 1.0f
                else -> defaultValue
            }
        }
        return mapUtil.optionalFloat(attributes, "verticalGravity", defaultValue)
    }

}

package com.crescentflare.piratesgame.components.utility


import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView

import com.crescentflare.piratesgame.BuildConfig
import com.crescentflare.piratesgame.infrastructure.coreextensions.localized
import com.crescentflare.unilayout.helpers.UniLayoutParams
import com.crescentflare.unilayout.views.UniView
import com.crescentflare.viewletcreator.ViewletCreator
import com.crescentflare.viewletcreator.binder.ViewletAnnotationBinder
import com.crescentflare.viewletcreator.binder.ViewletBinder
import com.crescentflare.viewletcreator.binder.ViewletRef
import com.crescentflare.viewletcreator.utility.ViewletMapUtil

import org.junit.Assert
import java.util.Arrays

/**
 * Shared utilities for viewlet integration, also contains the viewlet for a basic UniView
 */
object ViewletUtil {

    // ---
    // Basic view viewlet
    // ---

    val basicViewViewlet: ViewletCreator.Viewlet = object : ViewletCreator.Viewlet {

        override fun create(context: Context): View {
            return UniView(context)
        }

        override fun update(view: View, attributes: Map<String, Any>?, parent: ViewGroup?, binder: ViewletBinder?): Boolean {
            ViewletUtil.applyGenericViewAttributes(view, attributes)
            return true
        }

        override fun canRecycle(view: View, attributes: Map<String, Any>?): Boolean {
            return view is UniView
        }

    }


    // ---
    // Inflate with assertion
    // ---

    fun assertInflateOn(view: View, attributes: Map<String, Any>?, binder: ViewletBinder) {
        assertInflateOn(view, attributes, null, binder)
    }

    fun assertInflateOn(view: View, attributes: Map<String, Any>?, parent: ViewGroup?, binder: ViewletBinder) {
        val inflateResult = ViewletCreator.inflateOn(view, attributes, parent, binder)
        if (BuildConfig.DEBUG) {
            // First check if attributes are not null
            Assert.assertNotNull("Attributes are null, load issue?", attributes)

            // Check viewlet name
            val viewletName = ViewletCreator.findViewletNameInAttributes(attributes)
            Assert.assertNotNull("No viewlet found, JSON structure issue?", viewletName)

            // Check if the viewlet is registered
            Assert.assertNotNull("No viewlet implementation found, registration issue of $viewletName?", ViewletCreator.findViewletInAttributes(attributes))

            // Check result of inflate
            Assert.assertTrue("Can't inflate viewlet, class doesn't match with $viewletName?", inflateResult)

            // Check if there are any referenced views that are null
            if (binder is ViewletAnnotationBinder) {
                checkViewletRefs(view)
            }
        }
    }

    private fun checkViewletRefs(view: View) {
        for (field in view.javaClass.declaredFields) {
            for (annotation in field.declaredAnnotations) {
                if (annotation is ViewletRef) {
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


    // ---
    // Subview creation
    // ---

    fun createSubviews(container: ViewGroup, parent: ViewGroup, attributes: Map<String, Any>?, subviewItems: Any?, binder: ViewletBinder?) {
        // Check if children are the same before and after the update, then they can be updated instead of re-created
        var canRecycle = false
        val recycling = ViewletMapUtil.optionalBoolean(attributes, "recycling", false)
        val items = ViewletCreator.attributesForSubViewletList(subviewItems)
        if (recycling) {
            if (items.size == container.childCount) {
                canRecycle = true
                for (i in items.indices) {
                    if (!ViewletCreator.canRecycle(container.getChildAt(i), items[i])) {
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
                    ViewletCreator.inflateOn(child, item, parent)
                    ViewletUtil.applyLayoutAttributes(child, item)
                    ViewletUtil.bindRef(child, item, binder)
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
                val result = ViewletCreator.create(container.context, item, parent, binder)
                if (result != null) {
                    container.addView(result)
                    ViewletUtil.applyLayoutAttributes(result, item)
                    if (scrollPosition != 0 && foundScrollView == null) {
                        if (result is ScrollView) {
                            foundScrollView = result
                        }
                    }
                    ViewletUtil.bindRef(result, item, binder)
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


    // ---
    // Easy reference binding
    // ---

    private fun bindRef(view: View?, attributes: Map<String, Any>, binder: ViewletBinder?) {
        if (binder != null && view != null) {
            val refId = ViewletMapUtil.optionalString(attributes, "refId", null)
            if (refId != null) {
                binder.onBind(refId, view)
            }
        }
    }


    // ---
    // Easy localization with fallback
    // ---

    fun localizedString(context: Context, localizedKey: String?, fallbackString: String?): String? {
        return localizedKey?.localized(context) ?: fallbackString
    }


    // ---
    // Shared generic view handling
    // ---

    fun applyGenericViewAttributes(view: View, attributes: Map<String, Any>?) {
        // Visibility
        val visibility = ViewletMapUtil.optionalString(attributes, "visibility", "")
        view.visibility = when(visibility) {
            "hidden" -> View.GONE
            "invisible" -> View.INVISIBLE
            else -> View.VISIBLE
        }
        view.isEnabled = !ViewletMapUtil.optionalBoolean(attributes, "disabled", false)

        // TODO: ignore background color for buttons and text entry
        view.setBackgroundColor(ViewletMapUtil.optionalColor(attributes, "backgroundColor", 0))

        // Padding
        var defaultPadding = Arrays.asList(0, 0, 0, 0)
        val paddingArray = ViewletMapUtil.optionalDimensionList(attributes, "padding")
        if (paddingArray.size == 4) {
            defaultPadding = paddingArray
        }
        // TODO: ignore padding for text entry
        view.setPadding(
                ViewletMapUtil.optionalDimension(attributes, "paddingLeft", defaultPadding[0]),
                ViewletMapUtil.optionalDimension(attributes, "paddingTop", defaultPadding[1]),
                ViewletMapUtil.optionalDimension(attributes, "paddingRight", defaultPadding[2]),
                ViewletMapUtil.optionalDimension(attributes, "paddingBottom", defaultPadding[3])
        )

        // Capture touch
        // TODO: ignore block touch for containers
        view.isClickable = ViewletMapUtil.optionalBoolean(attributes, "blockTouch", false)
    }


    // ---
    // Shared layout parameters handling
    // ---

    private fun applyLayoutAttributes(view: View, attributes: Map<String, Any>?) {
        // Margin
        val layoutParams = if (view.layoutParams is UniLayoutParams) {
            view.layoutParams as UniLayoutParams
        } else {
            UniLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        var defaultMargin = Arrays.asList(0, 0, 0, 0)
        val marginArray = ViewletMapUtil.optionalDimensionList(attributes, "margin")
        if (marginArray.size == 4) {
            defaultMargin = marginArray
        }
        layoutParams.leftMargin = ViewletMapUtil.optionalDimension(attributes, "marginLeft", defaultMargin[0])
        layoutParams.topMargin = ViewletMapUtil.optionalDimension(attributes, "marginTop", defaultMargin[1])
        layoutParams.rightMargin = ViewletMapUtil.optionalDimension(attributes, "marginRight", defaultMargin[2])
        layoutParams.bottomMargin = ViewletMapUtil.optionalDimension(attributes, "marginBottom", defaultMargin[3])
        layoutParams.spacingMargin = ViewletMapUtil.optionalDimension(attributes, "marginSpacing", 0)

        // Forced size or stretching
        val widthString = ViewletMapUtil.optionalString(attributes, "width", "")
        val heightString = ViewletMapUtil.optionalString(attributes, "height", "")
        layoutParams.width = when(widthString) {
            "stretchToParent" -> ViewGroup.LayoutParams.MATCH_PARENT
            "fitContent" -> ViewGroup.LayoutParams.WRAP_CONTENT
            else -> ViewletMapUtil.optionalDimension(attributes, "width", ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        layoutParams.height = when(heightString) {
            "stretchToParent" -> ViewGroup.LayoutParams.MATCH_PARENT
            "fitContent" -> ViewGroup.LayoutParams.WRAP_CONTENT
            else -> ViewletMapUtil.optionalDimension(attributes, "height", ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        // Size limits and weight
        layoutParams.minWidth = ViewletMapUtil.optionalDimension(attributes, "minWidth", 0)
        layoutParams.maxWidth = ViewletMapUtil.optionalDimension(attributes, "maxWidth", 0xFFFFFF)
        layoutParams.minHeight = ViewletMapUtil.optionalDimension(attributes, "minHeight", 0)
        layoutParams.maxHeight = ViewletMapUtil.optionalDimension(attributes, "maxHeight", 0xFFFFFF)
        layoutParams.weight = ViewletMapUtil.optionalFloat(attributes, "weight", 0f)

        // Gravity
        layoutParams.horizontalGravity = optionalHorizontalGravity(attributes, 0.0f)
        layoutParams.verticalGravity = optionalVerticalGravity(attributes, 0.0f)
        view.layoutParams = layoutParams
    }


    // ---
    // Viewlet property helpers
    // ---

    private fun optionalHorizontalGravity(attributes: Map<String, Any>?, defaultValue: Float): Float {
        // Extract horizontal gravity from shared horizontal/vertical string
        if (attributes != null) {
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
        }
        return ViewletMapUtil.optionalFloat(attributes, "horizontalGravity", defaultValue)
    }

    private fun optionalVerticalGravity(attributes: Map<String, Any>?, defaultValue: Float): Float {
        // Extract horizontal gravity from shared horizontal/vertical string
        if (attributes != null) {
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
        }
        return ViewletMapUtil.optionalFloat(attributes, "verticalGravity", defaultValue)
    }

}

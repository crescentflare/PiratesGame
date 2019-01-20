package com.crescentflare.piratesgame.components.simpleviewlets

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.piratesgame.infrastructure.uri.ImageURI
import com.crescentflare.unilayout.views.UniImageView
import com.crescentflare.viewletcreator.ViewletCreator
import com.crescentflare.viewletcreator.binder.ViewletBinder
import com.crescentflare.viewletcreator.utility.ViewletMapUtil

/**
 * Basic view viewlet: an image view
 */
object ImageViewlet {

    // ---
    // Viewlet instance
    // ---

    val viewlet: ViewletCreator.Viewlet = object : ViewletCreator.Viewlet {

        override fun create(context: Context): View {
            return UniImageView(context)
        }

        override fun update(view: View, attributes: Map<String, Any>, parent: ViewGroup?, binder: ViewletBinder?): Boolean {
            if (view is UniImageView) {
                // Set image
                applyImageURI(view, ImageURI(ViewletMapUtil.optionalString(attributes, "uri", null)))

                // Scale factor
                val scaleType = ScaleType.fromString(ViewletMapUtil.optionalString(attributes, "scaleType", ""))
                view.scaleType = scaleType.toImageViewScaleType()

                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(view, attributes)
                return true
            }
            return false
        }

        override fun canRecycle(view: View, attributes: Map<String, Any>): Boolean {
            return view is UniImageView
        }

    }


    // ---
    // Helpers
    // ---

    fun applyImageURI(imageView: UniImageView?, uri: ImageURI?): Boolean {
        if (imageView != null) {
            if (uri != null) {
                val imageResource = uri.getInternalImageResource(imageView.context)
                if (imageResource > 0) {
                    imageView.setImageResource(imageResource)
                    if (uri.tintColor != 0) {
                        imageView.colorFilter = PorterDuffColorFilter(uri.tintColor, PorterDuff.Mode.SRC_IN)
                    } else {
                        imageView.colorFilter = null
                    }
                    return true
                }
            }
            imageView.colorFilter = null
            imageView.setImageResource(0)
        }
        return false
    }


    // ---
    // Scale type enum
    // ---

    enum class ScaleType(val value: String) {

        FitCenter("fitCenter"),
        Stretch("stretch"),
        ScaleFit("scaleFit"),
        ScaleCrop("scaleCrop");

        fun toImageViewScaleType(): ImageView.ScaleType {
            return when(this) {
                Stretch -> ImageView.ScaleType.FIT_XY
                ScaleFit -> ImageView.ScaleType.CENTER_INSIDE
                ScaleCrop -> ImageView.ScaleType.CENTER_CROP
                else -> ImageView.ScaleType.FIT_CENTER
            }
        }

        companion object {

            fun fromString(string: String?): ScaleType {
                for (enum in ScaleType.values()) {
                    if (enum.value == string) {
                        return enum
                    }
                }
                return FitCenter
            }

        }

    }

}

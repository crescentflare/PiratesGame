package com.crescentflare.piratesgame.components.simpleviewlets

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.crescentflare.piratesgame.components.utility.CustomNinePatchDrawable
import com.crescentflare.piratesgame.components.utility.CustomThreePatchDrawable
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.piratesgame.components.utility.ImageSource
import com.crescentflare.unilayout.views.UniImageView
import com.crescentflare.viewletcreator.ViewletCreator
import com.crescentflare.viewletcreator.binder.ViewletBinder
import com.crescentflare.viewletcreator.utility.ViewletMapUtil
import com.squareup.picasso.Picasso


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
                applyImageSource(view, ImageSource(ViewletMapUtil.optionalString(attributes, "source", null)))

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

    fun applyImageSource(imageView: UniImageView?, source: ImageSource?): Boolean {
        if (imageView != null) {
            if (source != null) {
                source.onlinePath?.let {
                    Picasso.get().load(it).into(imageView)
                } ?: run {
                    val imageResource = source.getInternalImageResource(imageView.context)
                    if (imageResource > 0) {
                        if (source.threePatch > 0) {
                            imageView.setImageDrawable(prepareThreePatch(imageView.context, imageResource, source.threePatch))
                        } else if (source.ninePatch > 0) {
                            imageView.setImageDrawable(prepareNinePatch(imageView.context, imageResource, source.ninePatch))
                        } else {
                            imageView.setImageResource(imageResource)
                        }
                        if (source.tintColor != 0) {
                            imageView.colorFilter = PorterDuffColorFilter(source.tintColor, PorterDuff.Mode.SRC_IN)
                        } else {
                            imageView.colorFilter = null
                        }
                        return true
                    }
                }
            }
            imageView.colorFilter = null
            imageView.setImageResource(0)
        }
        return false
    }

    private fun prepareThreePatch(context: Context, resourceId: Int, edgeInset: Int): Drawable? {
        val resourceDrawable = ContextCompat.getDrawable(context, resourceId)
        if (resourceDrawable is BitmapDrawable) {
            val bitmap = resourceDrawable.bitmap
            if (bitmap != null) {
                return CustomThreePatchDrawable(bitmap, edgeInset)
            }
        }
        return null
    }

    private fun prepareNinePatch(context: Context, resourceId: Int, edgesInset: Int): Drawable? {
        val resourceDrawable = ContextCompat.getDrawable(context, resourceId)
        if (resourceDrawable is BitmapDrawable) {
            val bitmap = resourceDrawable.bitmap
            if (bitmap != null) {
                return CustomNinePatchDrawable(bitmap, edgesInset, edgesInset)
            }
        }
        return null
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

package com.crescentflare.piratesgame.components.simpleviewlets

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.crescentflare.piratesgame.components.utility.CustomNinePatchDrawable
import com.crescentflare.piratesgame.components.utility.CustomThreePatchDrawable
import com.crescentflare.piratesgame.components.utility.ImageSource
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.unilayout.views.UniImageView
import com.crescentflare.jsoninflator.JsonInflatable
import com.crescentflare.jsoninflator.binder.InflatorBinder
import com.crescentflare.jsoninflator.utility.InflatorMapUtil


/**
 * Basic view viewlet: an image view
 */
object ImageViewlet {

    // --
    // Viewlet instance
    // --

    val viewlet: JsonInflatable = object : JsonInflatable {

        override fun create(context: Context): Any {
            return UniImageView(context)
        }

        override fun update(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>, parent: Any?, binder: InflatorBinder?): Boolean {
            if (obj is UniImageView) {
                // Set image
                applyImageSource(obj, ImageSource.fromObject(attributes["source"]))

                // Scale factor
                val scaleType = ScaleType.fromString(mapUtil.optionalString(attributes, "scaleType", ""))
                obj.scaleType = scaleType.toImageViewScaleType()

                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(mapUtil, obj, attributes)
                return true
            }
            return false
        }

        override fun canRecycle(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>): Boolean {
            return obj is UniImageView
        }

    }


    // --
    // Helpers
    // --

    fun applyImageSource(imageView: UniImageView?, source: ImageSource?): Boolean {
        if (imageView != null) {
            if (source != null) {
                source.onlineUri?.let {
                    if (source.type == ImageSource.Type.DevServerImage) {
                        val scale = Resources.getSystem().displayMetrics.density / 4f // Place xxxhdpi images in the dev server
                        Glide.with(imageView).load(it).apply(RequestOptions().sizeMultiplier(scale)).into(imageView)
                    } else {
                        Glide.with(imageView).load(it).into(imageView)
                    }
                    if (source.tintColor != 0) {
                        imageView.colorFilter = PorterDuffColorFilter(source.tintColor, PorterDuff.Mode.SRC_IN)
                    } else {
                        imageView.colorFilter = null
                    }
                    return true
                } ?: run {
                    val drawable = source.getDrawable(imageView.context)
                    if (drawable != null) {
                        if (source.threePatch > 0) {
                            imageView.setImageDrawable(prepareThreePatch(drawable, source.threePatch))
                        } else if (source.ninePatch > 0) {
                            imageView.setImageDrawable(prepareNinePatch(drawable, source.ninePatch))
                        } else {
                            imageView.setImageDrawable(drawable)
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

    private fun prepareThreePatch(drawable: Drawable, edgeInset: Int): Drawable? {
        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            if (bitmap != null) {
                return CustomThreePatchDrawable(bitmap, edgeInset)
            }
        }
        return null
    }

    private fun prepareNinePatch(drawable: Drawable, edgesInset: Int): Drawable? {
        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            if (bitmap != null) {
                return CustomNinePatchDrawable(bitmap, edgesInset, edgesInset)
            }
        }
        return null
    }


    // --
    // Scale type enum
    // --

    enum class ScaleType(val value: String) {

        Center("center"),
        Stretch("stretch"),
        ScaleFit("scaleFit"),
        ScaleCrop("scaleCrop");

        fun toImageViewScaleType(): ImageView.ScaleType {
            return when(this) {
                Stretch -> ImageView.ScaleType.FIT_XY
                ScaleFit -> ImageView.ScaleType.FIT_CENTER
                ScaleCrop -> ImageView.ScaleType.CENTER_CROP
                else -> ImageView.ScaleType.CENTER_INSIDE
            }
        }

        companion object {

            fun fromString(string: String?): ScaleType {
                for (enum in ScaleType.values()) {
                    if (enum.value == string) {
                        return enum
                    }
                }
                return Center
            }

        }

    }

}

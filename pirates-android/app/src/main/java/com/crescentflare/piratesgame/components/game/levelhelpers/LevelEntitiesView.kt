package com.crescentflare.piratesgame.components.game.levelhelpers

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import com.crescentflare.piratesgame.R
import com.crescentflare.unilayout.views.UniImageView

/**
 * LevelView helper: a layer for showing entities, like a boat
 */
class LevelEntitiesView : ViewGroup {

    // --
    // Statics
    // --

    companion object {

        val playerBoatCharacter = 'P'
        val enemyBoatCharacter = 'E'

    }


    // --
    // Members
    // --

    var mapWidth = 0

    var tiles = mutableListOf<String>()
        set(tiles) {
            field = tiles
            recreateTileViews()
        }


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
        // No implementation
    }


    // --
    // Creating tiles
    // --

    private fun recreateTileViews() {
        // Clear existing tiles
        removeAllViews()

        // Determine map width
        mapWidth = 0
        for (string in tiles) {
            mapWidth = Math.max(mapWidth, string.length)
        }

        // Create rock tile views
        for (rowIndex in tiles.indices) {
            val lineMap = tiles[rowIndex]
            for (characterIndex in lineMap.indices) {
                val character = lineMap[characterIndex]
                if (character == playerBoatCharacter || character == enemyBoatCharacter) {
                    val imageView = UniImageView(context)
                    imageView.setImageResource(if (character == playerBoatCharacter) R.drawable.entity_boat_side_player else R.drawable.entity_boat_side_enemy)
                    imageView.scaleType = ImageView.ScaleType.FIT_XY
                    imageView.tag = rowIndex * 1024 + characterIndex
                    addView(imageView)
                }
            }
        }
    }


    // --
    // Custom layout
    // --

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (mapWidth > 0) {
            val tileSize = (right - left) / mapWidth
            val childCount = childCount
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                val childPosition = child.tag as? Int ?: 0
                val xTile = childPosition % 1024
                val yTile = childPosition / 1024
                var imageWidth = 0
                var imageHeight = 0
                val drawable = (child as? UniImageView)?.drawable
                if (drawable != null) {
                    val referenceSize = (Resources.getSystem().displayMetrics.density * 64).toInt()
                    imageWidth = drawable.intrinsicWidth * tileSize / referenceSize
                    imageHeight = drawable.intrinsicHeight * tileSize / referenceSize
                }
                val x = (xTile * tileSize + tileSize * 0.5f - imageWidth / 2).toInt()
                val y = (yTile * tileSize + tileSize * 0.54f - imageHeight / 2).toInt()
                child.layout(x, y, x + imageWidth, y + imageHeight)
            }
        }
    }

}

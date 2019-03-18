package com.crescentflare.piratesgame.components.game.levelhelpers

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import com.crescentflare.piratesgame.R
import com.crescentflare.unilayout.views.UniImageView

/**
 * LevelView helper: a map for rendering tiles
 */
class LevelTileMapView : ViewGroup {

    // --
    // Statics
    // --

    companion object {

        val rockTileCharacter = '#'

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
                if (character == rockTileCharacter) {
                    val imageView = UniImageView(context)
                    imageView.setImageResource(R.drawable.tile_rock)
                    imageView.scaleType = ImageView.ScaleType.FIT_XY
                    imageView.tag = rowIndex * 1024 + characterIndex
                    addView(imageView)
                }
            }
        }
    }


    // --
    // Check for a blocking tile
    // --

    fun isOccupied(tileX: Int, tileY: Int): Boolean {
        if (tileY >= 0 && tileY < tiles.size) {
            val string = tiles[tileY]
            if (tileX >= 0 && tileX < string.length) {
                return string[tileX] == rockTileCharacter
            }
        }
        return false
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
                var ratio = 1f
                val drawable = (child as? UniImageView)?.drawable
                if (drawable != null && drawable.intrinsicWidth > 0) {
                    ratio = drawable.intrinsicHeight.toFloat() / drawable.intrinsicWidth
                }
                child.layout(xTile * tileSize, yTile * tileSize, (xTile + 1) * tileSize, yTile * tileSize + (tileSize * ratio).toInt())
            }
        }
    }

}

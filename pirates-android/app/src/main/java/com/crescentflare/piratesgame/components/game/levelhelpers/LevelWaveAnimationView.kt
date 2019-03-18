package com.crescentflare.piratesgame.components.game.levelhelpers

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.crescentflare.piratesgame.R
import com.crescentflare.unilayout.views.UniImageView

/**
 * LevelView helper: a view containing animated wave particles
 */
class LevelWaveAnimationView : ViewGroup {

    // --
    // Members
    // --

    var tileMapView: LevelTileMapView? = null
    private var visibleWaves = mutableListOf<Point>()


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
    // Spawn a wave
    // --

    fun spawnRandomWave(maxIterations: Int = 4) {
        val mapWidth = tileMapView?.mapWidth ?: 0
        val mapHeight = Math.ceil(mapWidth.toDouble() * height.toDouble() / width.toDouble()).toInt()
        if (mapWidth > 0) {
            val halfTileX = -1 + (Math.floor(Math.random() * (mapWidth * 2 + 3).toDouble())).toInt()
            val halfTileY = (Math.floor(Math.random() * (mapHeight * 2).toDouble())).toInt()
            if (!isOccupied(halfTileX, halfTileY)) {
                spawnWave(halfTileX, halfTileY)
                return
            }
        }
        if (maxIterations > 0) {
            spawnRandomWave(maxIterations - 1)
        }
    }

    private fun spawnWave(halfTileX: Int, halfTileY: Int) {
        val mapWidth = tileMapView?.mapWidth ?: 0
        val particleDrawable = ContextCompat.getDrawable(context, R.drawable.particle_wave)
        if (particleDrawable != null && mapWidth > 0) {
            // Prepare the particle
            val tileSize = width / mapWidth
            val waveHeight = tileSize * particleDrawable.intrinsicHeight / particleDrawable.intrinsicWidth
            val particle = ImageView(context)
            particle.setImageDrawable(particleDrawable)
            particle.tag = halfTileY * 1024 + halfTileX
            particle.scaleX = 1.5f
            particle.scaleY = 0.1f
            particle.alpha = 0f

            // Add to the visible wave list to prevent overlapping waves
            visibleWaves.add(Point(halfTileX, halfTileY))

            // Prepare animation
            val distance = tileSize.toFloat()
            val duration = 4000 + (Math.random() * 1000).toLong()
            val animation = AnimatorSet()
            val scale = 1f - Math.random().toFloat() * 0.5f
            animation.playTogether(
                ObjectAnimator.ofFloat(particle, View.ALPHA, 0f, 1f, 0f),
                ObjectAnimator.ofFloat(particle, View.SCALE_X, 1.5f, 1f, 1.5f),
                ObjectAnimator.ofFloat(particle, View.SCALE_Y, 0.1f, scale, 0.1f),
                ObjectAnimator.ofFloat(particle, View.TRANSLATION_X, 0f, -distance),
                ObjectAnimator.ofFloat(particle, View.TRANSLATION_Y, 0f, -0.75f * waveHeight * scale, 0f)
            )
            animation.duration = duration
            animation.startDelay = 0
            animation.interpolator = LinearInterpolator()
            animation.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    // No implementation
                }

                override fun onAnimationRepeat(animation: Animator) {
                    // No implementation
                }

                override fun onAnimationEnd(animation: Animator) {
                    removeView(particle)
                    removeVisibleWave(halfTileX, halfTileY)
                }

                override fun onAnimationCancel(animation: Animator) {
                    removeView(particle)
                    removeVisibleWave(halfTileX, halfTileY)
                }
            })
            animation.start()

            // Add view
            addView(particle)
        }
    }


    // --
    // Check for spawning in available places
    // --

    private fun isOccupied(halfTileX: Int, halfTileY: Int): Boolean {
        // The animation should not be overlapping with a tile
        val tileMapView = tileMapView
        if (tileMapView != null) {
            val checkPositions = listOf(halfTileX - 2, halfTileX -1, halfTileX, halfTileX + 1)
            for (checkX in checkPositions) {
                if (tileMapView.isOccupied(checkX / 2, halfTileY / 2)) {
                    return true
                }
            }
        }

        // The animation should not be overlapping an existing wave
        for (visibleWave in visibleWaves) {
            if (visibleWave.y == halfTileY) {
                val checkPositions = listOf(halfTileX - 2, halfTileX - 1, halfTileX, halfTileX + 1)
                val checkPositions2 = listOf(visibleWave.x - 2, visibleWave.x - 1, visibleWave.x, visibleWave.x + 1)
                for (checkX in checkPositions) {
                    for (checkX2 in checkPositions2) {
                        if (checkX == checkX2) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    private fun removeVisibleWave(halfTileX: Int, halfTileY: Int) {
        for (waveIndex in visibleWaves.indices) {
            val checkWave = visibleWaves[waveIndex]
            if (checkWave.x == halfTileX && checkWave.y == halfTileY) {
                visibleWaves.removeAt(waveIndex)
                break
            }
        }
    }


    // --
    // Custom layout
    // --

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val mapWidth = tileMapView?.mapWidth ?: 0
        if (mapWidth > 0) {
            val tileSize = (right - left) / mapWidth
            val halfTileSize = tileSize / 2
            val childCount = childCount
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                val childPosition = child.tag as? Int ?: 0
                val xHalfTile = childPosition % 1024
                val yHalfTile = childPosition / 1024
                var ratio = 1f
                val drawable = (child as? UniImageView)?.drawable
                if (drawable != null && drawable.intrinsicWidth > 0) {
                    ratio = drawable.intrinsicHeight.toFloat() / drawable.intrinsicWidth
                }
                child.layout(xHalfTile * halfTileSize, yHalfTile * halfTileSize, xHalfTile * halfTileSize + tileSize, yHalfTile * halfTileSize + (tileSize * ratio).toInt())
            }
        }
    }

}

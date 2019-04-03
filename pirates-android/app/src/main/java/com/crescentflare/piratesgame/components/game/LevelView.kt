package com.crescentflare.piratesgame.components.game

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.ViewGroup
import com.crescentflare.piratesgame.components.containers.FrameContainerView
import com.crescentflare.piratesgame.components.game.levelhelpers.LevelEntitiesView
import com.crescentflare.piratesgame.components.game.levelhelpers.LevelTileMapView
import com.crescentflare.piratesgame.components.game.levelhelpers.LevelWaveAnimationView
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.infrastructure.events.AppEventObserver
import com.crescentflare.unilayout.helpers.UniLayoutParams
import com.crescentflare.jsoninflator.JsonInflatable
import com.crescentflare.jsoninflator.binder.InflatorBinder
import com.crescentflare.jsoninflator.utility.InflatorMapUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Game view: a level
 */
class LevelView : FrameContainerView {

    // --
    // Static: viewlet integration
    // --

    companion object {

        val viewlet: JsonInflatable = object : JsonInflatable {

            override fun create(context: Context): Any {
                return LevelView(context)
            }

            override fun update(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>, parent: Any?, binder: InflatorBinder?): Boolean {
                if (obj is LevelView) {
                    // Apply animation
                    obj.waveSpawnInterval = mapUtil.optionalFloat(attributes, "waveSpawnInterval", 0.2f)

                    // Set tiles
                    obj.tileMap = mapUtil.optionalStringList(attributes, "tileMap")

                    // Generic view properties
                    ViewletUtil.applyGenericViewAttributes(mapUtil, obj, attributes)

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
                return obj is LevelView
            }

        }

    }


    // --
    // Members
    // --

    private val tileMapView: LevelTileMapView
    private val entitiesView: LevelEntitiesView
    private val waveAnimationView: LevelWaveAnimationView
    private var isAttached = false
    private var spawnIdle = true


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
        // Add tile map view
        tileMapView = LevelTileMapView(context)
        tileMapView.layoutParams = UniLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        addView(tileMapView)

        // Add wave animation view
        waveAnimationView = LevelWaveAnimationView(context)
        waveAnimationView.layoutParams = UniLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        waveAnimationView.tileMapView = tileMapView
        addView(waveAnimationView)

        // Add entities view
        entitiesView = LevelEntitiesView(context)
        entitiesView.layoutParams = UniLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        addView(entitiesView)

        // Start spawning wave particles
        continueWaveSpawning()
    }


    // --
    // Configurable values
    // --

    var waveSpawnInterval = 0.2f

    var tileMap: MutableList<String>
        set(tileMap) {
            tileMapView.tiles = tileMap
            entitiesView.tiles = tileMap
        }
        get() = tileMapView.tiles


    // --
    // Spawning wave particles
    // --

    private fun continueWaveSpawning() {
        spawnIdle = false
        GlobalScope.launch(Dispatchers.Main) {
            delay((waveSpawnInterval * 1000).toLong())
            waveAnimationView.spawnRandomWave()
            spawnIdle = true
            if (isAttached) {
                continueWaveSpawning()
            }
        }
    }


    // --
    // Listen for attachment/detachment
    // --

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isAttached = false
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isAttached = true
        if (spawnIdle) {
            continueWaveSpawning()
        }
    }

}

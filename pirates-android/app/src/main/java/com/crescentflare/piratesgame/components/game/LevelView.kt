package com.crescentflare.piratesgame.components.game

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.crescentflare.piratesgame.components.containers.FrameContainerView
import com.crescentflare.piratesgame.components.game.levelhelpers.LevelTileMapView
import com.crescentflare.piratesgame.components.game.levelhelpers.LevelWaveAnimationView
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.infrastructure.events.AppEventObserver
import com.crescentflare.unilayout.helpers.UniLayoutParams
import com.crescentflare.viewletcreator.ViewletCreator
import com.crescentflare.viewletcreator.binder.ViewletBinder
import com.crescentflare.viewletcreator.utility.ViewletMapUtil
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

        val viewlet: ViewletCreator.Viewlet = object : ViewletCreator.Viewlet {

            override fun create(context: Context): View {
                return LevelView(context)
            }

            override fun update(view: View, attributes: Map<String, Any>, parent: ViewGroup?, binder: ViewletBinder?): Boolean {
                if (view is LevelView) {
                    // Apply animation
                    view.waveSpawnInterval = ViewletMapUtil.optionalFloat(attributes, "waveSpawnInterval", 0.2f)

                    // Set tiles
                    view.tileMap = ViewletMapUtil.optionalStringList(attributes, "tileMap")

                    // Generic view properties
                    ViewletUtil.applyGenericViewAttributes(view, attributes)

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
                return view is LevelView
            }

        }

    }


    // --
    // Members
    // --

    private val tileMapView: LevelTileMapView
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
        // Add tile map and wave animation views
        tileMapView = LevelTileMapView(context)
        tileMapView.layoutParams = UniLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        addView(tileMapView)
        waveAnimationView = LevelWaveAnimationView(context)
        waveAnimationView.layoutParams = UniLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        waveAnimationView.tileMapView = tileMapView
        addView(waveAnimationView)

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

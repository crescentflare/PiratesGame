package com.crescentflare.piratesgame.page.modules.splash

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.crescentflare.jsoninflator.JsonInflatable
import com.crescentflare.jsoninflator.binder.InflatorBinder
import com.crescentflare.jsoninflator.binder.InflatorMapBinder
import com.crescentflare.jsoninflator.utility.InflatorMapUtil
import com.crescentflare.piratesgame.components.compoundviews.SplashLoadingBar
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.infrastructure.events.AppEventObserver
import com.crescentflare.piratesgame.page.storage.Page
import com.crescentflare.piratesgame.page.modules.ControllerModule
import kotlinx.coroutines.*
import java.lang.ref.WeakReference

/**
 * Splash module: handles loading and updating the loading bar component
 */
class SplashLoaderModule: ControllerModule {

    // --
    // Static: inflatable integration
    // --

    companion object {

        val inflatable: JsonInflatable = object : JsonInflatable {

            override fun create(context: Context): Any {
                val module = SplashLoaderModule()
                module.onCreate(context)
                return module
            }

            override fun update(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>, parent: Any?, binder: InflatorBinder?): Boolean {
                return obj is SplashLoaderModule
            }

            override fun canRecycle(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>): Boolean {
                return obj is SplashLoaderModule
            }

        }

    }


    // --
    // Members
    // --

    override val eventType = "splashLoader"
    private var context: WeakReference<Context>? = null
    private var binder: InflatorMapBinder? = null
    private var loadingTasks = mutableListOf<LoadingTask>()
    private var busy = false
    private var preparing = false
    private var done = false


    // --
    // Lifecycle
    // --

    override fun onCreate(context: Context) {
        // Keep weak reference to context
        this.context = WeakReference(context)

        // Add loading tasks
        loadingTasks.add(LoadingTask {
            // Template for a loading task
        })
    }

    override fun onResume() {
        // No implementation
    }

    override fun onPause() {
        // No implementation
    }


    // --
    // Page updates
    // --

    override fun onPageUpdated(page: Page, binder: InflatorMapBinder) {
        this.binder = binder
        if (busy) {
            showLoading(false)
            if (done) {
                (binder.findByReference("loadingBar") as? SplashLoadingBar)?.progress = 1f
            }
        }
    }


    // --
    // Event handling
    // --

    override fun catchEvent(event: AppEvent, sender: Any?): Boolean {
        if (event.rawType == eventType) {
            if (!preparing) {
                preparing = true
                startLoading()
                return true
            }
        }
        return false
    }


    // --
    // Loading
    // --

    private fun startLoading() {
        GlobalScope.launch(Dispatchers.Main) {
            delay(1000)
            busy = true
            showLoading()
            startLoadTask {
                GlobalScope.launch(Dispatchers.Main) {
                    val bar = binder?.findByReference("loadingBar") as? SplashLoadingBar
                    if (bar?.autoAnimation == true) {
                        delay(SplashLoadingBar.animationDuration)
                    }
                    val eventObserver = context?.get() as? AppEventObserver
                    eventObserver?.observedEvent(AppEvent("navigate://summary?type=replace"), null)
                    done = true
                }
            }
        }
    }

    private fun startLoadTask(index: Int = 0, completion: () -> Unit) {
        if (context?.get() == null) {
            return
        }
        if (index >= loadingTasks.size) {
            completion()
            return
        }
        loadingTasks[index].start {
            val bar = binder?.findByReference("loadingBar") as? SplashLoadingBar
            bar?.progress = (index + 1).toFloat() / loadingTasks.size.toFloat()
            startLoadTask(index + 1, completion)
        }
    }

    private fun showLoading(animated: Boolean = true) {
        val bar = binder?.findByReference("loadingBar") as? View
        val text = binder?.findByReference("loadingText") as? View
        bar?.visibility = View.VISIBLE
        text?.visibility = View.VISIBLE
        if (animated) {
            if (bar != null && text != null) {
                val animation = AnimatorSet()
                bar.alpha = 0f
                text.alpha = 0f
                animation.playTogether(
                    ObjectAnimator.ofFloat(bar, View.ALPHA, 0f, 1f),
                    ObjectAnimator.ofFloat(text, View.ALPHA, 0f, 1f)
                )
                animation.duration = 250
                animation.interpolator = AccelerateDecelerateInterpolator()
                animation.start()
            }
        } else {
            bar?.alpha = 1f
            text?.alpha = 1f
        }
    }


    // --
    // Loading task
    // --

    private class LoadingTask(private val delay: Long = 0, private val loader: () -> Unit) {

        fun start(completion: () -> Unit) {
            GlobalScope.launch(Dispatchers.Default) {
                if (delay > 0) {
                    delay(delay)
                }
                loader()
                GlobalScope.launch(Dispatchers.Main) {
                    completion()
                }
            }
        }

    }

}

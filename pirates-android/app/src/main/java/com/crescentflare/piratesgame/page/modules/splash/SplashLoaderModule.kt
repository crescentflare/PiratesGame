package com.crescentflare.piratesgame.page.modules.splash

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.crescentflare.piratesgame.components.compoundviews.SplashLoadingBar
import com.crescentflare.piratesgame.components.styling.AppFonts
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.infrastructure.events.AppEventObserver
import com.crescentflare.piratesgame.page.storage.Page
import com.crescentflare.piratesgame.page.utility.ControllerModule
import com.crescentflare.viewletcreator.binder.ViewletMapBinder
import kotlinx.coroutines.*
import java.lang.ref.WeakReference

/**
 * Shared module: catches alert events to show popup dialogs
 */
class SplashLoaderModule: ControllerModule {

    // ---
    // Members
    // ---

    override val eventType = "splashLoader"
    private var context: WeakReference<Context>? = null
    private var binder: ViewletMapBinder? = null
    private var loadingTasks = mutableListOf<LoadingTask>()
    private var busy = false
    private var preparing = false


    // ---
    // Initialization
    // ---

    override fun onCreate(context: Context) {
        // Keep weak reference to context
        this.context = WeakReference(context)

        // Add loading tasks
        loadingTasks.add(LoadingTask {
            AppFonts.loadAll()
        })
    }


    // ---
    // Page updates
    // ---

    override fun onPageUpdated(page: Page, binder: ViewletMapBinder) {
        this.binder = binder
        if (busy) {
            showLoading(false)
        }
    }


    // ---
    // Event handling
    // ---

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


    // ---
    // Loading
    // ---

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
                    eventObserver?.observedEvent(AppEvent("alert://simple?title=Loading+complete&text=TODO:+implement+next+screen"), null)
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
        val bar = binder?.findByReference("loadingBar")
        val text = binder?.findByReference("loadingText")
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


    // ---
    // Loading task
    // ---

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

package com.crescentflare.piratesgame

import android.app.Application
import com.crescentflare.dynamicappconfig.manager.AppConfigStorage
import com.crescentflare.jsoninflator.utility.InflatorResourceColorLookup
import com.crescentflare.jsoninflator.utility.InflatorResourceDimensionLookup
import com.crescentflare.piratesgame.components.basicviews.ButtonView
import com.crescentflare.piratesgame.components.basicviews.GradientView
import com.crescentflare.piratesgame.components.complexviews.PublisherLogo
import com.crescentflare.piratesgame.components.compoundviews.SplashAnimation
import com.crescentflare.piratesgame.components.compoundviews.SplashLoadingBar
import com.crescentflare.piratesgame.components.containers.FrameContainerView
import com.crescentflare.piratesgame.components.containers.LinearContainerView
import com.crescentflare.piratesgame.components.containers.ScrollContainerView
import com.crescentflare.piratesgame.components.game.LevelView
import com.crescentflare.piratesgame.components.navigationbars.SolidNavigationBar
import com.crescentflare.piratesgame.components.navigationbars.TransparentNavigationBar
import com.crescentflare.piratesgame.components.simpleviewlets.ImageViewlet
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.piratesgame.components.simpleviewlets.SpacerViewlet
import com.crescentflare.piratesgame.components.simpleviewlets.SpinnerViewlet
import com.crescentflare.piratesgame.components.simpleviewlets.TextViewlet
import com.crescentflare.piratesgame.components.styling.AppFonts
import com.crescentflare.piratesgame.infrastructure.appconfig.CustomAppConfigManager
import com.crescentflare.piratesgame.infrastructure.inflator.Inflators


/**
 * The main application singleton
 */
class BaseApplication : Application(), AppConfigStorage.ChangedConfigListener {

    // --
    // Initialization
    // --

    override fun onCreate() {
        // Enable app config utility for non-release builds
        super.onCreate()
        if (!BuildConfig.BUILD_TYPE.equals("release")) {
            AppConfigStorage.instance.init(this, CustomAppConfigManager.instance)
            AppConfigStorage.instance.addChangedConfigListener(this)
            onChangedConfig()
        }

        // Configure framework
        AppFonts.setContext(this)
        registerViewlets()
    }


    // --
    // App config integration
    // --

    override fun onChangedConfig() {
        // No implementation needed (for now)
    }


    // --
    // Viewlet registration
    // --

    private fun registerViewlets() {
        // Enable platform specific attributes
        Inflators.viewlet.setMergeSubAttributes(listOf("android"))
        Inflators.viewlet.setExcludeAttributes(listOf("ios"))

        // Lookups
        Inflators.viewlet.setColorLookup(InflatorResourceColorLookup(this))
        Inflators.viewlet.setDimensionLookup(InflatorResourceDimensionLookup(this))

        // Basic views
        Inflators.viewlet.register("button", ButtonView.viewlet)
        Inflators.viewlet.registerAttributeSet("button", "default", ButtonView.defaultStyle())
        Inflators.viewlet.register("gradient", GradientView.viewlet)

        // Compound views
        Inflators.viewlet.register("splashAnimation", SplashAnimation.viewlet)
        Inflators.viewlet.register("splashLoadingBar", SplashLoadingBar.viewlet)

        // Complex views
        Inflators.viewlet.register("publisherLogo", PublisherLogo.viewlet)

        // Containers
        Inflators.viewlet.register("frameContainer", FrameContainerView.viewlet)
        Inflators.viewlet.register( "linearContainer", LinearContainerView.viewlet)
        Inflators.viewlet.register( "scrollContainer", ScrollContainerView.viewlet)

        // Game
        Inflators.viewlet.register("level", LevelView.viewlet)

        // Navigation bars
        Inflators.viewlet.register("transparentNavigationBar", TransparentNavigationBar.viewlet)
        Inflators.viewlet.register("solidNavigationBar", SolidNavigationBar.viewlet)

        // Simple viewlets
        Inflators.viewlet.register("image", ImageViewlet.viewlet)
        Inflators.viewlet.register("spacer", SpacerViewlet.viewlet)
        Inflators.viewlet.register("spinner", SpinnerViewlet.viewlet)
        Inflators.viewlet.register("text", TextViewlet.viewlet)
        Inflators.viewlet.register("view", ViewletUtil.basicViewViewlet)
    }

}

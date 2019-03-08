package com.crescentflare.piratesgame

import android.app.Application
import com.crescentflare.dynamicappconfig.manager.AppConfigStorage
import com.crescentflare.piratesgame.components.basicviews.GradientView
import com.crescentflare.piratesgame.components.complexviews.PublisherLogo
import com.crescentflare.piratesgame.components.compoundviews.SplashAnimation
import com.crescentflare.piratesgame.components.compoundviews.SplashLoadingBar
import com.crescentflare.piratesgame.components.containers.FrameContainerView
import com.crescentflare.piratesgame.components.containers.LinearContainerView
import com.crescentflare.piratesgame.components.containers.ScrollContainerView
import com.crescentflare.piratesgame.components.navigationbars.TransparentNavigationBar
import com.crescentflare.piratesgame.components.simpleviewlets.ImageViewlet
import com.crescentflare.viewletcreator.ViewletCreator
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.piratesgame.components.simpleviewlets.SpacerViewlet
import com.crescentflare.piratesgame.components.simpleviewlets.SpinnerViewlet
import com.crescentflare.piratesgame.components.simpleviewlets.TextViewlet
import com.crescentflare.piratesgame.components.styling.AppFonts
import com.crescentflare.piratesgame.infrastructure.appconfig.CustomAppConfigManager
import com.crescentflare.viewletcreator.utility.ViewletResourceDimensionLookup
import com.crescentflare.viewletcreator.utility.ViewletMapUtil
import com.crescentflare.viewletcreator.utility.ViewletResourceColorLookup


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
        // Lookups
        ViewletMapUtil.setColorLookup(ViewletResourceColorLookup(this))
        ViewletMapUtil.setDimensionLookup(ViewletResourceDimensionLookup(this))

        // Basic views
        ViewletCreator.registerViewlet("gradient", GradientView.viewlet)

        // Compound views
        ViewletCreator.registerViewlet("splashAnimation", SplashAnimation.viewlet)
        ViewletCreator.registerViewlet("splashLoadingBar", SplashLoadingBar.viewlet)

        // Complex views
        ViewletCreator.registerViewlet("publisherLogo", PublisherLogo.viewlet)

        // Containers
        ViewletCreator.registerViewlet("frameContainer", FrameContainerView.viewlet)
        ViewletCreator.registerViewlet( "linearContainer", LinearContainerView.viewlet)
        ViewletCreator.registerViewlet( "scrollContainer", ScrollContainerView.viewlet)

        // Navigation bars
        ViewletCreator.registerViewlet("transparentNavigationBar", TransparentNavigationBar.viewlet)

        // Simple viewlets
        ViewletCreator.registerViewlet("image", ImageViewlet.viewlet)
        ViewletCreator.registerViewlet("spacer", SpacerViewlet.viewlet)
        ViewletCreator.registerViewlet("spinner", SpinnerViewlet.viewlet)
        ViewletCreator.registerViewlet("text", TextViewlet.viewlet)
        ViewletCreator.registerViewlet("view", ViewletUtil.basicViewViewlet)
    }

}

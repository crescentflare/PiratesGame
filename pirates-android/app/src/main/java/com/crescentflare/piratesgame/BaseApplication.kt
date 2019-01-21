package com.crescentflare.piratesgame

import android.app.Application
import com.crescentflare.piratesgame.components.basicviews.GradientView
import com.crescentflare.piratesgame.components.complexviews.PublisherLogo
import com.crescentflare.piratesgame.components.compoundviews.SplashAnimation
import com.crescentflare.piratesgame.components.containers.FrameContainerView
import com.crescentflare.piratesgame.components.containers.LinearContainerView
import com.crescentflare.piratesgame.components.simpleviewlets.ImageViewlet
import com.crescentflare.viewletcreator.ViewletCreator
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.piratesgame.components.simpleviewlets.SpacerViewlet
import com.crescentflare.piratesgame.components.simpleviewlets.SpinnerViewlet
import com.crescentflare.piratesgame.components.simpleviewlets.TextViewlet
import com.crescentflare.piratesgame.components.styling.AppFonts
import com.crescentflare.viewletcreator.utility.ViewletResourceDimensionLookup
import com.crescentflare.viewletcreator.utility.ViewletMapUtil
import com.crescentflare.viewletcreator.utility.ViewletResourceColorLookup


/**
 * The main application singleton
 */
class BaseApplication : Application() {

    // ---
    // Initialization
    // ---

    override fun onCreate() {
        super.onCreate()
        AppFonts.setContext(this)
        registerViewlets()
    }


    // ---
    // Viewlet registration
    // ---

    private fun registerViewlets() {
        // Lookups
        ViewletMapUtil.setColorLookup(ViewletResourceColorLookup(this))
        ViewletMapUtil.setDimensionLookup(ViewletResourceDimensionLookup(this))

        // Basic views
        ViewletCreator.registerViewlet("gradient", GradientView.viewlet)

        // Compound views
        ViewletCreator.registerViewlet("splashAnimation", SplashAnimation.viewlet)

        // Complex views
        ViewletCreator.registerViewlet("publisherLogo", PublisherLogo.viewlet)

        // Containers
        ViewletCreator.registerViewlet("frameContainer", FrameContainerView.viewlet)
        ViewletCreator.registerViewlet( "linearContainer", LinearContainerView.viewlet)

        // Simple viewlets
        ViewletCreator.registerViewlet("image", ImageViewlet.viewlet)
        ViewletCreator.registerViewlet("spacer", SpacerViewlet.viewlet)
        ViewletCreator.registerViewlet("spinner", SpinnerViewlet.viewlet)
        ViewletCreator.registerViewlet("text", TextViewlet.viewlet)
        ViewletCreator.registerViewlet("view", ViewletUtil.basicViewViewlet)
    }

}

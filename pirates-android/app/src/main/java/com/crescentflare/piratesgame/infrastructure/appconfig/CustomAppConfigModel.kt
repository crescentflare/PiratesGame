package com.crescentflare.piratesgame.infrastructure.appconfig

import com.crescentflare.dynamicappconfig.model.AppConfigBaseModel
import com.crescentflare.dynamicappconfig.model.AppConfigModelCategory
import com.crescentflare.dynamicappconfig.model.AppConfigModelGlobal
import com.crescentflare.dynamicappconfig.model.AppConfigModelSort

/**
 * App config: custom configuration model
 */
class CustomAppConfigModel : AppConfigBaseModel() {

    // --
    // Name
    // --

    private val name = "Production"


    // --
    // Development server settings
    // --

    @AppConfigModelGlobal
    @AppConfigModelSort(10)
    @AppConfigModelCategory("Development server")
    var devServerUrl = ""

    @AppConfigModelGlobal
    @AppConfigModelSort(20)
    @AppConfigModelCategory("Development server")
    var enablePageHotReload = false

    @AppConfigModelGlobal
    @AppConfigModelSort(30)
    @AppConfigModelCategory("Development server")
    var enableEventReceiver = false

}

//
//  CustomAppConfigModel.swift
//  AppConfigSwift Integration
//
//  App config: application configuration
//  A convenience model used by the build environment selector and has strict typing
//  Important when using model structure to define default values: always reflect a production situation
//

import UIKit
import DynamicAppConfig

class CustomAppConfigModel : AppConfigBaseModel {
    
    // --
    // MARK: Configuration fields
    // --
    
    var name: String = "Production"


    // --
    // MARK: Global fields
    // --
    
    var devServerUrl: String = ""
    var enablePageHotReload: Bool = false
    var enableEventReceiver: Bool = false


    // --
    // MARK: Field grouping and serialization
    // --
    
    override func map(mapper: AppConfigModelMapper) {
        // Map configuration fields
        mapper.map(key: "name", value: &name)
        
        // Map global fields
        mapper.map(key: "devServerUrl", value: &devServerUrl, category: "Development server", global: true)
        mapper.map(key: "enablePageHotReload", value: &enablePageHotReload, category: "Development server", global: true)
        mapper.map(key: "enableEventReceiver", value: &enableEventReceiver, category: "Development server", global: true)
    }
    
}

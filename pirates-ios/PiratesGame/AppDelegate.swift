//
//  AppDelegate.swift
//  Base application: catches global application events
//

import UIKit
import JsonInflator
import DynamicAppConfig

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    // --
    // MARK: Window member needed for the global application
    // --
    
    var window: UIWindow?


    // --
    // MARK: Lifecycle
    // --

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Enable app config utility for non-release builds
        #if RELEASE
        #else
            AppConfigStorage.shared.activate(manager: CustomAppConfigManager.sharedManager)
            AppConfigStorage.shared.addDataObserver(self, selector: #selector(updateConfigurationValues), name: AppConfigStorage.configurationChanged)
            updateConfigurationValues()
        #endif

        // Configure framework
        registerModules()
        registerViewlets()
        
        // Launch view controller
        window = CustomWindow(frame: UIScreen.main.bounds)
        window?.backgroundColor = UIColor.black
        window?.rootViewController = UINavigationController(rootViewController: SplashViewController())
        window?.makeKeyAndVisible()
        return true
    }

    func applicationWillResignActive(_ application: UIApplication) {
    }

    func applicationDidEnterBackground(_ application: UIApplication) {
    }

    func applicationWillEnterForeground(_ application: UIApplication) {
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
    }

    func applicationWillTerminate(_ application: UIApplication) {
    }
    

    // --
    // MARK: App config integration
    // --

    @objc func updateConfigurationValues() {
        // No implementation needed (for now)
    }


    // --
    // MARK: Inflatable registration
    // --
    
    func registerModules() {
        // Enable platform specific attributes
        Inflators.viewlet.setMergeSubAttributes(["ios"])
        Inflators.viewlet.setExcludeAttributes(["android"])
        
        // Modules
        Inflators.module.register(name: "alert", inflatable: AlertModule.inflatable())
        Inflators.module.register(name: "navigation", inflatable: NavigationModule.inflatable())
        Inflators.module.register(name: "splashLoader", inflatable: SplashLoaderModule.inflatable())
    }

    func registerViewlets() {
        // Enable platform specific attributes
        Inflators.viewlet.setMergeSubAttributes(["ios"])
        Inflators.viewlet.setExcludeAttributes(["android"])
        
        // Lookups
        Inflators.viewlet.colorLookup = AppColors.AppColorLookup()
        Inflators.viewlet.dimensionLookup = AppDimensions.AppDimensionLookup()
        
        // Basic views
        Inflators.viewlet.register(name: "button", inflatable: ButtonView.viewlet())
        Inflators.viewlet.registerAttributeSet(inflatableName: "button", setName: "default", setAttributes: ButtonView.defaultStyle)
        Inflators.viewlet.register(name: "gradient", inflatable: GradientView.viewlet())
        
        // Compound views
        Inflators.viewlet.register(name: "splashAnimation", inflatable: SplashAnimation.viewlet())
        Inflators.viewlet.register(name: "splashLoadingBar", inflatable: SplashLoadingBar.viewlet())

        // Complex views
        Inflators.viewlet.register(name: "publisherLogo", inflatable: PublisherLogo.viewlet())
        
        // Containers
        Inflators.viewlet.register(name: "frameContainer", inflatable: FrameContainerView.viewlet())
        Inflators.viewlet.register(name: "linearContainer", inflatable: LinearContainerView.viewlet())
        Inflators.viewlet.register(name: "navigationContainer", inflatable: NavigationContainerView.viewlet())
        Inflators.viewlet.register(name: "scrollContainer", inflatable: ScrollContainerView.viewlet())
        
        // Game views
        Inflators.viewlet.register(name: "level", inflatable: LevelView.viewlet())
        
        // Navigation bars
        Inflators.viewlet.register(name: "solidNavigationBar", inflatable: SolidNavigationBar.viewlet())
        Inflators.viewlet.register(name: "transparentNavigationBar", inflatable: TransparentNavigationBar.viewlet())

        // Simple viewlets
        Inflators.viewlet.register(name: "image", inflatable: ImageViewlet.viewlet())
        Inflators.viewlet.register(name: "spacer", inflatable: SpacerViewlet.viewlet())
        Inflators.viewlet.register(name: "spinner", inflatable: SpinnerViewlet.viewlet())
        Inflators.viewlet.register(name: "text", inflatable: TextViewlet.viewlet())
        Inflators.viewlet.register(name: "view", inflatable: ViewletUtil.basicViewViewlet())
    }

}

class CustomWindow: UIWindow {
    
    override func sendEvent(_ event: UIEvent) {
        super.sendEvent(event)
        if AppConfigStorage.shared.isActivated() && event.subtype == .motionShake {
            AppConfigManageViewController.launch()
        }
    }
    
}

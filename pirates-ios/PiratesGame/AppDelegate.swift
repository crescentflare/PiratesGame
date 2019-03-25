//
//  AppDelegate.swift
//  Base application: catches global application events
//

import UIKit
import ViewletCreator
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

        // Component registration
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
    // MARK: Viewlet registration
    // --

    func registerViewlets() {
        // Enable platform specific attributes
        ViewletCreator.setMergeSubAttributes(["ios"])
        ViewletCreator.setExcludeAttributes(["android"])
        
        // Lookups
        ViewletConvUtil.colorLookup = AppColors.AppColorLookup()
        ViewletConvUtil.dimensionLookup = AppDimensions.AppDimensionLookup()
        
        // Basic views
        ViewletCreator.register(name: "button", viewlet: ButtonView.viewlet())
        ViewletCreator.registerStyle(viewletName: "button", styleName: "default", styleAttributes: ButtonView.defaultStyle)
        ViewletCreator.register(name: "gradient", viewlet: GradientView.viewlet())
        
        // Compound views
        ViewletCreator.register(name: "splashAnimation", viewlet: SplashAnimation.viewlet())
        ViewletCreator.register(name: "splashLoadingBar", viewlet: SplashLoadingBar.viewlet())

        // Complex views
        ViewletCreator.register(name: "publisherLogo", viewlet: PublisherLogo.viewlet())
        
        // Containers
        ViewletCreator.register(name: "scrollContainer", viewlet: ScrollContainerView.viewlet())
        ViewletCreator.register(name: "frameContainer", viewlet: FrameContainerView.viewlet())
        ViewletCreator.register(name: "linearContainer", viewlet: LinearContainerView.viewlet())
        
        // Game views
        ViewletCreator.register(name: "level", viewlet: LevelView.viewlet())
        
        // Navigation bars
        ViewletCreator.register(name: "transparentNavigationBar", viewlet: TransparentNavigationBar.viewlet())
        ViewletCreator.register(name: "solidNavigationBar", viewlet: SolidNavigationBar.viewlet())

        // Simple viewlets
        ViewletCreator.register(name: "image", viewlet: ImageViewlet.viewlet())
        ViewletCreator.register(name: "spacer", viewlet: SpacerViewlet.viewlet())
        ViewletCreator.register(name: "spinner", viewlet: SpinnerViewlet.viewlet())
        ViewletCreator.register(name: "text", viewlet: TextViewlet.viewlet())
        ViewletCreator.register(name: "view", viewlet: ViewletUtil.basicViewViewlet())
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

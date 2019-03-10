//
//  SummaryViewController.swift
//  View controller: the main screen showing the summary of the player state
//

import UIKit
import ViewletCreator

class SummaryViewController: ComponentViewController, AppEventObserver, PageLoaderContinuousCompletion {

    // --
    // MARK: Outlets
    // --
    
    private let pageJson = "summary.json"
    private var pageLoader: PageLoader?
    private var hotReloadPageUrl = ""
    private let containerView = FrameContainerView()
    private var modules = [ControllerModule]()


    // --
    // MARK: Lifecycle
    // --
    
    override func viewDidLoad() {
        // Set navigation bar
        super.viewDidLoad()
        let navigationBar = SolidNavigationBar()
        navigationBar.isLightContent = true
        navigationBarView = navigationBar
        
        // Set container
        contentView = containerView
        containerView.eventObserver = self
        
        // Add module
        modules.append(AlertModule())
        modules.append(NavigationModule())
        for module in modules {
            module.didCreate(viewController: self)
        }
        
        // Start loading
        startContinuousPageLoad()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        let wasHotReloadPageUrl = hotReloadPageUrl
        if !CustomAppConfigManager.currentConfig().devServerUrl.isEmpty && CustomAppConfigManager.currentConfig().enablePageHotReload {
            hotReloadPageUrl = CustomAppConfigManager.currentConfig().devServerUrl
            if !hotReloadPageUrl.hasPrefix("http") {
                hotReloadPageUrl = "http://\(hotReloadPageUrl)"
            }
            hotReloadPageUrl = "\(hotReloadPageUrl)/pages/\(pageJson)"
        } else {
            hotReloadPageUrl = ""
        }
        if wasHotReloadPageUrl != hotReloadPageUrl || pageLoader == nil {
            pageLoader = PageLoader(location: !hotReloadPageUrl.isEmpty ? hotReloadPageUrl : pageJson.replacingOccurrences(of: ".json", with: ""))
            PageCache.shared.removeEntry(cacheKey: wasHotReloadPageUrl)
            PageCache.shared.removeEntry(cacheKey: pageJson)
        }
        EventReceiverTool.shared.addObserver(addObserver: self)
        startContinuousPageLoad()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        EventReceiverTool.shared.removeObserver(removeObserver: self)
        stopContinuousPageLoad()
    }


    // --
    // MARK: Interaction
    // --
    
    func observedEvent(_ event: AppEvent, sender: Any?) {
        for module in modules {
            if module.catchEvent(event, sender: sender) {
                break
            }
        }
    }


    // --
    // MARK: Page loader integration
    // --
    
    private func startContinuousPageLoad() {
        pageLoader?.startLoadingContinuously(completion: self)
    }
    
    private func stopContinuousPageLoad() {
        pageLoader?.stopLoadingContinuously()
    }
    
    func didUpdatePage(page: Page) {
        let binder = ViewletDictBinder()
        let inflateLayout: [String: Any] = [
            "viewlet": "frameContainer",
            "width": "stretchToParent",
            "height": "stretchToParent",
            "recycling": true,
            "items": [page.layout]
        ]
        ViewletUtil.assertInflateOn(view: containerView, attributes: inflateLayout, binder: binder)
        for module in modules {
            module.didUpdatePage(page: page, binder: binder)
        }
    }
    
}

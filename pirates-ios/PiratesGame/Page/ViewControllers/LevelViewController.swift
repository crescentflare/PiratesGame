//
//  LevelViewController.swift
//  View controller: a game level
//

import UIKit
import JsonInflator

class LevelViewController: NavigationViewController, AppEventObserver, PageLoaderContinuousCompletion {

    // --
    // MARK: Outlets
    // --
    
    private let pageJson = "level.json"
    private var pageLoader: PageLoader?
    private var hotReloadPageUrl = ""
    private var modules = [ControllerModule]()


    // --
    // MARK: Lifecycle
    // --
    
    override func viewDidLoad() {
        // Add modules
        super.viewDidLoad()
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
        let binder = InflatorDictBinder()
        inflateLayout(layout: page.layout, binder: binder)
        for module in modules {
            module.didUpdatePage(page: page, binder: binder)
        }
    }
    
}

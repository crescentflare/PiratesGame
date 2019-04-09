//
//  SummaryViewController.swift
//  View controller: the main screen showing the summary of the player state
//

import UIKit
import JsonInflator

class SummaryViewController: NavigationViewController, AppEventObserver, PageLoaderContinuousCompletion {

    // --
    // MARK: Outlets
    // --
    
    private let pageJson = "summary.json"
    private var pageLoader: PageLoader?
    private var hotReloadPageUrl = ""
    private var modules = [ControllerModule]()
    private var isResumed = false


    // --
    // MARK: Lifecycle
    // --
    
    override func viewDidLoad() {
        super.viewDidLoad()
        startContinuousPageLoad()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        // Update modules
        super.viewWillAppear(animated)
        isResumed = true
        for module in modules {
            module.didResume()
        }
        
        // Check for page and event updates
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
        // Update modules
        super.viewWillDisappear(animated)
        isResumed = false
        for module in modules {
            module.didPause()
        }
        
        // Stop page and event updates
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
        // Update modules
        updateModules(moduleItems: page.modules)
        
        // Update layout
        let binder = InflatorDictBinder()
        inflateLayout(layout: page.layout, binder: binder)
        for module in modules {
            module.didUpdatePage(page: page, binder: binder)
        }
    }
    
    
    // --
    // MARK: Helper
    // --
    
    private func updateModules(moduleItems: Any?) {
        // Check if modules are the same before and after the update, then they can be updated instead of re-created
        var canRecycle = false
        let items = Inflators.module.attributesForNestedInflatableList(moduleItems)
        if items.count == modules.count {
            canRecycle = true
            for i in items.indices {
                if !Inflators.module.canRecycle(object: modules[i], attributes: items[i]) {
                    canRecycle = false
                    break
                }
            }
        }
        
        // Update modules
        if canRecycle {
            var moduleIndex = 0
            for item in items {
                if moduleIndex < modules.count {
                    let module = modules[moduleIndex]
                    Inflators.module.inflate(onObject: module, attributes: item, parent: nil, binder: nil)
                    moduleIndex += 1
                }
            }
        } else {
            // First clear all modules
            modules.removeAll()
            
            // Add modules
            for item in items {
                if let result = Inflators.module.inflate(attributes: item, parent: nil, binder: nil) as? ControllerModule {
                    result.didCreate(viewController: self)
                    if isResumed {
                        result.didResume()
                    }
                    modules.append(result)
                }
            }
        }
    }
    
}

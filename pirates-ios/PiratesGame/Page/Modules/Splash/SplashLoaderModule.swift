//
//  SplashLoaderModule.swift
//  Shared module: catches alert events to show popup dialogs
//

import UIKit
import UniLayout
import JsonInflator

fileprivate class SplashLoaderModuleTask {
    
    private let delay: Double
    private let loader: () -> Void
    
    init(delay: Double = 0, loader: @escaping () -> Void) {
        self.delay = delay
        self.loader = loader
    }
    
    func start(completion: @escaping () -> Void) {
        DispatchQueue.global().asyncAfter(deadline: .now() + delay, execute: {
            self.loader()
            DispatchQueue.main.async {
                completion()
            }
        })
    }
    
}

class SplashLoaderModule: ControllerModule {
    
    // --
    // MARK: Members
    // --
    
    let eventType = "splashLoader"
    private weak var viewController: UIViewController?
    private var binder: InflatorDictBinder?
    private var loadingTasks = [SplashLoaderModuleTask]()
    private var busy = false
    private var preparing = false
    private var done = false

    
    // --
    // MARK: Inflator integration
    // --
    
    class func inflatable() -> JsonInflatable {
        return InflatorClass()
    }
    
    private class InflatorClass: JsonInflatable {
        
        func create() -> Any {
            return SplashLoaderModule()
        }
        
        func update(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any], parent: Any?, binder: InflatorBinder?) -> Bool {
            return object is SplashLoaderModule
        }
        
        func canRecycle(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any]) -> Bool {
            return object is SplashLoaderModule
        }
        
    }


    // --
    // MARK: Initialization
    // --
    
    init() {
        // No implementation
    }


    // --
    // MARK: Lifecycle
    // --

    func didCreate(viewController: UIViewController) {
        // Keep reference to the view controller
        self.viewController = viewController
        
        // Add loading tasks
        loadingTasks.append(SplashLoaderModuleTask(loader: {
            // Template for a loading task
        }))
    }
    
    func didPause() {
        // No implementation
    }
    
    func didResume() {
        // No implementation
    }

    
    // --
    // MARK: Page updates
    // --

    func didUpdatePage(page: Page, binder: InflatorDictBinder) {
        self.binder = binder
        if (busy) {
            showLoading(animated: false)
            if (done) {
                (binder.findByReference("loadingBar") as? SplashLoadingBar)?.progress = 1.0
            }
        }
    }


    // --
    // MARK: Event handling
    // --

    func catchEvent(_ event: AppEvent, sender: Any?) -> Bool {
        if event.rawType == eventType {
            if !preparing {
                preparing = true
                startLoading()
                return true
            }
        }
        return false
    }


    // --
    // MARK: Loading
    // --
    
    private func startLoading() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 1, execute: {
            self.busy = true
            self.showLoading()
            self.startLoadTask {
                var delay: Double = 0
                let bar = self.binder?.findByReference("loadingBar") as? SplashLoadingBar
                if bar?.autoAnimation == true {
                    delay = SplashLoadingBar.animationDuration
                }
                DispatchQueue.main.asyncAfter(deadline: .now() + delay, execute: {
                    let eventObserver = self.viewController as? AppEventObserver
                    eventObserver?.observedEvent(AppEvent(string: "navigate://summary?type=replace"), sender: nil)
                    self.done = true
                })
            }
        })
    }
    
    private func startLoadTask(index: Int = 0, completion: @escaping () -> Void) {
        if viewController == nil {
            return
        }
        if index >= loadingTasks.count {
            completion()
            return
        }
        loadingTasks[index].start {
            let bar = self.binder?.findByReference("loadingBar") as? SplashLoadingBar
            bar?.progress = Float(index + 1) / Float(self.loadingTasks.count)
            self.startLoadTask(index: index + 1, completion: completion)
        }
    }
    
    private func showLoading(animated: Bool = true) {
        let bar = binder?.findByReference("loadingBar") as? SplashLoadingBar
        let text = binder?.findByReference("loadingText") as? UniTextView
        bar?.visibility = .visible
        text?.visibility = .visible
        if animated {
            if let bar = bar, let text = text {
                bar.alpha = 0
                text.alpha = 0
                UIView.animate(withDuration: 0.25, delay: 0.0, options: [.curveEaseInOut], animations: {
                    bar.alpha = 1
                    text.alpha = 1
                })
            }
        } else {
            bar?.alpha = 1.0
            text?.alpha = 1.0
        }
    }

}

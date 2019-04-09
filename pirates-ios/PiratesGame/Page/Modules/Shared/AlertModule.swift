//
//  AlertModule.swift
//  Shared module: catches alert events to show popup dialogs
//

import UIKit
import JsonInflator

class AlertModule: ControllerModule {
    
    // --
    // MARK: Members
    // --
    
    let eventType = "alert"
    private weak var viewController: UIViewController?

    
    // --
    // MARK: Inflator integration
    // --
    
    class func inflatable() -> JsonInflatable {
        return InflatorClass()
    }
    
    private class InflatorClass: JsonInflatable {
        
        func create() -> Any {
            return AlertModule()
        }
        
        func update(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any], parent: Any?, binder: InflatorBinder?) -> Bool {
            return object is AlertModule
        }
        
        func canRecycle(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any]) -> Bool {
            return object is AlertModule
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
        self.viewController = viewController
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
        // No implementation
    }


    // --
    // MARK: Event handling
    // --

    func catchEvent(_ event: AppEvent, sender: Any?) -> Bool {
        if event.rawType == eventType {
            let alertController = UIAlertController(title: event.parameters["title"] ?? "Alert", message: event.parameters["text"] ?? "No text specified", preferredStyle: .alert)
            let okAction = UIAlertAction(title: "OK", style: .default) { (action: UIAlertAction) in
                // No implementation, just dismisses the alert
            }
            alertController.addAction(okAction)
            viewController?.present(alertController, animated: true, completion: nil)
            return true
        }
        return false
    }

}

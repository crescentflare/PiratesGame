//
//  AlertModule.swift
//  Shared module: catches alert events to show popup dialogs
//

import UIKit
import ViewletCreator

class AlertModule: ControllerModule {
    
    // --
    // MARK: Members
    // --
    
    let eventType = "alert"
    private var viewController: UIViewController?

    
    // --
    // MARK: Initialization
    // --
    
    init() {
        // No implementation
    }

    
    // --
    // MARK: Event handling
    // --
    
    func didCreate(viewController: UIViewController) {
        self.viewController = viewController
    }
    
    func didUpdatePage(page: Page, binder: ViewletDictBinder) {
        // No implementation
    }

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

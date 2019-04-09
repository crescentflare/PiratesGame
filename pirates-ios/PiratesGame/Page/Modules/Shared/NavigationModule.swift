//
//  NavigationModule.swift
//  Shared module: handles navigation between screens in the app
//

import UIKit
import JsonInflator

fileprivate enum NavigationType: String {
    
    case push = "push"
    case replace = "replace"
    case present = "present"

}

class NavigationModule: ControllerModule {
    
    // --
    // MARK: Members
    // --
    
    let eventType = "navigate"
    private weak var viewController: UIViewController?

    
    // --
    // MARK: Inflator integration
    // --
    
    class func inflatable() -> JsonInflatable {
        return InflatorClass()
    }
    
    private class InflatorClass: JsonInflatable {
        
        func create() -> Any {
            return NavigationModule()
        }
        
        func update(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any], parent: Any?, binder: InflatorBinder?) -> Bool {
            return object is NavigationModule
        }
        
        func canRecycle(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any]) -> Bool {
            return object is NavigationModule
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
            // Handle back navigation
            if event.fullPath == "back" {
                if let presentingViewController = viewController?.navigationController?.presentingViewController {
                    presentingViewController.dismiss(animated: true, completion: nil)
                } else {
                    viewController?.navigationController?.popViewController(animated: true)
                }
                return false
            }

            // Determine which view controller to navigate to
            var openViewController: UIViewController?
            switch event.fullPath {
            case "splash":
                openViewController = SplashViewController()
            case "summary":
                openViewController = SummaryViewController()
            case "level":
                openViewController = LevelViewController()
            default:
                break
            }
            
            // Handle navigation
            if let openViewController = openViewController {
                let navigationType = NavigationType(rawValue: event.parameters["type"] ?? "") ?? .push
                switch navigationType {
                case .push:
                    viewController?.navigationController?.pushViewController(openViewController, animated: true)
                case .replace:
                    viewController?.navigationController?.setViewControllers([openViewController], animated: true)
                case .present:
                    viewController?.present(UINavigationController(rootViewController: openViewController), animated: true, completion: nil)
                }
            }
        }
        return false
    }

}

//
//  NavigationModule.swift
//  Shared module: handles navigation between screens in the app
//

import UIKit
import ViewletCreator

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
    // MARK: Initialization
    // --
    
    init() {
        // No implementation
    }

    func didCreate(viewController: UIViewController) {
        self.viewController = viewController
    }
    

    // --
    // MARK: Page updates
    // --

    func didUpdatePage(page: Page, binder: ViewletDictBinder) {
        // No implementation
    }


    // --
    // MARK: Event handling
    // --

    func catchEvent(_ event: AppEvent, sender: Any?) -> Bool {
        if event.rawType == eventType {
            // Determine which view controller to navigate to
            var openViewController: UIViewController?
            switch event.fullPath {
            case "splash":
                openViewController = SplashViewController()
            case "summary":
                openViewController = SummaryViewController()
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
                    viewController?.present(openViewController, animated: true, completion: nil)
                }
            }
        }
        return false
    }

}

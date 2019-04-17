//
//  ComponentViewController.swift
//  View controller: the base class providing an easy way to set up navigation bar components
//

import UIKit
import UniLayout
import JsonInflator

class NavigationViewController: UIViewController {

    // --
    // MARK: Lifecycle
    // --
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return ((view as? NavigationContainerView)?.topBarView as? NavigationBarComponent)?.isLightContent ?? false ? .lightContent : .default
    }
    
    override func loadView() {
        super.loadView()
        let navigationContainer = NavigationContainerView()
        navigationContainer.viewController = self
        view = navigationContainer
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        updateNavigationBar()
    }
    

    // --
    // MARK: Inflation
    // --

    func inflateLayout(layout: [String: Any]?, binder: InflatorBinder) {
        // First inflate
        var inflateLayout = layout ?? [:]
        if Inflators.viewlet.findInflatableNameInAttributes(inflateLayout) != "navigationContainer" {
            inflateLayout = [
                "viewlet": "navigationContainer",
                "recycling": true,
                "content": layout ?? [:],
                "topBar": [
                    "viewlet": "simpleNavigationBar",
                    "width": "stretchToParent"
                ],
                "bottomBar": [
                    "viewlet": "bottomNavigationBar",
                    "width": "stretchToParent"
                ]
            ]
        }
        if inflateLayout["topBar"] == nil {
            inflateLayout["topBar"] = [
                "viewlet": "simpleNavigationBar",
                "width": "stretchToParent"
            ]
        }
        if inflateLayout["bottomBar"] == nil {
            inflateLayout["bottomBar"] = [
                "viewlet": "bottomNavigationBar",
                "width": "stretchToParent"
            ]
        }
        ViewletUtil.assertInflateOn(view: view, attributes: inflateLayout, binder: binder)
        (view as? NavigationContainerView)?.eventObserver = self as? AppEventObserver
        
        // Update for possible bar changes
        updateNavigationBar()
    }
    
    
    // --
    // MARK: Handle navigation bar
    // --
    
    private func updateNavigationBar() {
        let navigationBar = navigationController?.navigationBar
        navigationBar?.barStyle = preferredStatusBarStyle == .lightContent ? .black : .default
        navigationBar?.isTranslucent = true
        navigationBar?.tintColor = UIColor.clear
        navigationBar?.backgroundColor = UIColor.clear
        navigationController?.navigationBar.setBackgroundImage(UIImage(), for: UIBarMetrics.default)
        navigationController?.navigationBar.shadowImage = UIImage()
        navigationController?.navigationBar.isHidden = true
    }
    
}

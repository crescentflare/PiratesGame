//
//  ComponentViewController.swift
//  View controller: a base class for view controllers using components
//

import UIKit
import UniLayout
import ViewletCreator

class ComponentViewController: UIViewController {

    // --
    // MARK: Members
    // --
    
    var navigationBarView: (NavigationBarComponent & UIView)? {
        get {
            return (view as? ComponentViewControllerView)?.navigationBarView
        }
        set {
            (view as? ComponentViewControllerView)?.navigationBarView = newValue
            updateNavigationBar()
        }
    }

    var contentView: UIView? {
        get {
            return (view as? ComponentViewControllerView)?.contentView
        }
        set {
            (view as? ComponentViewControllerView)?.contentView = newValue
        }
    }


    // --
    // MARK: Lifecycle
    // --
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return (view as? ComponentViewControllerView)?.navigationBarView?.isLightContent ?? false ? .lightContent : .default
    }
    
    override func loadView() {
        super.loadView()
        view = ComponentViewControllerView(viewController: self)
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        updateNavigationBar()
    }
    

    // --
    // MARK: Helper
    // --
    
    private func updateNavigationBar() {
        let navigationBar = navigationController?.navigationBar
        navigationBar?.barStyle = preferredStatusBarStyle == .lightContent ? .black : .default
        navigationBar?.isTranslucent = true
        navigationBar?.tintColor = UIColor.clear
        navigationBar?.backgroundColor = UIColor.clear
        navigationController?.navigationBar.setBackgroundImage(UIImage(), for: UIBarMetrics.default)
        navigationController?.navigationBar.shadowImage = UIImage()
    }
    
}

fileprivate class ComponentViewControllerView: UIView {
    
    var navigationBarView: (NavigationBarComponent & UIView)? {
        didSet {
            subviews.forEach { $0.removeFromSuperview() }
            if let contentView = contentView {
                addSubview(contentView)
            }
            if let navigationBarView = navigationBarView {
                addSubview(navigationBarView)
            }
        }
    }
    
    var contentView: UIView? {
        didSet {
            subviews.forEach { $0.removeFromSuperview() }
            if let contentView = contentView {
                addSubview(contentView)
            }
            if let navigationBarView = navigationBarView {
                addSubview(navigationBarView)
            }
        }
    }
    
    var viewController: ComponentViewController?
    
    init(viewController: ComponentViewController) {
        super.init(frame: CGRect.zero)
        self.viewController = viewController
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    func topBarHeight() -> CGFloat {
        var height: CGFloat = 0
        height += min(UIApplication.shared.statusBarFrame.width, UIApplication.shared.statusBarFrame.height)
        if let navigationController = viewController?.navigationController {
            height = navigationController.navigationBar.frame.origin.y
            height += navigationController.navigationBar.frame.height
        }
        return height
    }

    override func layoutSubviews() {
        var reducedHeight: CGFloat = 0
        if let navigationBarView = navigationBarView {
            let barHeight = topBarHeight()
            navigationBarView.statusBarInset = min(UIApplication.shared.statusBarFrame.width, UIApplication.shared.statusBarFrame.height)
            UniLayout.setFrame(view: navigationBarView, frame: CGRect(x: 0, y: 0, width: bounds.width, height: barHeight))
            reducedHeight = navigationBarView.isTranslucent ? 0 : barHeight
        }
        if let contentView = contentView {
            UniLayout.setFrame(view: contentView, frame: CGRect(x: 0, y: reducedHeight, width: bounds.width, height: bounds.height - reducedHeight))
        }
    }
    
}

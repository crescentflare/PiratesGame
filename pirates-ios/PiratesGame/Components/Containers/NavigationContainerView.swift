//
//  NavigationContainerView.swift
//  Container view: contains the layout framework of a page, a content view and navigation bar components with optional translucency
//

import UIKit
import UniLayout
import JsonInflator

class NavigationContainerView: UIView, AppEventObserver {
    
    // --
    // MARK: Members
    // --

    weak var viewController: NavigationViewController?


    // --
    // MARK: Viewlet integration
    // --
    
    class func viewlet() -> JsonInflatable {
        return ViewletClass()
    }
    
    private class ViewletClass: JsonInflatable {
        
        func create() -> Any {
            return NavigationContainerView()
        }
        
        func update(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any], parent: Any?, binder: InflatorBinder?) -> Bool {
            if let navigationContainer = object as? NavigationContainerView {
                // Update content view
                let contentItem = Inflators.viewlet.attributesForNestedInflatable(attributes["content"])
                let recycling = convUtil.asBool(value: attributes["recycling"]) ?? false
                if recycling && Inflators.viewlet.canRecycle(object: navigationContainer.contentView, attributes: contentItem) {
                    if let item = contentItem {
                        if let view = navigationContainer.contentView {
                            Inflators.viewlet.inflate(onObject: view, attributes: item, binder: binder)
                            ViewletUtil.applyLayoutAttributes(convUtil: convUtil, view: view, attributes: item)
                            ViewletUtil.bindRef(view: view, attributes: item, binder: binder)
                        }
                    }
                } else {
                    // First empty content
                    navigationContainer.contentView = nil
                    
                    // Set content item
                    if let item = contentItem {
                        if let view = Inflators.viewlet.inflate(attributes: item, parent: navigationContainer, binder: binder) as? UIView {
                            navigationContainer.contentView = view
                            ViewletUtil.applyLayoutAttributes(convUtil: convUtil, view: view, attributes: item)
                            ViewletUtil.bindRef(view: view, attributes: item, binder: binder)
                        }
                    }
                }
                
                // Update top bar
                let topBarItem = Inflators.viewlet.attributesForNestedInflatable(attributes["topBar"])
                if recycling && Inflators.viewlet.canRecycle(object: navigationContainer.topBarView, attributes: contentItem) {
                    if let item = topBarItem {
                        if let view = navigationContainer.topBarView {
                            Inflators.viewlet.inflate(onObject: view, attributes: item, binder: binder)
                            ViewletUtil.applyLayoutAttributes(convUtil: convUtil, view: view, attributes: item)
                            ViewletUtil.bindRef(view: view, attributes: item, binder: binder)
                        }
                    }
                } else {
                    // First empty content
                    navigationContainer.topBarView = nil
                    
                    // Set content item
                    if let item = topBarItem {
                        if let view = Inflators.viewlet.inflate(attributes: item, parent: navigationContainer, binder: binder) as? UIView {
                            navigationContainer.topBarView = view
                            ViewletUtil.applyLayoutAttributes(convUtil: convUtil, view: view, attributes: item)
                            ViewletUtil.bindRef(view: view, attributes: item, binder: binder)
                        }
                    }
                }

                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(convUtil: convUtil, view: navigationContainer, attributes: attributes)
                
                // Forward event observer
                if let eventObserver = parent as? AppEventObserver {
                    navigationContainer.eventObserver = eventObserver
                }
                return true
            }
            return false
        }
        
        func canRecycle(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any]) -> Bool {
            return object is NavigationContainerView
        }
        
    }


    // --
    // MARK: Initialization
    // --

    override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    

    // --
    // MARK: Configurable values
    // --
    
    weak var eventObserver: AppEventObserver?

    var contentView: UIView? {
        didSet {
            oldValue?.removeFromSuperview()
            if let contentView = contentView {
                insertSubview(contentView, at: 0)
            }
        }
    }

    var topBarView: UIView? {
        didSet {
            oldValue?.removeFromSuperview()
            if let topBarView = topBarView {
                addSubview(topBarView)
            }
        }
    }
    

    // --
    // MARK: Interaction
    // --

    func observedEvent(_ event: AppEvent, sender: Any?) {
        eventObserver?.observedEvent(event, sender: sender)
    }

    
    // --
    // MARK: Custom layout
    // --

    override func layoutSubviews() {
        var reducedHeight: CGFloat = 0
        if let topBarView = topBarView {
            let barHeight = topBarHeight()
            (topBarView as? NavigationBarComponent)?.statusBarInset = min(UIApplication.shared.statusBarFrame.width, UIApplication.shared.statusBarFrame.height)
            UniLayout.setFrame(view: topBarView, frame: CGRect(x: 0, y: 0, width: bounds.width, height: barHeight))
            reducedHeight = (topBarView as? NavigationBarComponent)?.isTranslucent ?? false ? 0 : barHeight
        }
        if let contentView = contentView {
            UniLayout.setFrame(view: contentView, frame: CGRect(x: 0, y: reducedHeight, width: bounds.width, height: bounds.height - reducedHeight))
        }
    }


    // --
    // MARK: Helper
    // --

    func topBarHeight() -> CGFloat {
        var height: CGFloat = 0
        height += min(UIApplication.shared.statusBarFrame.width, UIApplication.shared.statusBarFrame.height)
        if let navigationController = viewController?.navigationController {
            height = navigationController.navigationBar.frame.origin.y
            height += navigationController.navigationBar.frame.height
        }
        return height
    }
    
}

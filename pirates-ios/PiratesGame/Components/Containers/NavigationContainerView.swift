//
//  NavigationContainerView.swift
//  Container view: contains the layout framework of a page, a content view and navigation bar components with optional translucency
//

import UIKit
import UniLayout
import JsonInflator

enum NavigationContainerViewScrollPaddingType: String {
    
    case none = "none"
    case topAndBottom = "topAndBottom"
    case statusAndBottom = "statusAndBottom"
    
}

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
                if recycling && Inflators.viewlet.canRecycle(object: navigationContainer.topBarView, attributes: topBarItem) {
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
                    
                    // Set bar item
                    if let item = topBarItem {
                        if let view = Inflators.viewlet.inflate(attributes: item, parent: navigationContainer, binder: binder) as? UIView {
                            navigationContainer.topBarView = view
                            ViewletUtil.applyLayoutAttributes(convUtil: convUtil, view: view, attributes: item)
                            ViewletUtil.bindRef(view: view, attributes: item, binder: binder)
                        }
                    }
                }
                
                // Update bottom bar
                let bottomBarItem = Inflators.viewlet.attributesForNestedInflatable(attributes["bottomBar"])
                if recycling && Inflators.viewlet.canRecycle(object: navigationContainer.bottomBarView, attributes: bottomBarItem) {
                    if let item = bottomBarItem {
                        if let view = navigationContainer.bottomBarView {
                            Inflators.viewlet.inflate(onObject: view, attributes: item, binder: binder)
                            ViewletUtil.applyLayoutAttributes(convUtil: convUtil, view: view, attributes: item)
                            ViewletUtil.bindRef(view: view, attributes: item, binder: binder)
                        }
                    }
                } else {
                    // First empty content
                    navigationContainer.bottomBarView = nil
                    
                    // Set bar item
                    if let item = bottomBarItem {
                        if let view = Inflators.viewlet.inflate(attributes: item, parent: navigationContainer, binder: binder) as? UIView {
                            navigationContainer.bottomBarView = view
                            ViewletUtil.applyLayoutAttributes(convUtil: convUtil, view: view, attributes: item)
                            ViewletUtil.bindRef(view: view, attributes: item, binder: binder)
                        }
                    }
                }
                
                // Linked scroll container
                let linkedScrollContainer = (binder as? InflatorDictBinder)?.findByReference(convUtil.asString(value: attributes["linkedScrollContainer"]) ?? "")
                navigationContainer.linkedScrollContainer = linkedScrollContainer as? ScrollContainerView
                navigationContainer.automaticScrollPadding = NavigationContainerViewScrollPaddingType(rawValue: convUtil.asString(value: attributes["automaticScrollPadding"]) ?? "") ?? .none
                
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
            updateLinkedScrollPadding()
        }
    }
    
    var bottomBarView: UIView? {
        didSet {
            oldValue?.removeFromSuperview()
            if let bottomBarView = bottomBarView {
                addSubview(bottomBarView)
            }
            updateLinkedScrollPadding()
        }
    }
    
    weak var linkedScrollContainer: ScrollContainerView? {
        didSet {
            updateLinkedScrollPadding()
        }
    }
    
    var automaticScrollPadding: NavigationContainerViewScrollPaddingType = .none {
        didSet {
            updateLinkedScrollPadding()
        }
    }
    
    
    // --
    // MARK: Interaction
    // --
    
    func observedEvent(_ event: AppEvent, sender: Any?) {
        eventObserver?.observedEvent(event, sender: sender)
    }
    
    
    // --
    // MARK: Handle insets
    // --
    
    private func updateLinkedScrollPadding() {
        if let linkedScrollContainer = linkedScrollContainer {
            if automaticScrollPadding != .none && (topBarView as? NavigationBarComponent)?.isTranslucent ?? false {
                linkedScrollContainer.extraTopInset = automaticScrollPadding == .statusAndBottom ? statusBarHeight() : topBarHeight()
            } else {
                linkedScrollContainer.extraTopInset = 0
            }
            if automaticScrollPadding != .none && (bottomBarView as? NavigationBarComponent)?.isTranslucent ?? false {
                linkedScrollContainer.extraBottomInset = bottomBarHeight()
            } else {
                linkedScrollContainer.extraBottomInset = 0
            }
        }
    }
    
    
    // --
    // MARK: Custom layout
    // --
    
    override func layoutSubviews() {
        var topInset: CGFloat = 0
        var bottomInset: CGFloat = 0
        updateLinkedScrollPadding()
        if let topBarView = topBarView {
            let barHeight = topBarHeight()
            (topBarView as? NavigationBarComponent)?.statusBarInset = min(UIApplication.shared.statusBarFrame.width, UIApplication.shared.statusBarFrame.height)
            UniLayout.setFrame(view: topBarView, frame: CGRect(x: 0, y: 0, width: bounds.width, height: barHeight))
            topInset = (topBarView as? NavigationBarComponent)?.isTranslucent ?? false ? 0 : barHeight
        }
        if let bottomBarView = bottomBarView {
            let barHeight = bottomBarHeight()
            UniLayout.setFrame(view: bottomBarView, frame: CGRect(x: 0, y: bounds.height - barHeight, width: bounds.width, height: barHeight))
            bottomInset = (bottomBarView as? NavigationBarComponent)?.isTranslucent ?? false ? 0 : barHeight
        }
        if let contentView = contentView {
            UniLayout.setFrame(view: contentView, frame: CGRect(x: 0, y: topInset, width: bounds.width, height: bounds.height - topInset - bottomInset))
        }
    }
    
    
    // --
    // MARK: Helper
    // --
    
    func statusBarHeight() -> CGFloat {
        return min(UIApplication.shared.statusBarFrame.width, UIApplication.shared.statusBarFrame.height)
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
    
    func bottomBarHeight() -> CGFloat {
        if #available(iOS 11.0, *) {
            return safeAreaInsets.bottom
        }
        return 0
    }
    
}

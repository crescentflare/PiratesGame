//
//  ScrollContainerView.swift
//  Container view: a scroll view specifically made for the layout containers (like LinearContainerView)
//

import UIKit
import UniLayout
import JsonInflator

class ScrollContainerView: UniVerticalScrollContainer, AppEventObserver {
    
    // --
    // MARK: Members
    // --
    
    internal var refreshControlView: UIRefreshControl?
    
    
    // --
    // MARK: Viewlet integration
    // --
    
    class func viewlet() -> JsonInflatable {
        return ViewletClass()
    }
    
    private class ViewletClass: JsonInflatable {
        
        func create() -> Any {
            return ScrollContainerView()
        }
        
        func update(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any], parent: Any?, binder: InflatorBinder?) -> Bool {
            if let scrollContainer = object as? ScrollContainerView {
                // Set fill content mode
                scrollContainer.fillContent = convUtil.asBool(value: attributes["fillContent"]) ?? false
                
                // Update content view
                let checkItem = Inflators.viewlet.attributesForNestedInflatable(attributes["item"])
                let recycling = convUtil.asBool(value: attributes["recycling"]) ?? false
                if recycling && Inflators.viewlet.canRecycle(object: scrollContainer.contentView, attributes: checkItem) {
                    if let item = checkItem {
                        if let view = scrollContainer.contentView {
                            Inflators.viewlet.inflate(onObject: view, attributes: item)
                            ViewletUtil.applyLayoutAttributes(convUtil: convUtil, view: view, attributes: item)
                            ViewletUtil.bindRef(view: view, attributes: item, binder: binder)
                        }
                    }
                } else {
                    // First empty content
                    scrollContainer.contentView = nil
                    
                    // Set content item
                    if let item = checkItem {
                        if let view = Inflators.viewlet.inflate(attributes: item, parent: scrollContainer, binder: binder) as? UIView {
                            scrollContainer.contentView = view
                            ViewletUtil.applyLayoutAttributes(convUtil: convUtil, view: view, attributes: item)
                            ViewletUtil.bindRef(view: view, attributes: item, binder: binder)
                        }
                    }
                }
                
                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(convUtil: convUtil, view: scrollContainer, attributes: attributes)
                
                // Event handling
                scrollContainer.pullToRefreshEvent = AppEvent(value: attributes["pullToRefreshEvent"])

                // Forward event observer
                if let eventObserver = parent as? AppEventObserver {
                    scrollContainer.eventObserver = eventObserver
                }
                return true
            }
            return false
        }
        
        func canRecycle(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any]) -> Bool {
            return object is ScrollContainerView
        }
        
    }
    
    
    // --
    // MARK: Initialization
    // --
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setup()
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        setup()
    }
    
    private func setup() {
        if #available(iOS 11.0, *) {
            contentInsetAdjustmentBehavior = .never
        }
    }
    
    
    // --
    // MARK: Configurable values
    // --

    weak var eventObserver: AppEventObserver?

    var pullToRefreshEvent: AppEvent? {
        didSet {
            if pullToRefreshEvent != nil && refreshControlView == nil {
                refreshControlView = UIRefreshControl()
                refreshControlView?.tintColor = AppColors.primary
                refreshControlView?.addTarget(self, action: #selector(pulledToRefresh(_:)), for: .valueChanged)
                backgroundView = refreshControlView
                alwaysBounceVertical = true
            } else if pullToRefreshEvent == nil && refreshControlView != nil {
                backgroundView = nil
                refreshControlView = nil
                alwaysBounceVertical = false
            }
        }
    }
    
    func stopRefreshing() {
        refreshControlView?.endRefreshing()
    }
    
    
    // --
    // MARK: Interaction
    // --
    
    func observedEvent(_ event: AppEvent, sender: Any?) {
        eventObserver?.observedEvent(event, sender: sender)
    }

    @objc func pulledToRefresh(_ refreshControl: UIRefreshControl) {
        if let event = pullToRefreshEvent {
            eventObserver?.observedEvent(event, sender: self)
        }
    }

}

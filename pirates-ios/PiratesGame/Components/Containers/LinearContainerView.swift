//
//  LinearContainerView.swift
//  Container view: basic layout container for horizontally or vertically aligned views
//

import UIKit
import UniLayout
import JsonInflator

class LinearContainerView: UniLinearContainer, AppEventObserver, /*PageViewComponentInfo,*/ UniTapDelegate {
    
    // --
    // MARK: Viewlet integration
    // --
    
    class func viewlet() -> JsonInflatable {
        return ViewletClass()
    }
    
    private class ViewletClass: JsonInflatable {
        
        func create() -> Any {
            return LinearContainerView()
        }
        
        func update(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any], parent: Any?, binder: InflatorBinder?) -> Bool {
            if let linearContainer = object as? LinearContainerView {
                // Set container name
                linearContainer.accessibilityLabel = convUtil.asString(value: attributes["containerName"])

                // Set orientation
                if let orientation = UniLinearContainerOrientation(rawValue: convUtil.asString(value: attributes["orientation"]) ?? "") {
                    linearContainer.orientation = orientation
                } else {
                    linearContainer.orientation = .vertical
                }
                
                // Create or update subviews
                ViewletUtil.createSubviews(convUtil: convUtil, container: linearContainer, parent: linearContainer, attributes: attributes, subviewItems: attributes["items"], binder: binder)
                
                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(convUtil: convUtil, view: linearContainer, attributes: attributes)
                
                // Event handling
                linearContainer.tapEvent = AppEvent(value: attributes["tapEvent"])

                // Forward event observer
                if let eventObserver = parent as? AppEventObserver {
                    linearContainer.eventObserver = eventObserver
                }
                return true
            }
            return false
        }
        
        func canRecycle(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any]) -> Bool {
            return object is LinearContainerView
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
    }
    
    
    // --
    // MARK: Configurable values
    // --

    weak var eventObserver: AppEventObserver?

    var tapEvent: AppEvent? {
        didSet {
            tapDelegate = tapEvent != nil ? self : nil
        }
    }

    
    // --
    // MARK: Interaction
    // --
    
    var senderLabel: String? {
        get {
            return accessibilityLabel
        }
    }
    
    func containerTapped(_ sender: UIView) {
        if let tapEvent = tapEvent {
            observedEvent(tapEvent, sender: self)
        }
    }
    
    func observedEvent(_ event: AppEvent, sender: Any?) {
        eventObserver?.observedEvent(event, sender: sender)
    }

}

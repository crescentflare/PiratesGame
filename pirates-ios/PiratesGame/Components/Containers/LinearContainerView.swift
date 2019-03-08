//
//  LinearContainerView.swift
//  Container view: basic layout container for horizontally or vertically aligned views
//

import UIKit
import UniLayout
import ViewletCreator

class LinearContainerView: UniLinearContainer, AppEventObserver, /*PageViewComponentInfo,*/ UniTapDelegate {
    
    // --
    // MARK: Viewlet integration
    // --
    
    class func viewlet() -> Viewlet {
        return ViewletClass()
    }
    
    private class ViewletClass: Viewlet {
        
        func create() -> UIView {
            return LinearContainerView()
        }
        
        func update(view: UIView, attributes: [String : Any], parent: UIView?, binder: ViewletBinder?) -> Bool {
            if let linearContainer = view as? LinearContainerView {
                // Set container name
                linearContainer.accessibilityLabel = ViewletConvUtil.asString(value: attributes["containerName"])

                // Set orientation
                if let orientation = UniLinearContainerOrientation(rawValue: ViewletConvUtil.asString(value: attributes["orientation"]) ?? "") {
                    linearContainer.orientation = orientation
                } else {
                    linearContainer.orientation = .vertical
                }
                
                // Create or update subviews
                ViewletUtil.createSubviews(container: linearContainer, parent: linearContainer, attributes: attributes, subviewItems: attributes["items"], binder: binder)
                
                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(view: view, attributes: attributes)
                
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
        
        func canRecycle(view: UIView, attributes: [String : Any]) -> Bool {
            return view is LinearContainerView
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

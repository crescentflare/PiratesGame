//
//  FrameContainerView.swift
//  Container view: basic layout container for overlapping views
//

import UIKit
import UniLayout
import ViewletCreator

class FrameContainerView: UniFrameContainer, AppEventObserver, AppEventLabeledSender, UniTapDelegate {
    
    // --
    // MARK: Viewlet integration
    // --
    
    class func viewlet() -> Viewlet {
        return ViewletClass()
    }
    
    private class ViewletClass: Viewlet {
        
        func create() -> UIView {
            return FrameContainerView()
        }
        
        func update(view: UIView, attributes: [String : Any], parent: UIView?, binder: ViewletBinder?) -> Bool {
            if let frameContainer = view as? FrameContainerView {
                // Set container name
                frameContainer.accessibilityLabel = ViewletConvUtil.asString(value: attributes["containerName"])
                
                // Create or update subviews
                ViewletUtil.createSubviews(container: frameContainer, parent: frameContainer, attributes: attributes, subviewItems: attributes["items"], binder: binder)
                
                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(view: view, attributes: attributes)
                
                // Event handling
                frameContainer.tapEvent = AppEvent(value: attributes["tapEvent"])
                
                // Forward event observer
                if let eventObserver = parent as? AppEventObserver {
                    frameContainer.eventObserver = eventObserver
                }
                return true
            }
            return false
        }
        
        func canRecycle(view: UIView, attributes: [String : Any]) -> Bool {
            return view is FrameContainerView
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

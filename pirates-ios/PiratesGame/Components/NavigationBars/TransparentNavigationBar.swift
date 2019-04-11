//
//  TransparentNavigationBar.swift
//  Navigation bar: an invisible bar, used if no navigation bar is specified
//

import UIKit
import UniLayout
import JsonInflator

class TransparentNavigationBar: UniView, NavigationBarComponent {
    
    // --
    // MARK: Members
    // --
    
    var isTranslucent: Bool {
        get {
            return true
        }
    }
    
    
    // --
    // MARK: Viewlet integration
    // --
    
    class func viewlet() -> JsonInflatable {
        return ViewletClass()
    }
    
    private class ViewletClass: JsonInflatable {
        
        func create() -> Any {
            return TransparentNavigationBar()
        }
        
        func update(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any], parent: Any?, binder: InflatorBinder?) -> Bool {
            if let navigationBar = object as? TransparentNavigationBar {
                // Bar properties
                navigationBar.isLightContent = convUtil.asBool(value: attributes["lightContent"]) ?? false
                
                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(convUtil: convUtil, view: navigationBar, attributes: attributes)
                return true
            }
            return false
        }
        
        func canRecycle(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any]) -> Bool {
            return object is TransparentNavigationBar
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
    
    fileprivate func setup() {
    }
    
    
    // --
    // MARK: Configurable values
    // --
    
    var isLightContent: Bool = false
    
    var statusBarInset: CGFloat = 0
    
}

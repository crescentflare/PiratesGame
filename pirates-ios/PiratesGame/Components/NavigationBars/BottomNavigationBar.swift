//
//  BottomNavigationBar.swift
//  Navigation bar: a bar used at the bottom under the iPhone X safe area
//

import UIKit
import UniLayout
import JsonInflator

class BottomNavigationBar: UIView, NavigationBarComponent {

    // --
    // MARK: Members
    // --
    
    var isTranslucent: Bool {
        get {
            return backgroundColor?.cgColor.alpha ?? 0 < 1.0
        }
    }
    
    var isLightContent: Bool {
        get {
            return ViewletUtil.colorIntensity(color: backgroundColor) < 0.25
        }
    }
    
    var statusBarInset: CGFloat = 0

    
    // --
    // MARK: Viewlet integration
    // --
    
    class func viewlet() -> JsonInflatable {
        return ViewletClass()
    }
    
    private class ViewletClass: JsonInflatable {
        
        func create() -> Any {
            return BottomNavigationBar()
        }
        
        func update(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any], parent: Any?, binder: InflatorBinder?) -> Bool {
            if let navigationBar = object as? BottomNavigationBar {
                ViewletUtil.applyGenericViewAttributes(convUtil: convUtil, view: navigationBar, attributes: attributes)
                return true
            }
            return false
        }
        
        func canRecycle(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any]) -> Bool {
            return object is BottomNavigationBar
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
        // No implementation
    }
    
}

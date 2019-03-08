//
//  TransparentNavigationBar.swift
//  Navigation bar: an invisible bar, used by default
//

import UIKit
import UniLayout
import ViewletCreator

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
    
    class func viewlet() -> Viewlet {
        return ViewletClass()
    }
    
    private class ViewletClass: Viewlet {
        
        func create() -> UIView {
            return TransparentNavigationBar()
        }
        
        func update(view: UIView, attributes: [String : Any], parent: UIView?, binder: ViewletBinder?) -> Bool {
            if let navigationBar = view as? TransparentNavigationBar {
                // Bar properties
                navigationBar.isLightContent = ViewletConvUtil.asBool(value: attributes["lightContent"]) ?? false

                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(view: view, attributes: attributes)
                return true
            }
            return false
        }
        
        func canRecycle(view: UIView, attributes: [String : Any]) -> Bool {
            return view is TransparentNavigationBar
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

}

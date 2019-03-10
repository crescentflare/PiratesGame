//
//  SolidNavigationBar.swift
//  Navigation bar: a simple navigation bar, the default for apps
//

import UIKit
import UniLayout
import ViewletCreator

class SolidNavigationBar: UniView, NavigationBarComponent {

    // --
    // MARK: Members
    // --
    
    var isTranslucent: Bool {
        get {
            return false
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
            return SolidNavigationBar()
        }
        
        func update(view: UIView, attributes: [String : Any], parent: UIView?, binder: ViewletBinder?) -> Bool {
            if let navigationBar = view as? SolidNavigationBar {
                // Bar properties
                navigationBar.isLightContent = ViewletConvUtil.asBool(value: attributes["lightContent"]) ?? false

                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(view: view, attributes: attributes)
                return true
            }
            return false
        }
        
        func canRecycle(view: UIView, attributes: [String : Any]) -> Bool {
            return view is SolidNavigationBar
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

    var isLightContent: Bool = false {
        didSet {
            backgroundColor = isLightContent ? AppColors.primary : UIColor.white
        }
    }

}

//
//  SpinnerViewlet.swift
//  Basic view viewlet: a waiting spinner
//

import UIKit
import UniLayout
import ViewletCreator

class SpinnerViewlet {
    
    // --
    // MARK: Viewlet instance
    // --
    
    class func viewlet() -> Viewlet {
        return ViewletClass()
    }
    
    private class ViewletClass: Viewlet {
        
        func create() -> UIView {
            let spinner = UniSpinnerView()
            spinner.startAnimating()
            return spinner
        }
        
        func update(view: UIView, attributes: [String : Any], parent: UIView?, binder: ViewletBinder?) -> Bool {
            if let spinner = view as? UniSpinnerView {
                // Style
                if ViewletConvUtil.asString(value: attributes["style"]) == "inverted" {
                    spinner.activityIndicatorViewStyle = .white
                } else {
                    spinner.activityIndicatorViewStyle = .gray
                }
                
                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(view: view, attributes: attributes)
                return true
            }
            return false
        }
        
        func canRecycle(view: UIView, attributes: [String : Any]) -> Bool {
            return view is UniSpinnerView
        }
        
    }
    
}

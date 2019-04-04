//
//  SpinnerViewlet.swift
//  Basic view viewlet: a waiting spinner
//

import UIKit
import UniLayout
import JsonInflator

class SpinnerViewlet {
    
    // --
    // MARK: Viewlet instance
    // --
    
    class func viewlet() -> JsonInflatable {
        return ViewletClass()
    }
    
    private class ViewletClass: JsonInflatable {
        
        func create() -> Any {
            let spinner = UniSpinnerView()
            spinner.startAnimating()
            return spinner
        }
        
        func update(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any], parent: Any?, binder: InflatorBinder?) -> Bool {
            if let spinner = object as? UniSpinnerView {
                // Style
                if convUtil.asString(value: attributes["style"]) == "inverted" {
                    spinner.activityIndicatorViewStyle = .white
                } else {
                    spinner.activityIndicatorViewStyle = .gray
                }
                
                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(convUtil: convUtil, view: spinner, attributes: attributes)
                return true
            }
            return false
        }
        
        func canRecycle(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any]) -> Bool {
            return object is UniSpinnerView
        }
        
    }
    
}

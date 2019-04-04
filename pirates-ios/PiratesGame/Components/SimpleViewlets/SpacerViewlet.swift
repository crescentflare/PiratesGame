//
//  SpacerViewlet.swift
//  Basic view viewlet: a spacing element
//

import UIKit
import UniLayout
import JsonInflator

enum SpacerTakeSize: String {
    
    case none = ""
    case topBar = "topBar"
    case bottomSafeArea = "bottomSafeArea"
    
}

class SpacerViewlet: UniView {
    
    // --
    // MARK: Members
    // --
    
    private var curTakeWidth = SpacerTakeSize.none
    private var curTakeHeight = SpacerTakeSize.none
    
    
    // --
    // MARK: Viewlet integration
    // --
    
    class func viewlet() -> JsonInflatable {
        return ViewletClass()
    }
    
    private class ViewletClass: JsonInflatable {
        
        func create() -> Any {
            return SpacerViewlet()
        }
        
        func update(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any], parent: Any?, binder: InflatorBinder?) -> Bool {
            if let spacer = object as? SpacerViewlet {
                // Apply take width and height
                spacer.takeWidth = SpacerTakeSize(rawValue: convUtil.asString(value: attributes["takeWidth"]) ?? "") ?? .none
                spacer.takeHeight = SpacerTakeSize(rawValue: convUtil.asString(value: attributes["takeHeight"]) ?? "") ?? .none
                
                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(convUtil: convUtil, view: spacer, attributes: attributes)
                return true
            }
            return false
        }
        
        func canRecycle(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any]) -> Bool {
            return object is SpacerViewlet
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
    // MARK: Change values
    // --
    
    var takeWidth: SpacerTakeSize {
        set {
            curTakeWidth = newValue
            UniLayout.setNeedsLayout(view: self)
        }
        get { return curTakeWidth }
    }
    
    var takeHeight: SpacerTakeSize {
        set {
            curTakeHeight = newValue
            UniLayout.setNeedsLayout(view: self)
        }
        get { return curTakeHeight }
    }
    
    
    // --
    // MARK: Custom layout
    // --
    
    public static func topBarHeight(view: UIView) -> CGFloat {
        var responder: UIResponder? = view
        var height: CGFloat = 0
        height += min(UIApplication.shared.statusBarFrame.width, UIApplication.shared.statusBarFrame.height)
        for _ in 0..<32 { // Limited recursion
            if responder != nil {
                if let viewController = responder as? UIViewController {
                    height = viewController.navigationController?.navigationBar.frame.origin.y ?? 0
                    height += viewController.navigationController?.navigationBar.frame.height ?? 0
                    break
                }
                responder = responder?.next
            } else {
                break
            }
        }
        return height
    }
    
    public static func safeAreaBottomHeight() -> CGFloat {
        if #available(iOS 11.0, *) {
            return UIApplication.shared.delegate?.window??.safeAreaInsets.bottom ?? 0
        }
        return 0
    }
    
    open override func measuredSize(sizeSpec: CGSize, widthSpec: UniMeasureSpec, heightSpec: UniMeasureSpec) -> CGSize {
        // Basic size
        var result = CGSize(width: padding.left + padding.right, height: padding.top + padding.bottom)
        
        // Apply sizes taken from standard controls
        if takeHeight == .topBar {
            result.height += SpacerViewlet.topBarHeight(view: self)
        } else if takeHeight == .bottomSafeArea {
            result.height += SpacerViewlet.safeAreaBottomHeight()
        }
        
        // Apply limits and return result
        if widthSpec == .exactSize {
            result.width = sizeSpec.width
        } else if widthSpec == .limitSize {
            result.width = min(result.width, sizeSpec.width)
        }
        if heightSpec == .exactSize {
            result.height = sizeSpec.height
        } else if heightSpec == .limitSize {
            result.height = min(result.height, sizeSpec.height)
        }
        return result
    }
    
}

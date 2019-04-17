//
//  ViewletUtil.swift
//  Component utility: shared utilities for viewlet integration, also contains the viewlet for a basic UniView
//

import UIKit
import UniLayout
import JsonInflator

class ViewletUtil {
    
    // --
    // MARK: Basic view viewlet
    // --
    
    class func basicViewViewlet() -> JsonInflatable {
        return viewletClass()
    }
    
    private class viewletClass: JsonInflatable {
        
        func create() -> Any {
            return UniView()
        }
        
        func update(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any], parent: Any?, binder: InflatorBinder?) -> Bool {
            if let view = object as? UIView {
                ViewletUtil.applyGenericViewAttributes(convUtil: convUtil, view: view, attributes: attributes)
            }
            return true
        }
        
        func canRecycle(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any]) -> Bool {
            return object is UniView
        }
        
    }
    
    
    // --
    // MARK: Inflate with assertion
    // --

    class func assertInflateOn(view: UIView, attributes: [String: Any]?, parent: UIView? = nil, binder: InflatorBinder? = nil) {
        // Check if attributes are not nil
        assert(attributes != nil, "Attributes are null, load issue?")
        
        // Check viewlet name
        var viewletName: String?
        if let attributes = attributes {
            viewletName = Inflators.viewlet.findInflatableNameInAttributes(attributes)
            assert(viewletName != nil, "No viewlet found, JSON structure issue?")
        }
        
        // Check if the viewlet is registered
        if let attributes = attributes {
            let viewlet = Inflators.viewlet.findInflatableInAttributes(attributes)
            assert(viewlet != nil, "No viewlet implementation found, registration issue of \(String(describing: viewletName))?")
        }

        // Check result of inflate
        let result = Inflators.viewlet.inflate(onObject: view, attributes: attributes, parent: parent, binder: binder)
        assert(result == true, "Can't inflate viewlet, class doesn't match with \(String(describing: viewletName))?")
    }
    
    
    // --
    // MARK: Subview creation
    // --
    
    class func createSubviews(convUtil: InflatorConvUtil, container: UIView, parent: UIView?, attributes: [String: Any], subviewItems: Any?, binder: InflatorBinder?) {
        // Check if views are the same before and after the update, then they can be updated instead of re-created
        var canRecycle = false
        let recycling = convUtil.asBool(value: attributes["recycling"]) ?? false
        let items = Inflators.viewlet.attributesForNestedInflatableList(subviewItems)
        if recycling {
            if items.count == container.subviews.count {
                canRecycle = true
                for i in 0..<items.count {
                    if !Inflators.viewlet.canRecycle(object: container.subviews[i], attributes: items[i]) {
                        canRecycle = false
                        break
                    }
                }
            }
        }
        
        // Update subviews
        if canRecycle {
            var subviewIndex: Int = 0
            for item in items {
                if subviewIndex < container.subviews.count {
                    let view = container.subviews[subviewIndex]
                    Inflators.viewlet.inflate(onObject: view, attributes: item, parent: parent, binder: binder)
                    ViewletUtil.applyLayoutAttributes(convUtil: convUtil, view: view, attributes: item)
                    ViewletUtil.bindRef(view: view, attributes: item, binder: binder)
                    subviewIndex += 1
                }
            }
            container.layoutSubviews()
        } else {
            // First remove all subviews (and remember the scroll position if one of them is a scrollview)
            var scrollPosition: CGFloat = 0
            let views = container.subviews
            for view in views {
                if let scrollView = view as? UIScrollView {
                    scrollPosition = scrollView.contentOffset.y
                }
                view.removeFromSuperview()
            }
            
            // Add subviews
            var foundScrollView: UIScrollView?
            for item in items {
                if let view = Inflators.viewlet.inflate(attributes: item, parent: parent, binder: binder) as? UIView {
                    container.addSubview(view)
                    ViewletUtil.applyLayoutAttributes(convUtil: convUtil, view: view, attributes: item)
                    if scrollPosition != 0 && foundScrollView == nil {
                        if let scrollView = view as? UIScrollView {
                            foundScrollView = scrollView
                        }
                    }
                    ViewletUtil.bindRef(view: view, attributes: item, binder: binder)
                }
            }
            
            // Set back the scrollview position (if remembered)
            if let scrollView = foundScrollView {
                container.layoutIfNeeded()
                scrollView.contentOffset = CGPoint(x: CGFloat(0), y: max(0, min(scrollPosition, scrollView.contentSize.height - scrollView.bounds.height)))
            }
        }
    }
    
    
    // --
    // MARK: Easy reference binding
    // --
    
    class func bindRef(view: UIView?, attributes: [String: Any], binder: InflatorBinder?) {
        if let view = view, binder != nil {
            if let refId = attributes["refId"] as? String {
                binder?.onBind(refId: refId, object: view)
            }
        }
    }
    
    
    // --
    // MARK: Color calculation
    // --
    
    class func colorIntensity(color: UIColor?) -> Double {
        var red: CGFloat = 0
        var green: CGFloat = 0
        var blue: CGFloat = 0
        var alpha: CGFloat = 0
        if color?.getRed(&red, green: &green, blue: &blue, alpha: &alpha) ?? false {
            var colorComponents: [Double] = [Double(blue), Double(green), Double(red)]
            for i in colorComponents.indices {
                if colorComponents[i] <= 0.03928 {
                    colorComponents[i] /= 12.92
                }
                colorComponents[i] = pow((colorComponents[i] + 0.055) / 1.055, 2.4)
            }
            return 0.2126 * colorComponents[0] + 0.7152 * colorComponents[1] + 0.0722 * colorComponents[2]
        }
        return 0
    }
    
    
    // --
    // MARK: Waiting for view helpers
    // --
    
    class func waitViewLayout(view: UIView?, completion: @escaping () -> Void, timeout: @escaping () -> Void, maxIterations: Int = 8) {
        // If no iterations are left, time out
        if maxIterations == 0 {
            timeout()
            return
        }
        
        // Check view state, complete or try again
        if view?.bounds.width == 0 || view?.bounds.height == 0 {
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.001, execute: {
                ViewletUtil.waitViewLayout(view: view, completion: completion, timeout: timeout, maxIterations: maxIterations - 1)
            })
        } else {
            completion()
        }
    }

    
    // --
    // MARK: Shared generic view handling
    // --
    
    class func applyGenericViewAttributes(convUtil: InflatorConvUtil, view: UIView, attributes: [String: Any]) {
        // Standard view properties
        let visibility = convUtil.asString(value: attributes["visibility"]) ?? ""
        view.isHidden = visibility == "hidden" || visibility == "invisible"
        if let control = view as? UIControl {
            control.isEnabled = !(convUtil.asBool(value: attributes["disabled"]) ?? false)
        }
        // TODO: ignore background color for text entry
        view.backgroundColor = convUtil.asColor(value: attributes["backgroundColor"]) ?? UIColor.clear
        
        // Padding
        if var uniPaddedView = view as? UniLayoutPaddedView {
            let paddingArray = convUtil.asDimensionArray(value: attributes["padding"])
            var defaultPadding: [CGFloat] = [ 0, 0, 0, 0 ]
            if paddingArray.count == 4 {
                defaultPadding = paddingArray
            }
            uniPaddedView.padding = UIEdgeInsets(
                top: convUtil.asDimension(value: attributes["paddingTop"]) ?? defaultPadding[1],
                left: convUtil.asDimension(value: attributes["paddingLeft"]) ?? defaultPadding[0],
                bottom: convUtil.asDimension(value: attributes["paddingBottom"]) ?? defaultPadding[3],
                right: convUtil.asDimension(value: attributes["paddingRight"]) ?? defaultPadding[2])
        }
    }
    
    
    // --
    // MARK: Shared layout properties handling
    // --
    
    class func applyLayoutAttributes(convUtil: InflatorConvUtil, view: UIView?, attributes: [String: Any]) {
        if let layoutProperties = (view as? UniLayoutView)?.layoutProperties {
            // Margin
            let marginArray = convUtil.asDimensionArray(value: attributes["margin"])
            var defaultMargin: [CGFloat] = [ 0, 0, 0, 0 ]
            if marginArray.count == 4 {
                defaultMargin = marginArray
            }
            layoutProperties.margin = UIEdgeInsets(
                top: convUtil.asDimension(value: attributes["marginTop"]) ?? defaultMargin[1],
                left: convUtil.asDimension(value: attributes["marginLeft"]) ?? defaultMargin[0],
                bottom: convUtil.asDimension(value: attributes["marginBottom"]) ?? defaultMargin[3],
                right: convUtil.asDimension(value: attributes["marginRight"]) ?? defaultMargin[2])
            layoutProperties.spacingMargin = convUtil.asDimension(value: attributes["marginSpacing"]) ?? 0
            
            // Forced size or stretching
            let widthString = convUtil.asString(value: attributes["width"]) ?? ""
            let heightString = convUtil.asString(value: attributes["height"]) ?? ""
            if widthString == "stretchToParent" {
                layoutProperties.width = UniLayoutProperties.stretchToParent
            } else if widthString == "fitContent" {
                layoutProperties.width = UniLayoutProperties.fitContent
            } else {
                layoutProperties.width = convUtil.asDimension(value: attributes["width"]) ?? UniLayoutProperties.fitContent
            }
            if heightString == "stretchToParent" {
                layoutProperties.height = UniLayoutProperties.stretchToParent
            } else if heightString == "fitContent" {
                layoutProperties.height = UniLayoutProperties.fitContent
            } else {
                layoutProperties.height = convUtil.asDimension(value: attributes["height"]) ?? UniLayoutProperties.fitContent
            }
            
            // Size limit, weight and hiding behavior
            let visibility = convUtil.asString(value: attributes["visibility"]) ?? ""
            layoutProperties.hiddenTakesSpace = visibility == "invisible"
            layoutProperties.minWidth = convUtil.asDimension(value: attributes["minWidth"]) ?? 0
            layoutProperties.maxWidth = convUtil.asDimension(value: attributes["maxWidth"]) ?? 0xFFFFFF
            layoutProperties.minHeight = convUtil.asDimension(value: attributes["minHeight"]) ?? 0
            layoutProperties.maxHeight = convUtil.asDimension(value: attributes["maxHeight"]) ?? 0xFFFFFF
            layoutProperties.weight = CGFloat(convUtil.asFloat(value: attributes["weight"]) ?? 0)
            
            // Gravity
            layoutProperties.horizontalGravity = getHorizontalGravity(convUtil: convUtil, attributes: attributes) ?? 0
            layoutProperties.verticalGravity = getVerticalGravity(convUtil: convUtil, attributes: attributes) ?? 0
            
            // Mark layout as updated
            if let view = view {
                UniLayout.setNeedsLayout(view: view)
            }
        }
    }
    
    
    // --
    // MARK: Gravity helpers
    // --
    
    class func getHorizontalGravity(convUtil: InflatorConvUtil, attributes: [String: Any]) -> CGFloat? {
        // Extract horizontal gravity from shared horizontal/vertical string
        if let gravityString = attributes["gravity"] as? String {
            if gravityString == "center" {
                return 0.5
            } else if gravityString == "centerHorizontal" {
                return 0.5
            } else if gravityString == "left" {
                return 0
            } else if gravityString == "right" {
                return 1
            }
            return nil
        }
        
        // Check horizontal gravity being specified separately
        if let horizontalGravityString = attributes["horizontalGravity"] as? String {
            if horizontalGravityString == "center" {
                return 0.5
            } else if horizontalGravityString == "left" {
                return 0
            } else if horizontalGravityString == "right" {
                return 1
            }
            return nil
        }
        if let horizontalGravity = convUtil.asFloat(value: attributes["horizontalGravity"]) {
            return CGFloat(horizontalGravity)
        }
        return nil
    }
    
    class func getVerticalGravity(convUtil: InflatorConvUtil, attributes: [String: Any]) -> CGFloat? {
        // Extract horizontal gravity from shared horizontal/vertical string
        if let gravityString = attributes["gravity"] as? String {
            if gravityString == "center" {
                return 0.5
            } else if gravityString == "centerVertical" {
                return 0.5
            } else if gravityString == "bottom" {
                return 1
            } else if gravityString == "top" {
                return 0
            }
            return nil
        }
        
        // Check horizontal gravity being specified separately
        if let verticalGravityString = attributes["verticalGravity"] as? String {
            if verticalGravityString == "center" {
                return 0.5
            } else if verticalGravityString == "top" {
                return 0
            } else if verticalGravityString == "bottom" {
                return 1
            }
            return nil
        }
        if let verticalGravity = convUtil.asFloat(value: attributes["verticalGravity"]) {
            return CGFloat(verticalGravity)
        }
        return nil
    }
    
}

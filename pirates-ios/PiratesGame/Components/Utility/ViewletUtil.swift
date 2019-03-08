//
//  ViewletUtil.swift
//  Component utility: shared utilities for viewlet integration, also contains the viewlet for a basic UniView
//

import UIKit
import UniLayout
import ViewletCreator

class ViewletUtil {
    
    // --
    // MARK: Basic view viewlet
    // --
    
    class func basicViewViewlet() -> Viewlet {
        return viewletClass()
    }
    
    private class viewletClass: Viewlet {
        
        func create() -> UIView {
            return UniView()
        }
        
        func update(view: UIView, attributes: [String: Any], parent: UIView?, binder: ViewletBinder?) -> Bool {
            ViewletUtil.applyGenericViewAttributes(view: view, attributes: attributes)
            return true
        }
        
        func canRecycle(view: UIView, attributes: [String: Any]) -> Bool {
            return view is UniView
        }
        
    }
    
    
    // --
    // MARK: Inflate with assertion
    // --

    class func assertInflateOn(view: UIView, attributes: [String: Any]?, parent: UIView? = nil, binder: ViewletBinder? = nil) {
        // Check if attributes are not nil
        assert(attributes != nil, "Attributes are null, load issue?")
        
        // Check viewlet name
        var viewletName: String?
        if attributes != nil {
            viewletName = ViewletCreator.findViewletNameInAttributes(attributes!)
            assert(viewletName != nil, "No viewlet found, JSON structure issue?")
        }
        
        // Check if the viewlet is registered
        if attributes != nil {
            let viewlet = ViewletCreator.findViewletInAttributes(attributes!)
            assert(viewlet != nil, "No viewlet implementation found, registration issue of \(String(describing: viewletName))?")
        }

        // Check result of inflate
        let result = ViewletCreator.inflateOn(view: view, attributes: attributes, parent: parent, binder: binder)
        assert(result == true, "Can't inflate viewlet, class doesn't match with \(String(describing: viewletName))?")
    }
    
    
    // --
    // MARK: Subview creation
    // --
    
    class func createSubviews(container: UIView, parent: UIView?, attributes: [String: Any], subviewItems: Any?, binder: ViewletBinder?) {
        // Check if views are the same before and after the update, then they can be updated instead of re-created
        var canRecycle = false
        let recycling = ViewletConvUtil.asBool(value: attributes["recycling"]) ?? false
        let items = ViewletCreator.attributesForSubViewletList(subviewItems)
        if recycling {
            if items.count == container.subviews.count {
                canRecycle = true
                for i in 0..<items.count {
                    if !ViewletCreator.canRecycle(view: container.subviews[i], attributes: items[i]) {
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
                    ViewletCreator.inflateOn(view: view, attributes: item, parent: parent)
                    ViewletUtil.applyLayoutAttributes(view: view, attributes: item)
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
                if let view = ViewletCreator.create(attributes: item, parent: parent, binder: binder) {
                    container.addSubview(view)
                    ViewletUtil.applyLayoutAttributes(view: view, attributes: item)
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
                scrollView.contentOffset = CGPoint(x: CGFloat(0), y: max(0, min(scrollPosition, foundScrollView!.contentSize.height - foundScrollView!.bounds.height)))
            }
        }
    }
    
    
    // --
    // MARK: Easy reference binding
    // --
    
    class func bindRef(view: UIView?, attributes: [String: Any], binder: ViewletBinder?) {
        if let view = view, binder != nil {
            if let refId = attributes["refId"] as? String {
                binder?.onBind(refId: refId, view: view)
            }
        }
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
    
    class func applyGenericViewAttributes(view: UIView, attributes: [String: Any]) {
        // Standard view properties
        let visibility = ViewletConvUtil.asString(value: attributes["visibility"]) ?? ""
        view.isHidden = visibility == "hidden" || visibility == "invisible"
        if let control = view as? UIControl {
            control.isEnabled = !(ViewletConvUtil.asBool(value: attributes["disabled"]) ?? false)
        }
        // TODO: ignore background color for text entry
        view.backgroundColor = ViewletConvUtil.asColor(value: attributes["backgroundColor"]) ?? UIColor.clear
        
        // Padding
        if var uniPaddedView = view as? UniLayoutPaddedView {
            let paddingArray = ViewletConvUtil.asDimensionArray(value: attributes["padding"])
            var defaultPadding: [CGFloat] = [ 0, 0, 0, 0 ]
            if paddingArray.count == 4 {
                defaultPadding = paddingArray
            }
            uniPaddedView.padding = UIEdgeInsets(
                top: ViewletConvUtil.asDimension(value: attributes["paddingTop"]) ?? defaultPadding[1],
                left: ViewletConvUtil.asDimension(value: attributes["paddingLeft"]) ?? defaultPadding[0],
                bottom: ViewletConvUtil.asDimension(value: attributes["paddingBottom"]) ?? defaultPadding[3],
                right: ViewletConvUtil.asDimension(value: attributes["paddingRight"]) ?? defaultPadding[2])
        }
    }
    
    
    // --
    // MARK: Shared layout properties handling
    // --
    
    class func applyLayoutAttributes(view: UIView?, attributes: [String: Any]) {
        if let layoutProperties = (view as? UniLayoutView)?.layoutProperties {
            // Margin
            let marginArray = ViewletConvUtil.asDimensionArray(value: attributes["margin"])
            var defaultMargin: [CGFloat] = [ 0, 0, 0, 0 ]
            if marginArray.count == 4 {
                defaultMargin = marginArray
            }
            layoutProperties.margin = UIEdgeInsets(
                top: ViewletConvUtil.asDimension(value: attributes["marginTop"]) ?? defaultMargin[1],
                left: ViewletConvUtil.asDimension(value: attributes["marginLeft"]) ?? defaultMargin[0],
                bottom: ViewletConvUtil.asDimension(value: attributes["marginBottom"]) ?? defaultMargin[3],
                right: ViewletConvUtil.asDimension(value: attributes["marginRight"]) ?? defaultMargin[2])
            layoutProperties.spacingMargin = ViewletConvUtil.asDimension(value: attributes["marginSpacing"]) ?? 0
            
            // Forced size or stretching
            let widthString = ViewletConvUtil.asString(value: attributes["width"]) ?? ""
            let heightString = ViewletConvUtil.asString(value: attributes["height"]) ?? ""
            if widthString == "stretchToParent" {
                layoutProperties.width = UniLayoutProperties.stretchToParent
            } else if widthString == "fitContent" {
                layoutProperties.width = UniLayoutProperties.fitContent
            } else {
                layoutProperties.width = ViewletConvUtil.asDimension(value: attributes["width"]) ?? UniLayoutProperties.fitContent
            }
            if heightString == "stretchToParent" {
                layoutProperties.height = UniLayoutProperties.stretchToParent
            } else if heightString == "fitContent" {
                layoutProperties.height = UniLayoutProperties.fitContent
            } else {
                layoutProperties.height = ViewletConvUtil.asDimension(value: attributes["height"]) ?? UniLayoutProperties.fitContent
            }
            
            // Size limit, weight and hiding behavior
            let visibility = ViewletConvUtil.asString(value: attributes["visibility"]) ?? ""
            layoutProperties.hiddenTakesSpace = visibility == "invisible"
            layoutProperties.minWidth = ViewletConvUtil.asDimension(value: attributes["minWidth"]) ?? 0
            layoutProperties.maxWidth = ViewletConvUtil.asDimension(value: attributes["maxWidth"]) ?? 0xFFFFFF
            layoutProperties.minHeight = ViewletConvUtil.asDimension(value: attributes["minHeight"]) ?? 0
            layoutProperties.maxHeight = ViewletConvUtil.asDimension(value: attributes["maxHeight"]) ?? 0xFFFFFF
            layoutProperties.weight = CGFloat(ViewletConvUtil.asFloat(value: attributes["weight"]) ?? 0)
            
            // Gravity
            layoutProperties.horizontalGravity = getHorizontalGravity(attributes: attributes) ?? 0
            layoutProperties.verticalGravity = getVerticalGravity(attributes: attributes) ?? 0
            
            // Mark layout as updated
            if let view = view {
                UniLayout.setNeedsLayout(view: view)
            }
        }
    }
    
    
    // --
    // MARK: Gravity helpers
    // --
    
    class func getHorizontalGravity(attributes: [String: Any]) -> CGFloat? {
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
        if let horizontalGravity = ViewletConvUtil.asFloat(value: attributes["horizontalGravity"]) {
            return CGFloat(horizontalGravity)
        }
        return nil
    }
    
    class func getVerticalGravity(attributes: [String: Any]) -> CGFloat? {
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
        if let verticalGravity = ViewletConvUtil.asFloat(value: attributes["verticalGravity"]) {
            return CGFloat(verticalGravity)
        }
        return nil
    }
    
}

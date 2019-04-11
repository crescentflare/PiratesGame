//
//  SolidNavigationBar.swift
//  Navigation bar: a simple navigation bar, the default for apps
//

import UIKit
import UniLayout
import JsonInflator

class SolidNavigationBar: FrameContainerView, NavigationBarComponent {
    
    // --
    // MARK: Layout JSON
    // --
    
    private let layoutFile = "SolidNavigationBar"
    
    
    // --
    // MARK: Bound views
    // --
    
    private var titleView: UniTextView?
    
    
    // --
    // MARK: Members
    // --
    
    var isTranslucent: Bool {
        get {
            return false
        }
    }
    
    override var padding: UIEdgeInsets {
        didSet {
            super.padding = UIEdgeInsets(top: statusBarInset, left: padding.left, bottom: padding.bottom, right: padding.right)
        }
    }
    
    
    // --
    // MARK: Viewlet integration
    // --
    
    override class func viewlet() -> JsonInflatable {
        return ViewletClass()
    }
    
    private class ViewletClass: JsonInflatable {
        
        func create() -> Any {
            return SolidNavigationBar()
        }
        
        func update(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any], parent: Any?, binder: InflatorBinder?) -> Bool {
            if let navigationBar = object as? SolidNavigationBar {
                // Apply text
                if let localizedTitle = convUtil.asString(value: attributes["localizedTitle"]) {
                    navigationBar.title = localizedTitle.localized()
                } else {
                    navigationBar.title = convUtil.asString(value: attributes["title"])
                }
                
                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(convUtil: convUtil, view: navigationBar, attributes: attributes)
                return true
            }
            return false
        }
        
        func canRecycle(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any]) -> Bool {
            return object is SolidNavigationBar
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
        let binder = InflatorDictBinder()
        ViewletUtil.assertInflateOn(view: self, attributes: JsonLoader.shared.attributesFrom(jsonFile: layoutFile), parent: nil, binder: binder)
        titleView = binder.findByReference("title") as? UniTextView
    }
    
    
    // --
    // MARK: Configurable values
    // --
    
    var isLightContent: Bool {
        get {
            let whiteColor = UIColor.white
            let blackColor = UIColor.black
            let result = pickBestForegroundColor(backgroundColor: backgroundColor ?? UIColor.clear, lightForegroundColor: whiteColor, darkForegroundColor: blackColor)
            return result == whiteColor
        }
    }
    
    var statusBarInset: CGFloat = 0 {
        didSet {
            padding.top = statusBarInset
        }
    }
    
    var title: String? {
        set {
            titleView?.text = newValue
        }
        get { return titleView?.text }
    }
    
    override var backgroundColor: UIColor? {
        didSet {
            titleView?.textColor = isLightContent ? AppColors.textInverted : AppColors.text
        }
    }
    
    
    // --
    // MARK: Helper
    // --
    
    private func pickBestForegroundColor(backgroundColor: UIColor, lightForegroundColor: UIColor, darkForegroundColor: UIColor) -> UIColor {
        var red: CGFloat = 0
        var green: CGFloat = 0
        var blue: CGFloat = 0
        var alpha: CGFloat = 0
        if backgroundColor.getRed(&red, green: &green, blue: &blue, alpha: &alpha) {
            var colorComponents = [blue, green, red]
            for i in colorComponents.indices {
                if colorComponents[i] <= 0.03928 {
                    colorComponents[i] /= 12.92
                }
                colorComponents[i] = pow((colorComponents[i] + 0.055) / 1.055, 2.4)
            }
            let intensity = 0.2126 * colorComponents[0] + 0.7152 * colorComponents[1] + 0.0722 * colorComponents[2]
            return intensity > 0.179 ? darkForegroundColor : lightForegroundColor
        }
        return lightForegroundColor
    }
    
}

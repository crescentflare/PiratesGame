//
//  SimpleNavigationBar.swift
//  Navigation bar: a simple navigation bar, the default for apps
//

import UIKit
import UniLayout
import JsonInflator

class SimpleNavigationBar: LinearContainerView, NavigationBarComponent {

    // --
    // MARK: Layout JSON
    // --
    
    private let layoutFile = "SimpleNavigationBar"
    
    
    // --
    // MARK: Bound views
    // --
    
    private var statusContainerView: UniView?
    private var barContainerView: LinearContainerView?
    private var titleView: UniTextView?
    private var backIconView: TappableImageView?
    private var menuActionIconView: TappableImageView?
    private var menuActionTextView: ButtonView?
    private var backIconSpacerView: UniImageView?
    private var menuActionIconSpacerView: UniImageView?
    private var menuActionTextSpacerView: ButtonView?


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
    
    
    // --
    // MARK: Viewlet integration
    // --
    
    override class func viewlet() -> JsonInflatable {
        return ViewletClass()
    }
    
    private class ViewletClass: JsonInflatable {
        
        func create() -> Any {
            return SimpleNavigationBar()
        }
        
        func update(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any], parent: Any?, binder: InflatorBinder?) -> Bool {
            if let navigationBar = object as? SimpleNavigationBar {
                // Apply text
                if let localizedTitle = convUtil.asString(value: attributes["localizedTitle"]) {
                    navigationBar.title = localizedTitle.localized()
                } else {
                    navigationBar.title = convUtil.asString(value: attributes["title"])
                }
                
                // Apply actions
                navigationBar.backIcon = ImageSource(value: attributes["backIcon"])
                navigationBar.menuActionIcon = ImageSource(value: attributes["menuActionIcon"])
                if let localizedMenuActionText = convUtil.asString(value: attributes["localizedMenuActionText"]) {
                    navigationBar.menuActionText = localizedMenuActionText.localized()
                } else {
                    navigationBar.menuActionText = convUtil.asString(value: attributes["menuActionText"])
                }
                navigationBar.backEvent = AppEvent(value: attributes["backEvent"])
                navigationBar.menuActionEvent = AppEvent(value: attributes["menuActionEvent"])

                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(convUtil: convUtil, view: navigationBar, attributes: attributes)

                // Forward event observer
                if let eventObserver = parent as? AppEventObserver {
                    navigationBar.eventObserver = eventObserver
                }
                return true
            }
            return false
        }
        
        func canRecycle(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any]) -> Bool {
            return object is SimpleNavigationBar
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
        // Inflate
        let binder = InflatorDictBinder()
        ViewletUtil.assertInflateOn(view: self, attributes: JsonLoader.shared.attributesFrom(jsonFile: layoutFile), parent: nil, binder: binder)
        
        // Bind views
        statusContainerView = binder.findByReference("statusContainer") as? UniView
        barContainerView = binder.findByReference("barContainer") as? LinearContainerView
        titleView = binder.findByReference("title") as? UniTextView
        backIconView = binder.findByReference("backIcon") as? TappableImageView
        menuActionIconView = binder.findByReference("menuActionIcon") as? TappableImageView
        menuActionTextView = binder.findByReference("menuActionText") as? ButtonView
        backIconSpacerView = binder.findByReference("backIconSpacer") as? UniImageView
        menuActionIconSpacerView = binder.findByReference("menuActionIconSpacer") as? UniImageView
        menuActionTextSpacerView = binder.findByReference("menuActionTextSpacer") as? ButtonView
    }
    

    // --
    // MARK: Configurable values
    // --

    var statusBarInset: CGFloat = 0 {
        didSet {
            statusContainerView?.layoutProperties.height = statusBarInset
        }
    }
    
    var title: String? {
        set {
            titleView?.text = newValue
        }
        get { return titleView?.text }
    }
    
    var backIcon: ImageSource? {
        didSet {
            backIconView?.source = colorizedCopyOfImageSource(backIcon)
            backIconSpacerView?.image = backIcon?.getImage()
            backIconView?.visibility = backIcon != nil ? .visible : .hidden
            backIconSpacerView?.visibility = backIcon != nil ? .invisible : .hidden
        }
    }
    
    var menuActionIcon: ImageSource? {
        didSet {
            menuActionIconView?.source = colorizedCopyOfImageSource(menuActionIcon)
            menuActionIconSpacerView?.image = menuActionIcon?.getImage()
            menuActionIconView?.visibility = menuActionIcon != nil ? .visible : .hidden
            menuActionIconSpacerView?.visibility = menuActionIcon != nil ? .invisible : .hidden
        }
    }

    var menuActionText: String? {
        didSet {
            menuActionTextView?.setTitle(menuActionText, for: .normal)
            menuActionTextSpacerView?.setTitle(menuActionText, for: .normal)
            menuActionTextView?.visibility = menuActionText != nil ? .visible : .hidden
            menuActionTextSpacerView?.visibility = menuActionText != nil ? .invisible : .hidden
        }
    }
    
    var backEvent: AppEvent? {
        didSet {
            backIconView?.tapEvent = backEvent
        }
    }

    var menuActionEvent: AppEvent? {
        didSet {
            menuActionIconView?.tapEvent = menuActionEvent
            menuActionTextView?.tapEvent = menuActionEvent
        }
    }

    override var backgroundColor: UIColor? {
        didSet {
            titleView?.textColor = isLightContent ? AppColors.textInverted : AppColors.text
            backIconView?.normalTintColor = isLightContent ? AppColors.textInverted : AppColors.text
            menuActionIconView?.normalTintColor = isLightContent ? AppColors.textInverted : AppColors.text
            backIconView?.highlightedColor = isLightContent ? AppColors.textInverted.withAlphaComponent(0.5) : AppColors.text.withAlphaComponent(0.5)
            menuActionIconView?.highlightedColor = isLightContent ? AppColors.textInverted.withAlphaComponent(0.5) : AppColors.text.withAlphaComponent(0.5)
            menuActionTextView?.setColorStyle(isLightContent ? .navigationBarInverted : .navigationBar)
        }
    }


    // --
    // MARK: Helper
    // --
    
    private func colorizedCopyOfImageSource(_ imageSource: ImageSource?) -> ImageSource? {
        if let imageSource = imageSource {
            let copy = ImageSource(dict: imageSource.dictionary)
            if copy.parameters["colorize"] == nil {
                copy.parameters["colorize"] = "#ffffff"
            }
            return copy
        }
        return nil
    }

}

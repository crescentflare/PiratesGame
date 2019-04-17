//
//  ButtonView.swift
//  Basic view: a button
//

import UIKit
import UniLayout
import JsonInflator

enum ButtonViewColorStyle: String {
    
    case primary = "primary"
    case primaryInverted = "primaryInverted"
    case secondary = "secondary"
    case navigationBar = "navigationBar"
    case navigationBarInverted = "navigationBarInverted"
    
}

class ButtonView: UniButtonView, AppEventLabeledSender {
    
    // --
    // MARK: Viewlet integration
    // --
    
    class func viewlet() -> JsonInflatable {
        return ViewletClass()
    }
    
    private class ViewletClass: JsonInflatable {
        
        func create() -> Any {
            return ButtonView()
        }
        
        func update(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any], parent: Any?, binder: InflatorBinder?) -> Bool {
            if let button = object as? ButtonView {
                // Apply text
                if let localizedText = convUtil.asString(value: attributes["localizedText"]) {
                    button.setTitle(localizedText.localized(), for: .normal)
                } else {
                    button.setTitle(convUtil.asString(value: attributes["text"]), for: .normal)
                }

                // Apply font
                let fontSize = convUtil.asDimension(value: attributes["textSize"]) ?? AppDimensions.buttonText
                button.titleLabel?.font = AppFonts.normal.font(ofSize: fontSize)

                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(convUtil: convUtil, view: button, attributes: attributes)

                // Button background and text color based on color style
                button.setColorStyle(ButtonViewColorStyle(rawValue: convUtil.asString(value: attributes["colorStyle"]) ?? "") ?? .primary)
                
                // Event handling
                button.tapEvent = AppEvent(value: attributes["tapEvent"])

                // Forward event observer
                if let eventObserver = parent as? AppEventObserver {
                    button.eventObserver = eventObserver
                }
                return true
            }
            return false
        }
        
        func canRecycle(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any]) -> Bool {
            return object is ButtonView
        }
        
    }
    
    
    // --
    // MARK: Styles
    // --
    
    class var defaultStyle: [String: Any] {
        get {
            return [
                "padding": [ "$buttonHorizontalPadding", "$buttonVerticalPadding", "$buttonHorizontalPadding", "$buttonVerticalPadding" ],
                "minHeight": "$buttonHeight",
                "textSize": "$buttonText"
            ]
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
        setColorStyle(.primary)
        layoutProperties.minHeight = AppDimensions.buttonHeight
        titleLabel?.font = AppFonts.normal.font(ofSize: AppDimensions.buttonText)
        titleLabel?.numberOfLines = 0
    }
    
    
    // --
    // MARK: Configurable values
    // --
    
    weak var eventObserver: AppEventObserver?
    
    var tapEvent: AppEvent? {
        didSet {
            if oldValue != nil {
                removeTarget(self, action: #selector(tappedButton(_:)), for: .touchUpInside)
            }
            if tapEvent != nil {
                addTarget(self, action: #selector(tappedButton(_:)), for: .touchUpInside)
            }
        }
    }
    
    func setColorStyle(_ colorStyle: ButtonViewColorStyle) {
        // Determine colors
        var textColorSet = textColorsForPrimary()
        switch colorStyle {
        case .primary:
            textColorSet = textColorsForPrimary()
            setBackgroundImage(ButtonView.primaryButtonImage, for: .normal)
            setBackgroundImage(ButtonView.primaryHighlightButtonImage, for: .highlighted)
            setBackgroundImage(ButtonView.disabledButtonImage, for: .disabled)
        case .primaryInverted:
            textColorSet = textColorsForPrimaryInverted()
            setBackgroundImage(ButtonView.invertedButtonImage, for: .normal)
            setBackgroundImage(ButtonView.invertedHighlightButtonImage, for: .highlighted)
            setBackgroundImage(ButtonView.disabledInvertedButtonImage, for: .disabled)
        case .secondary:
            textColorSet = textColorsForSecondary()
            setBackgroundImage(ButtonView.secondaryButtonImage, for: .normal)
            setBackgroundImage(ButtonView.secondaryHighlightButtonImage, for: .highlighted)
            setBackgroundImage(ButtonView.disabledButtonImage, for: .disabled)
        case .navigationBar:
            textColorSet = textColorsForNavigationBar()
            setBackgroundImage(nil, for: .normal)
            setBackgroundImage(nil, for: .highlighted)
            setBackgroundImage(nil, for: .disabled)
        case .navigationBarInverted:
            textColorSet = textColorsForNavigationBarInverted()
            setBackgroundImage(nil, for: .normal)
            setBackgroundImage(nil, for: .highlighted)
            setBackgroundImage(nil, for: .disabled)
        }
        
        // Apply title color and padding
        let horizontalPadding = colorStyle == .navigationBar || colorStyle == .navigationBarInverted ? AppDimensions.buttonNavigationHorizontalPadding : AppDimensions.buttonHorizontalPadding
        setTitleColor(textColorSet[0], for: .normal)
        setTitleColor(textColorSet[1], for: .highlighted)
        setTitleColor(textColorSet[2], for: .disabled)
        padding = UIEdgeInsets(top: AppDimensions.buttonVerticalPadding, left: horizontalPadding, bottom: AppDimensions.buttonVerticalPadding, right: horizontalPadding)
    }
    

    // --
    // MARK: Interaction
    // --
    
    var senderLabel: String? {
        get {
            return titleLabel?.text
        }
    }

    @objc func tappedButton(_ sender: Any) {
        if let tapEvent = tapEvent {
            eventObserver?.observedEvent(tapEvent, sender: self)
        }
    }
    

    // --
    // MARK: Helpers
    // --
    
    private func textColorsForPrimary() -> [UIColor] {
        return [ AppColors.textInverted, AppColors.textInverted, AppColors.textDisabled ]
    }
    
    private func textColorsForPrimaryInverted() -> [UIColor] {
        return [ AppColors.primary, AppColors.primary, AppColors.primary ]
    }
    
    private func textColorsForSecondary() -> [UIColor] {
        return [ AppColors.textInverted, AppColors.textInverted, AppColors.textDisabled ]
    }
    
    private func textColorsForNavigationBar() -> [UIColor] {
        return [ AppColors.text, AppColors.text.withAlphaComponent(0.5), AppColors.text.withAlphaComponent(0.5) ]
    }

    private func textColorsForNavigationBarInverted() -> [UIColor] {
        return [ AppColors.textInverted, AppColors.textInverted.withAlphaComponent(0.5), AppColors.textInverted.withAlphaComponent(0.5) ]
    }

    private static var primaryButtonImage: UIImage? {
        get {
            return imageForButton(colorDefinition: "$secondary", edgeColorDefinition: "$secondaryHighlight")
        }
    }
    
    private static var primaryHighlightButtonImage: UIImage? {
        get {
            return imageForButton(colorDefinition: "$secondaryHighlight")
        }
    }
    
    private static var secondaryButtonImage: UIImage? {
        get {
            return imageForButton(colorDefinition: "$primary", edgeColorDefinition: "$primaryHighlight")
        }
    }
    
    private static var secondaryHighlightButtonImage: UIImage? {
        get {
            return imageForButton(colorDefinition: "$primaryHighlight")
        }
    }
    
    private static var invertedButtonImage: UIImage? {
        get {
            return imageForButton(colorDefinition: "$inverted", edgeColorDefinition: "$invertedHighlight")
        }
    }
    
    private static var invertedHighlightButtonImage: UIImage? {
        get {
            return imageForButton(colorDefinition: "$invertedHighlight")
        }
    }
    
    private static var disabledButtonImage: UIImage? {
        get {
            return imageForButton(colorDefinition: "$disabled", edgeColorDefinition: "$disabledHighlight")
        }
    }
    
    private static var disabledInvertedButtonImage: UIImage? {
        get {
            return imageForButton(colorDefinition: "$disabledInverted", edgeColorDefinition: "$disabledInvertedHighlight")
        }
    }

    private class func imageForButton(colorDefinition: String, edgeColorDefinition: String? = nil) -> UIImage? {
        if let edgeColorDefinition = edgeColorDefinition {
            let attributes: [String: Any] = [
                "type": "generate",
                "name": "filledOval",
                "width": 64,
                "height": Float(AppDimensions.buttonHeight) * 1.625,
                "imageWidth": 64,
                "imageHeight": Float(AppDimensions.buttonHeight),
                "color": edgeColorDefinition,
                "caching": "always",
                "otherSources": [
                    [
                        "type": "generate",
                        "name": "filledOval",
                        "width": 52,
                        "height": Float(AppDimensions.buttonHeight) * 2,
                        "color": colorDefinition
                    ]
                ]
            ]
            return ImageSource(dict: attributes).getImage()?.stretchableImage(withLeftCapWidth: 12, topCapHeight: 0)
        }
        let attributes: [String: Any] = [
            "type": "generate",
            "name": "filledOval",
            "width": 64,
            "height": Float(AppDimensions.buttonHeight) * 1.625,
            "imageWidth": 64,
            "imageHeight": Float(AppDimensions.buttonHeight),
            "color": colorDefinition,
            "caching": "always"
        ]
        return ImageSource(dict: attributes).getImage()?.stretchableImage(withLeftCapWidth: 12, topCapHeight: 0)
    }

}

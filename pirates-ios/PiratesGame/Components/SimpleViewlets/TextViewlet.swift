//
//  TextViewlet.swift
//  Basic view viewlet: a text view
//

import UIKit
import UniLayout
import ViewletCreator
import SimpleMarkdownParser

enum TextAlignment: String {
    
    case left = "left"
    case center = "center"
    case right = "right"
    
}

class TextViewlet {
    
    // --
    // MARK: Viewlet instance
    // --
    
    class func viewlet() -> Viewlet {
        return ViewletClass()
    }
    
    private class ViewletClass: Viewlet {
        
        func create() -> UIView {
            return UniTextView()
        }
        
        func update(view: UIView, attributes: [String : Any], parent: UIView?, binder: ViewletBinder?) -> Bool {
            if let textView = view as? UniTextView {
                // Apply text styling
                let fontSize = ViewletConvUtil.asDimension(value: attributes["textSize"]) ?? AppDimensions.text
                let font = AppFonts.font(withName: ViewletConvUtil.asString(value: attributes["font"]) ?? "unknown", ofSize: fontSize)
                textView.font = font
                textView.numberOfLines = ViewletConvUtil.asInt(value: attributes["maxLines"]) ?? 0
                textView.attributedText = nil
                textView.textColor = ViewletConvUtil.asColor(value: attributes["textColor"]) ?? AppColors.text
                
                // Apply text alignment
                let textAlignment = TextAlignment(rawValue: ViewletConvUtil.asString(value: attributes["textAlignment"]) ?? "") ?? .left
                switch textAlignment {
                case .center:
                    textView.textAlignment = .center
                case .right:
                    textView.textAlignment = .right
                default:
                    textView.textAlignment = .left
                }
                
                // Apply text (markdown needs to be set after text alignment)
                if let localizedMarkdownText = ViewletConvUtil.asString(value: attributes["localizedMarkdownText"]) {
                    textView.attributedText = SimpleMarkdownConverter.toAttributedString(defaultFont: font, markdownText: localizedMarkdownText.localized(), attributedStringGenerator: MarkdownGenerator(noColorization: textView.textColor == AppColors.textInverted))
                } else if let markdownText = ViewletConvUtil.asString(value: attributes["markdownText"]) {
                    textView.attributedText = SimpleMarkdownConverter.toAttributedString(defaultFont: font, markdownText: markdownText, attributedStringGenerator: MarkdownGenerator(noColorization: textView.textColor == AppColors.textInverted))
                } else if let localizedText = ViewletConvUtil.asString(value: attributes["localizedText"]) {
                    textView.text = localizedText.localized()
                } else {
                    textView.text = ViewletConvUtil.asString(value: attributes["text"])
                }
                
                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(view: view, attributes: attributes)
                return true
            }
            return false
        }
        
        func canRecycle(view: UIView, attributes: [String : Any]) -> Bool {
            return view is UniTextView
        }
        
    }
    
    class func newTextView() -> UniTextView {
        let textView = UniTextView()
        textView.font = AppFonts.normal.font(ofSize: AppDimensions.text)
        textView.textColor = AppColors.text
        return textView
    }
    
}

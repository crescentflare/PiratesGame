//
//  TextViewlet.swift
//  Basic view viewlet: a text view
//

import UIKit
import UniLayout
import JsonInflator
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
    
    class func viewlet() -> JsonInflatable {
        return ViewletClass()
    }
    
    private class ViewletClass: JsonInflatable {
        
        func create() -> Any {
            return UniTextView()
        }
        
        func update(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any], parent: Any?, binder: InflatorBinder?) -> Bool {
            if let textView = object as? UniTextView {
                // Apply text styling
                let fontSize = convUtil.asDimension(value: attributes["textSize"]) ?? AppDimensions.text
                let font = AppFonts.font(withName: convUtil.asString(value: attributes["font"]) ?? "unknown", ofSize: fontSize)
                textView.font = font
                textView.numberOfLines = convUtil.asInt(value: attributes["maxLines"]) ?? 0
                textView.attributedText = nil
                textView.textColor = convUtil.asColor(value: attributes["textColor"]) ?? AppColors.text
                
                // Apply text alignment
                let textAlignment = TextAlignment(rawValue: convUtil.asString(value: attributes["textAlignment"]) ?? "") ?? .left
                switch textAlignment {
                case .center:
                    textView.textAlignment = .center
                case .right:
                    textView.textAlignment = .right
                default:
                    textView.textAlignment = .left
                }
                
                // Apply text (markdown needs to be set after text alignment)
                if let localizedMarkdownText = convUtil.asString(value: attributes["localizedMarkdownText"]) {
                    textView.attributedText = SimpleMarkdownConverter.toAttributedString(defaultFont: font, markdownText: localizedMarkdownText.localized(), attributedStringGenerator: MarkdownGenerator(noColorization: textView.textColor == AppColors.textInverted))
                } else if let markdownText = convUtil.asString(value: attributes["markdownText"]) {
                    textView.attributedText = SimpleMarkdownConverter.toAttributedString(defaultFont: font, markdownText: markdownText, attributedStringGenerator: MarkdownGenerator(noColorization: textView.textColor == AppColors.textInverted))
                } else if let localizedText = convUtil.asString(value: attributes["localizedText"]) {
                    textView.text = localizedText.localized()
                } else {
                    textView.text = convUtil.asString(value: attributes["text"])
                }
                
                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(convUtil: convUtil, view: textView, attributes: attributes)
                return true
            }
            return false
        }
        
        func canRecycle(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any]) -> Bool {
            return object is UniTextView
        }
        
    }
    
    class func newTextView() -> UniTextView {
        let textView = UniTextView()
        textView.font = AppFonts.normal.font(ofSize: AppDimensions.text)
        textView.textColor = AppColors.text
        return textView
    }
    
}

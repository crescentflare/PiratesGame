//
//  AppColors.swift
//  Component styling: the colors used in the app, available everywhere
//

import UIKit
import JsonInflator

class AppColors {
    
    // --
    // MARK: Color lookup
    // --
    
    class AppColorLookup: InflatorColorLookup {
        
        func getColor(refId: String) -> UIColor? {
            let colorTable: [String: UIColor] = [
                "primary": primary,
                "primaryHighlight": primaryHighlight,
                "secondary": secondary,
                "secondaryHighlight": secondaryHighlight,
                "inverted": inverted,
                "invertedHighlight": invertedHighlight,
                "disabled": disabled,
                "disabledHighlight": disabledHighlight,
                "disabledInverted": disabledInverted,
                "disabledInvertedHighlight": disabledInvertedHighlight,

                "text": text,
                "textInverted": textInverted,
                "textDisabled": textDisabled
            ]
            return colorTable[refId]
        }
        
    }
    
    
    // --
    // MARK: Default colors
    // --
    
    class var primary: UIColor { get { return colorFromInt(0xff07575b) } }
    class var primaryHighlight: UIColor { get { return colorFromInt(0xff00747a) } }
    class var secondary: UIColor { get { return colorFromInt(0xffbf8207) } }
    class var secondaryHighlight: UIColor { get { return colorFromInt(0xffd9a500) } }
    class var inverted: UIColor { get { return colorFromInt(0xd8ffffff) }}
    class var invertedHighlight: UIColor { get { return colorFromInt(0xffffffff) }}
    class var disabled: UIColor { get { return colorFromInt(0xffd0d0d0) } }
    class var disabledHighlight: UIColor { get { return colorFromInt(0xffe8e8e8) } }
    class var disabledInverted: UIColor { get { return colorFromInt(0x80ffffff) }}
    class var disabledInvertedHighlight: UIColor { get { return colorFromInt(0x98ffffff) }}

    
    // --
    // MARK: Text
    // --

    class var text: UIColor { get { return colorFromInt(0xff1a1a1a) } }
    class var textInverted: UIColor { get { return colorFromInt(0xffffffff) } }
    class var textDisabled: UIColor { get { return colorFromInt(0xff9a9a9a) } }

    
    // --
    // MARK: Helper
    // --
    
    static func colorFromInt(_ intValue: UInt32) -> UIColor {
        let alpha = CGFloat((intValue & 0xff000000) >> 24) / 255
        let red = CGFloat((intValue & 0xff0000) >> 16) / 255
        let green = CGFloat((intValue & 0xff00) >> 8) / 255
        let blue = CGFloat(intValue & 0xff) / 255
        return UIColor(red: red, green: green, blue: blue, alpha: alpha)
    }
    
}

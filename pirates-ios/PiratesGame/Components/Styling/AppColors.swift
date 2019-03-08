//
//  AppColors.swift
//  Component styling: the colors used in the app, available everywhere
//

import UIKit
import ViewletCreator

class AppColors {
    
    // --
    // MARK: Color lookup
    // --
    
    class AppColorLookup: ViewletColorLookup {
        
        func getColor(refId: String) -> UIColor? {
            let colorTable: [String: UIColor] = [
                "primary": primary,
                "secondary": secondary,

                "text": text,
                "textInverted": textInverted
            ]
            return colorTable[refId]
        }
        
    }
    
    
    // --
    // MARK: Default colors
    // --
    
    class var primary: UIColor { get { return colorFromInt(0xff07575b) } }
    class var secondary: UIColor { get { return colorFromInt(0xffff7114) } }

    
    // --
    // MARK: Text
    // --

    class var text: UIColor { get { return colorFromInt(0xff000000) } }
    class var textInverted: UIColor { get { return colorFromInt(0xffffffff) } }

    
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

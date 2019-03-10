//
//  AppDimensions.swift
//  Component styling: the dimensions used in the app, available everywhere
//

import UIKit
import ViewletCreator

class AppDimensions {
    
    // --
    // MARK: Dimension lookup
    // --
    
    class AppDimensionLookup: ViewletDimensionLookup {
        
        func getDimension(refId: String) -> CGFloat? {
            let dimensionTable: [String: CGFloat] = [
                "text": text,
                "titleText": titleText,
                "subTitleText": subTitleText,
                "buttonText": buttonText,
                
                "buttonHorizontalPadding": buttonHorizontalPadding,
                "buttonVerticalPadding": buttonVerticalPadding,
                "buttonHeight": buttonHeight,

                "splashMargin": splashMargin,
                "splashMaxWidth": splashMaxWidth
            ]
            return dimensionTable[refId]
        }
        
    }
    
    
    // --
    // MARK: Text
    // --
    
    class var text: CGFloat { get { return 16 } }
    class var titleText: CGFloat { get { return 20 } }
    class var subTitleText: CGFloat { get { return 18 } }
    class var buttonText: CGFloat { get { return 16 } }


    // --
    // MARK: Component padding and spacing
    // --

    class var buttonHorizontalPadding: CGFloat { get { return 8 } }
    class var buttonVerticalPadding: CGFloat { get { return 4 } }
    class var buttonHeight: CGFloat { get { return 40 } }

    
    // --
    // MARK: Screen specific: splash screen
    // --

    class var splashMargin: CGFloat { get { return 16 } }
    class var splashMaxWidth: CGFloat { get { return 400 } }

}

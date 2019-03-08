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
                "titleText": text,
                "subTitleText": text,

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


    // --
    // MARK: Screen specific: splash screen
    // --

    class var splashMargin: CGFloat { get { return 16 } }
    class var splashMaxWidth: CGFloat { get { return 400 } }

}

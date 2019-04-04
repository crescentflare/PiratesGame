//
//  UIImageGenerator.swift
//  Image generators: a base class to generate a new UIImage dynamically
//

import UIKit
import JsonInflator

class UIImageGeneratorDrawing {
    
    let context: CGContext
    let drawRect: CGRect
    let cleanStart: Bool
    
    init(context: CGContext, drawRect: CGRect, cleanStart: Bool) {
        self.context = context
        self.drawRect = drawRect
        self.cleanStart = cleanStart
    }
    
}

open class UIImageGenerator {

    // --
    // MARK: Generate from attributes template
    // --

    func generate(attributes: [String: Any], onImage: UIImage? = nil) -> UIImage? {
        return nil
    }

        
    // --
    // MARK: Context handling
    // --
    
    func beginImageDrawing(withSize: CGSize? = nil, horizontalGravity: CGFloat = 0.5, verticalGravity: CGFloat = 0.5, forceImageSize: CGSize? = nil, onImage: UIImage? = nil) -> UIImageGeneratorDrawing? {
        // Return early for invalid sizes
        let wantSize = withSize ?? onImage?.size ?? CGSize.zero
        if wantSize.width <= 0 || wantSize.height <= 0 {
            return nil
        }

        // Begin context
        var contextSize = wantSize
        if let onImage = onImage {
            contextSize = onImage.size
        } else if let forceImageSize = forceImageSize {
            contextSize = forceImageSize
        }
        UIGraphicsBeginImageContextWithOptions(contextSize, false, 0)

        // Check when drawing on an image
        let context = UIGraphicsGetCurrentContext()
        if let onImage = onImage {
            onImage.draw(at: CGPoint.zero)
        }
        
        // Determine rectangle and return result
        if let context = context {
            let rect = CGRect(x: (contextSize.width - wantSize.width) * horizontalGravity, y: (contextSize.height - wantSize.height) * verticalGravity, width: wantSize.width, height: wantSize.height)
            return UIImageGeneratorDrawing(context: context, drawRect: rect, cleanStart: onImage == nil)
        }
        return nil
    }
    
    func endDrawingResult() -> UIImage? {
        let result = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return result
    }

    
    // --
    // MARK: Attribute helpers
    // --
    
    func sizeFromAttributes(convUtil: InflatorConvUtil, attributes: [String: Any]) -> CGSize? {
        var size: CGSize? = CGSize(width: convUtil.asDimension(value: attributes["width"]) ?? 0, height: convUtil.asDimension(value: attributes["height"]) ?? 0)
        if size?.width ?? 0 <= 0 || size?.height ?? 0 <= 0 {
            size = nil
        }
        return size
    }

    func imageSizeFromAttributes(convUtil: InflatorConvUtil, attributes: [String: Any]) -> CGSize? {
        var size: CGSize? = CGSize(width: convUtil.asDimension(value: attributes["imageWidth"]) ?? 0, height: convUtil.asDimension(value: attributes["imageHeight"]) ?? 0)
        if size?.width ?? 0 <= 0 || size?.height ?? 0 <= 0 {
            size = nil
        }
        return size
    }
    
    func gravityFromAttributes(convUtil: InflatorConvUtil, attributes: [String: Any]) -> CGPoint {
        return CGPoint(x: ViewletUtil.getHorizontalGravity(convUtil: convUtil, attributes: attributes) ?? 0.5, y: ViewletUtil.getVerticalGravity(convUtil: convUtil, attributes: attributes) ?? 0.5)
    }

}

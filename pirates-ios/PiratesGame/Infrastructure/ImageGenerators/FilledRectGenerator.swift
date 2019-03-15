//
//  FilledRectGenerator.swift
//  Image generators: generates a rectangle filled with a color
//

import UIKit
import ViewletCreator

class FilledRectGenerator: UIImageGenerator {

    func generate(color: UIColor, size: CGSize? = nil, cornerRadius: CGFloat = 0, horizontalGravity: CGFloat = 0.5, verticalGravity: CGFloat = 0.5, forceImageSize: CGSize? = nil, onImage: UIImage? = nil) -> UIImage? {
        if let drawing = beginImageDrawing(withSize: size, horizontalGravity: horizontalGravity, verticalGravity: verticalGravity, forceImageSize: forceImageSize, onImage: onImage) {
            // Cut out shape to allow transparent overdraw without blending
            let rect = drawing.drawRect
            if !drawing.cleanStart && color.cgColor.alpha < 1 {
                // Determine the rectangle, shrink shape if it's not on a pixel boundary to improve edge blending
                var cutRect = CGRect(x: rect.origin.x, y: rect.origin.y, width: rect.width, height: rect.height)
                if abs(cutRect.origin.x - floor(cutRect.origin.x)) > 0.001 {
                    cutRect.origin.x += 0.5
                    cutRect.size.width -= 0.5
                }
                if abs(cutRect.origin.y - floor(cutRect.origin.y)) > 0.001 {
                    cutRect.origin.y += 0.5
                    cutRect.size.height -= 0.5
                }
                if abs(cutRect.origin.x + cutRect.size.width - floor(cutRect.origin.x + cutRect.size.width)) > 0.001 {
                    cutRect.size.width -= 0.5
                }
                if abs(cutRect.origin.y + cutRect.size.height - floor(cutRect.origin.y + cutRect.size.height)) > 0.001 {
                    cutRect.size.height -= 0.5
                }
                
                // Clear shape
                drawing.context.setBlendMode(.clear)
                if cornerRadius > 0 {
                    let path = UIBezierPath(roundedRect: cutRect, cornerRadius: cornerRadius)
                    drawing.context.saveGState()
                    drawing.context.addPath(path.cgPath)
                    drawing.context.fillPath()
                    drawing.context.restoreGState()
                } else {
                    drawing.context.fill(cutRect)
                }
                drawing.context.setBlendMode(.normal)
            }
            
            // Draw
            drawing.context.setFillColor(color.cgColor)
            if cornerRadius > 0 {
                let path = UIBezierPath(roundedRect: rect, cornerRadius: cornerRadius)
                drawing.context.addPath(path.cgPath)
                drawing.context.fillPath()
            } else {
                drawing.context.fill(rect)
            }
            return endDrawingResult()
        }
        return nil
    }
    
    override func generate(attributes: [String: Any], onImage: UIImage? = nil) -> UIImage? {
        let gravity = gravityFromAttributes(attributes)
        return generate(color: ViewletConvUtil.asColor(value: attributes["color"]) ?? UIColor.clear, size: sizeFromAttributes(attributes), cornerRadius: ViewletConvUtil.asDimension(value: attributes["cornerRadius"]) ?? 0, horizontalGravity: gravity.x, verticalGravity: gravity.y, forceImageSize: imageSizeFromAttributes(attributes), onImage: onImage)
    }

}

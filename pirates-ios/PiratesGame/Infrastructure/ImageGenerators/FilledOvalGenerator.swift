//
//  FilledOvalGenerator.swift
//  Image generators: generates an oval filled with a color
//

import UIKit
import ViewletCreator

class FilledOvalGenerator: UIImageGenerator {

    func generate(color: UIColor, size: CGSize? = nil, horizontalGravity: CGFloat = 0.5, verticalGravity: CGFloat = 0.5, forceImageSize: CGSize? = nil, onImage: UIImage? = nil) -> UIImage? {
        if let drawing = beginImageDrawing(withSize: size, horizontalGravity: horizontalGravity, verticalGravity: verticalGravity, forceImageSize: forceImageSize, onImage: onImage) {
            // Cut out shape to allow transparent overdraw without blending
            let rect = drawing.drawRect
            if !drawing.cleanStart && color.cgColor.alpha < 1 {
                drawing.context.setBlendMode(.clear)
                drawing.context.fillEllipse(in: CGRect(x: rect.origin.x + 0.5, y: rect.origin.y + 0.5, width: rect.width - 1, height: rect.height - 1))
                drawing.context.setBlendMode(.normal)
            }
            
            // Draw
            drawing.context.setFillColor(color.cgColor)
            drawing.context.fillEllipse(in: rect)
            return endDrawingResult()
        }
        return nil
    }
    
    override func generate(attributes: [String: Any], onImage: UIImage? = nil) -> UIImage? {
        let gravity = gravityFromAttributes(attributes)
        return generate(color: ViewletConvUtil.asColor(value: attributes["color"]) ?? UIColor.clear, size: sizeFromAttributes(attributes), horizontalGravity: gravity.x, verticalGravity: gravity.y, forceImageSize: imageSizeFromAttributes(attributes), onImage: onImage)
    }
    
}

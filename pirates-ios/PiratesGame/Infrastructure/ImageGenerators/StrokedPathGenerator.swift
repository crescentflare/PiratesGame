//
//  StrokedPathGenerator.swift
//  Image generators: generates a stroke following a path
//

import UIKit

class StrokedPathGenerator: UIImageGenerator {

    func generate(color: UIColor, strokeSize: CGFloat, points: [CGPoint] = [], horizontalGravity: CGFloat = 0.5, verticalGravity: CGFloat = 0.5, forceImageSize: CGSize? = nil, onImage: UIImage? = nil) -> UIImage? {
        var minPointX = CGFloat.greatestFiniteMagnitude
        var minPointY = CGFloat.greatestFiniteMagnitude
        var maxPointX = -CGFloat.greatestFiniteMagnitude
        var maxPointY = -CGFloat.greatestFiniteMagnitude
        for point in points {
            minPointX = min(minPointX, point.x - strokeSize)
            minPointY = min(minPointY, point.y - strokeSize)
            maxPointX = max(maxPointX, point.x + strokeSize)
            maxPointY = max(maxPointY, point.y + strokeSize)
        }
        let pathRect = CGRect(x: minPointX, y: minPointY, width: maxPointX - minPointX, height: maxPointY - minPointY)
        if pathRect.width > 0 && pathRect.height > 0 {
            if let drawing = beginImageDrawing(withSize: pathRect.size, horizontalGravity: horizontalGravity, verticalGravity: verticalGravity, forceImageSize: forceImageSize, onImage: onImage) {
                // Prepare path
                let rect = drawing.drawRect
                let path = UIBezierPath()
                var firstPoint = true
                path.lineWidth = strokeSize
                for point in points {
                    let position = CGPoint(x: point.x - pathRect.origin.x + rect.origin.x, y: point.y - pathRect.origin.y + rect.origin.y)
                    if firstPoint {
                        path.move(to: position)
                        firstPoint = false
                    } else {
                        path.addLine(to: position)
                    }
                }
                
                // Draw
                color.setStroke()
                path.stroke()
                return endDrawingResult()
            }
        }
        return nil
    }
    
    override func generate(attributes: [String: Any], onImage: UIImage? = nil) -> UIImage? {
        let convUtil = Inflators.viewlet.convUtil
        let gravity = gravityFromAttributes(convUtil: convUtil, attributes: attributes)
        var points = [CGPoint]()
        if let stringPoints = attributes["points"] as? String {
            let pointSets = stringPoints.split(separator: ";")
            for pointSet in pointSets {
                let pointPair = pointSet.trimmingCharacters(in: .whitespacesAndNewlines).split(separator: ",").map { String.init($0).trimmingCharacters(in: .whitespacesAndNewlines) }
                if pointPair.count == 2 {
                    points.append(CGPoint(x: convUtil.asDimension(value: pointPair[0]) ?? 0, y: convUtil.asDimension(value: pointPair[1]) ?? 0))
                }
            }
        } else if let dimensionalPointArray = attributes["points"] as? [[Any]] {
            for pointSet in dimensionalPointArray {
                if pointSet.count == 2 {
                    points.append(CGPoint(x: convUtil.asDimension(value: pointSet[0]) ?? 0, y: convUtil.asDimension(value: pointSet[1]) ?? 0))
                }
            }
        } else if let flatPointArray = attributes["points"] as? [Any] {
            for index in flatPointArray.indices {
                if index % 2 == 0 && index + 1 < flatPointArray.count {
                    points.append(CGPoint(x: convUtil.asDimension(value: flatPointArray[index]) ?? 0, y: convUtil.asDimension(value: flatPointArray[index + 1]) ?? 0))
                }
            }
        }
        return generate(color: convUtil.asColor(value: attributes["color"]) ?? UIColor.clear, strokeSize: convUtil.asDimension(value: attributes["strokeSize"]) ?? 0, points: points, horizontalGravity: gravity.x, verticalGravity: gravity.y, forceImageSize: imageSizeFromAttributes(convUtil: convUtil, attributes: attributes), onImage: onImage)
    }

}

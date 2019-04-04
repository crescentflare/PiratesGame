//
//  GradientView.swift
//  Basic view: a simple background with a gradient
//

import UIKit
import UniLayout
import JsonInflator

class GradientView: UniView {

    // --
    // MARK: Viewlet integration
    // --
    
    class func viewlet() -> JsonInflatable {
        return ViewletClass()
    }
    
    private class ViewletClass: JsonInflatable {
        
        func create() -> Any {
            return GradientView()
        }
        
        func update(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any], parent: Any?, binder: InflatorBinder?) -> Bool {
            if let gradientView = object as? GradientView {
                // Gradient properties
                gradientView.startColor = convUtil.asColor(value: attributes["startColor"]) ?? UIColor.clear
                gradientView.endColor = convUtil.asColor(value: attributes["endColor"]) ?? UIColor.clear
                gradientView.angle = convUtil.asInt(value: attributes["angle"]) ?? 0

                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(convUtil: convUtil, view: gradientView, attributes: attributes)
                return true
            }
            return false
        }
        
        func canRecycle(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any]) -> Bool {
            return object is GradientView
        }
        
    }


    // --
    // MARK: Initialization
    // --

    override init(frame: CGRect) {
        super.init(frame: frame)
        setup()
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        setup()
    }
    
    fileprivate func setup() {
    }
    

    // --
    // MARK: Configurable values
    // --
    
    var startColor: UIColor = UIColor.clear {
        didSet {
            updateLayer()
        }
    }

    var endColor: UIColor = UIColor.clear {
        didSet {
            updateLayer()
        }
    }

    var angle: Int = 0 {
        didSet {
            updateLayer()
        }
    }
    
    private func updateLayer() {
        if let gradientLayer = layer as? CAGradientLayer {
            // Set angle
            let alpha: Float = Float(angle) / 360
            let startPointX = powf(sinf(2 * Float.pi * ((alpha + 0.75) / 2)), 2)
            let startPointY = powf(sinf(2 * Float.pi * ((alpha + 0) / 2)), 2)
            let endPointX = powf(sinf(2 * Float.pi * ((alpha + 0.25) / 2)), 2)
            let endPointY = powf(sinf(2 * Float.pi * ((alpha + 0.5) / 2)), 2)
            gradientLayer.endPoint = CGPoint(x: CGFloat(endPointX), y: CGFloat(endPointY))
            gradientLayer.startPoint = CGPoint(x: CGFloat(startPointX), y: CGFloat(startPointY))
            
            // Set colors
            gradientLayer.colors = [startColor.cgColor, endColor.cgColor]
        }
    }


    // --
    // MARK: Custom layer class
    // --
    
    override class var layerClass: AnyClass {
        return CAGradientLayer.self
    }
    

    // --
    // MARK: Custom layout
    // --

    internal override func layoutSubviews() {
        super.layoutSubviews()
        layer.frame = bounds
    }
   
}

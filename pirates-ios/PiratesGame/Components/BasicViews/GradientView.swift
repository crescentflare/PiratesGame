//
//  GradientView.swift
//  Basic view: a simple background with a gradient
//

import UIKit
import UniLayout
import ViewletCreator

class GradientView: UniView {

    // --
    // MARK: Viewlet integration
    // --
    
    class func viewlet() -> Viewlet {
        return ViewletClass()
    }
    
    private class ViewletClass: Viewlet {
        
        func create() -> UIView {
            return GradientView()
        }
        
        func update(view: UIView, attributes: [String : Any], parent: UIView?, binder: ViewletBinder?) -> Bool {
            if let gradientView = view as? GradientView {
                // Gradient properties
                gradientView.startColor = ViewletConvUtil.asColor(value: attributes["startColor"]) ?? UIColor.clear
                gradientView.endColor = ViewletConvUtil.asColor(value: attributes["endColor"]) ?? UIColor.clear
                gradientView.angle = ViewletConvUtil.asInt(value: attributes["angle"]) ?? 0

                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(view: view, attributes: attributes)
                return true
            }
            return false
        }
        
        func canRecycle(view: UIView, attributes: [String : Any]) -> Bool {
            return view is GradientView
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

//
//  SplashLoadingBar.swift
//  Compound view: the loading bar on the splash screen
//

import UIKit
import UniLayout
import ViewletCreator

class SplashLoadingBar: FrameContainerView {

    // --
    // MARK: Layout JSON
    // --
    
    private let layoutFile = "SplashLoadingBar"


    // --
    // MARK: Constants
    // --
    
    static let animationDuration = 0.25


    // --
    // MARK: Bound views
    // --
    
    private var barView: UniImageView?

    
    // --
    // MARK: Members
    // --

    private var currentProgress: Float = 0.0
    private var isAnimating = false

    
    // --
    // MARK: Viewlet integration
    // --
    
    override class func viewlet() -> Viewlet {
        return ViewletClass()
    }
    
    private class ViewletClass: Viewlet {
        
        func create() -> UIView {
            return SplashLoadingBar()
        }
        
        func update(view: UIView, attributes: [String : Any], parent: UIView?, binder: ViewletBinder?) -> Bool {
            if let bar = view as? SplashLoadingBar {
                // Apply state
                bar.autoAnimation = ViewletConvUtil.asBool(value: attributes["autoAnimation"]) ?? false
                bar.progress = ViewletConvUtil.asFloat(value: attributes["progress"]) ?? 0
                
                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(view: view, attributes: attributes)
                return true
            }
            return false
        }
        
        func canRecycle(view: UIView, attributes: [String : Any]) -> Bool {
            return view is SplashLoadingBar
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
        let binder = ViewletDictBinder()
        ViewletUtil.assertInflateOn(view: self, attributes: ViewletLoader.attributesFrom(jsonFile: layoutFile), parent: nil, binder: binder)
        barView = binder.findByReference("bar") as? UniImageView
    }
    

    // --
    // MARK: Configurable values
    // --
    
    var autoAnimation = false

    var progress: Float {
        set {
            setProgress(newValue, animated: autoAnimation)
        }
        get { return currentProgress }
    }
    
    func setProgress(_ progress: Float, animated: Bool) {
        // Safeguard against non-changing operations
        if progress == currentProgress && !isAnimating {
            return
        }
        
        // Stop existing animation if it's busy
        if isAnimating {
            layer.removeAllAnimations()
            isAnimating = false
        }

        // Change state with optional animation
        if animated {
            ViewletUtil.waitViewLayout(view: self, completion: {
                self.currentProgress = min(progress, 1)
                self.isAnimating = true
                UIView.animate(withDuration: SplashLoadingBar.animationDuration, delay: 0, options: UIView.AnimationOptions.curveEaseOut, animations: {
                    self.setNeedsLayout()
                    self.layoutIfNeeded()
                }, completion: { (finished) -> Void in
                    self.isAnimating = false
                })
            }, timeout: {
                self.setProgress(progress, animated: false)
            })
        } else {
            currentProgress = min(progress, 1)
            setNeedsLayout()
            layoutIfNeeded()
        }
    }

    
    // --
    // MARK: Custom layout
    // --
    
    override func layoutSubviews() {
        if let barView = barView {
            let layoutProperties = barView.layoutProperties
            let maxWidth = bounds.width - padding.left - padding.right - layoutProperties.margin.left - layoutProperties.margin.right
            barView.layoutProperties.width = CGFloat(progress) * maxWidth
        }
        super.layoutSubviews()
    }
    
}

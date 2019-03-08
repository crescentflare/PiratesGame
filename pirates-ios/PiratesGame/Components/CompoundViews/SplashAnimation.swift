//
//  SplashAnimation.swift
//  Compound view: contains the publisher logo and background, handles animation
//

import UIKit
import UniLayout
import ViewletCreator

class SplashAnimation: FrameContainerView {

    // --
    // MARK: Layout JSON
    // --
    
    private let layoutFile = "SplashAnimation"


    // --
    // MARK: Members
    // --
    
    private var logoView: PublisherLogo?
    private var backgroundGradientView: GradientView?
    private var backgroundImageView: UniImageView?
    private var currentOn = false

    
    // --
    // MARK: Viewlet integration
    // --
    
    override class func viewlet() -> Viewlet {
        return ViewletClass()
    }
    
    private class ViewletClass: Viewlet {
        
        func create() -> UIView {
            return SplashAnimation()
        }
        
        func update(view: UIView, attributes: [String : Any], parent: UIView?, binder: ViewletBinder?) -> Bool {
            if let animation = view as? SplashAnimation {
                // Apply background
                animation.gradientColor = ViewletConvUtil.asColor(value: attributes["gradientColor"]) ?? UIColor.black
                animation.backgroundImage = ImageSource(string: ViewletConvUtil.asString(value: attributes["backgroundImage"]))
                
                // Apply state
                animation.autoAnimation = ViewletConvUtil.asBool(value: attributes["autoAnimation"]) ?? false
                animation.on = ViewletConvUtil.asBool(value: attributes["on"]) ?? false
                
                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(view: view, attributes: attributes)

                // Event handling
                animation.setOnEvent = AppEvent(value: attributes["setOnEvent"])

                // Forward event observer
                if let eventObserver = parent as? AppEventObserver {
                    animation.eventObserver = eventObserver
                }
                return true
            }
            return false
        }
        
        func canRecycle(view: UIView, attributes: [String : Any]) -> Bool {
            return view is SplashAnimation
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
        // Bind views
        let binder = ViewletDictBinder()
        ViewletUtil.assertInflateOn(view: self, attributes: ViewletLoader.attributesFrom(jsonFile: layoutFile), parent: nil, binder: binder)
        logoView = binder.findByReference("logo") as? PublisherLogo
        backgroundGradientView = binder.findByReference("backgroundGradient") as? GradientView
        backgroundImageView = binder.findByReference("backgroundImage") as? UniImageView
        
        // Default properties
        backgroundGradientView?.alpha = 0
    }
    

    // --
    // MARK: Configurable values
    // --
    
    var setOnEvent: AppEvent?
    
    var gradientColor: UIColor {
        set {
            backgroundGradientView?.endColor = newValue
        }
        get { return backgroundGradientView?.endColor ?? UIColor.clear }
    }
    
    var backgroundImage: ImageSource? {
        didSet {
            if backgroundImageView?.image != nil || currentOn {
                ImageViewlet.applyImageSource(imageView: backgroundImageView, source: backgroundImage)
            }
        }
    }

    var autoAnimation = false

    var on: Bool {
        set {
            setOn(newValue, animated: autoAnimation)
        }
        get { return currentOn }
    }
    
    func setOn(_ on: Bool, animated: Bool) {
        // Safeguard against non-changing operations
        if on == currentOn {
            return
        }
        
        // Change state with optional animation
        if animated {
            logoView?.setOn(on, animated: true, afterAssetLoad: {
                ImageViewlet.applyImageSource(imageView: self.backgroundImageView, source: self.backgroundImage)
            }, completion: {
                UIView.animate(withDuration: 0.5, delay: 0.5, options: [.curveEaseInOut], animations: {
                    self.backgroundGradientView?.alpha = on ? 1 : 0
                    self.logoView?.transform = CGAffineTransform(translationX: 0, y: on ? -UIScreen.main.bounds.height / 6 : 0)
                })
                self.currentOn = on
                if let setOnEvent = self.setOnEvent, on {
                    self.eventObserver?.observedEvent(setOnEvent, sender: self)
                }
            })
        } else {
            currentOn = on
            logoView?.setOn(on, animated: false)
            if on {
                ImageViewlet.applyImageSource(imageView: backgroundImageView, source: backgroundImage)
            }
            backgroundGradientView?.alpha = on ? 1 : 0
            if let setOnEvent = setOnEvent, on {
                eventObserver?.observedEvent(setOnEvent, sender: self)
            }
        }
    }
   
}

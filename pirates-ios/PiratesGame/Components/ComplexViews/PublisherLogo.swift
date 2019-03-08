//
//  PublisherLogo.swift
//  Complex view: the publisher logo with effect animation
//  Note: requires clipping on parent for logo effect
//

import UIKit
import UniLayout
import ViewletCreator

class PublisherLogo: UniView {

    // --
    // MARK: Members
    // --
    
    private let baseLogo = UIImageView()
    private let logoEffect = UIImageView()
    private let logoFlashEffect = UIImageView()
    private let effectUpOffset: CGFloat = -36
    private let effectLeftOffset: CGFloat = -15
    private var currentOn = false

    
    // --
    // MARK: Viewlet integration
    // --
    
    class func viewlet() -> Viewlet {
        return ViewletClass()
    }
    
    private class ViewletClass: Viewlet {
        
        func create() -> UIView {
            return PublisherLogo()
        }
        
        func update(view: UIView, attributes: [String : Any], parent: UIView?, binder: ViewletBinder?) -> Bool {
            if let logo = view as? PublisherLogo {
                // Apply state
                logo.autoAnimation = ViewletConvUtil.asBool(value: attributes["autoAnimation"]) ?? false
                logo.on = ViewletConvUtil.asBool(value: attributes["on"]) ?? false

                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(view: view, attributes: attributes)
                return true
            }
            return false
        }
        
        func canRecycle(view: UIView, attributes: [String : Any]) -> Bool {
            return view is PublisherLogo
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
        // Add base logo
        baseLogo.image = UIImage(named: "logo_publisher")
        addSubview(baseLogo)
        
        // Add overlay effect
        logoEffect.alpha = 0
        addSubview(logoEffect)
        logoFlashEffect.alpha = 0
        addSubview(logoFlashEffect)
    }
    

    // --
    // MARK: Configurable values
    // --
    
    var autoAnimation = false

    var on: Bool {
        set {
            setOn(newValue, animated: autoAnimation)
        }
        get { return currentOn }
    }
    
    func setOn(_ on: Bool, animated: Bool, afterAssetLoad: (() -> Void)? = nil, completion: (() -> Void)? = nil) {
        // Safeguard against non-changing operations
        if on == currentOn {
            return
        }
        
        // Change state with optional animation
        if animated {
            ViewletUtil.waitViewLayout(view: self, completion: {
                if on {
                    if self.logoEffect.image == nil {
                        self.logoEffect.image = UIImage(named: "logo_publisher_effect")
                        self.logoFlashEffect.image = UIImage(named: "logo_publisher_effect")?.withRenderingMode(.alwaysTemplate)
                        self.logoFlashEffect.tintColor = UIColor.white
                        UniLayout.setNeedsLayout(view: self)
                        afterAssetLoad?()
                    }
                    ViewletUtil.waitViewLayout(view: self.logoEffect, completion: {
                        self.logoEffect.alpha = 1
                        self.logoFlashEffect.alpha = 0.8
                        UIView.animate(withDuration: 1, delay: 0.0, options: [.curveEaseOut], animations: {
                            self.logoEffect.alpha = 1
                            self.logoFlashEffect.alpha = 0
                        })
                        self.currentOn = on
                        completion?()
                    }, timeout: {
                        self.setOn(on, animated: false)
                    })
                } else {
                    self.logoEffect.alpha = 1
                    UIView.animate(withDuration: 1, delay: 0.0, options: [], animations: {
                        self.logoEffect.alpha = 0
                    })
                    self.currentOn = on
                    completion?()
                }
            }, timeout: {
                self.setOn(on, animated: false)
            })
        } else {
            currentOn = on
            if on && logoEffect.image == nil {
                logoEffect.image = UIImage(named: "logo_publisher_effect")
                afterAssetLoad?()
            }
            self.logoEffect.alpha = on ? 1 : 0
            completion?()
        }
    }


    // --
    // MARK: Custom layout
    // --

    override func measuredSize(sizeSpec: CGSize, widthSpec: UniMeasureSpec, heightSpec: UniMeasureSpec) -> CGSize {
        // Determine the aspect ratio
        var wantAspectRatio: CGFloat = 0
        if let baseLogoImage = baseLogo.image {
            wantAspectRatio = baseLogoImage.size.width / baseLogoImage.size.height
        }
        
        // Calculate the measured size
        if wantAspectRatio > 0 {
            // Determine if either width or height need to be scaled up or down
            var scaleWidth = false
            var scaleHeight = false
            if widthSpec != .exactSize || heightSpec != .exactSize {
                if widthSpec == .exactSize {
                    scaleHeight = true
                } else if heightSpec == .exactSize {
                    scaleWidth = true
                } else if let baseLogoImage = baseLogo.image {
                    var reducedWidth: CGFloat = 1
                    var reducedHeight: CGFloat = 1
                    if widthSpec == .limitSize {
                        reducedWidth = (sizeSpec.width - padding.left - padding.right) / baseLogoImage.size.width
                    }
                    if heightSpec == .limitSize {
                        reducedHeight = (sizeSpec.height - padding.top - padding.bottom) / baseLogoImage.size.height
                    }
                    if reducedWidth < reducedHeight && reducedWidth < 1 {
                        scaleHeight = true
                    } else if reducedHeight < 1 {
                        scaleWidth = true
                    }
                }
            }
            
            // Apply scaling
            if scaleWidth {
                var limitWidth = CGFloat(0xFFFFFFF)
                if widthSpec == .limitSize {
                    limitWidth = sizeSpec.width
                }
                return CGSize(width: min(limitWidth, (sizeSpec.height - padding.top - padding.bottom) * wantAspectRatio + padding.left + padding.right), height: sizeSpec.height)
            } else if scaleHeight {
                var limitHeight = CGFloat(0xFFFFFFF)
                if heightSpec == .limitSize {
                    limitHeight = sizeSpec.height
                }
                return CGSize(width: sizeSpec.width, height: min(limitHeight, (sizeSpec.width - padding.left - padding.right) / wantAspectRatio + padding.top + padding.bottom))
            }
            
            // If no scaling was applied set it to the size of the image
            if let baseLogoImage = baseLogo.image {
                return CGSize(width: baseLogoImage.size.width + padding.left + padding.right, height: baseLogoImage.size.height + padding.top + padding.bottom)
            }
        }
        
        // If nothing was applied, just use the sizes given in the spec
        var width: CGFloat = 0
        var height: CGFloat = 0
        if widthSpec == .exactSize {
            width = sizeSpec.width
        }
        if heightSpec == .exactSize {
            height = sizeSpec.height
        }
        return CGSize(width: width, height: height)
    }
    
    internal override func systemLayoutSizeFitting(_ targetSize: CGSize, withHorizontalFittingPriority horizontalFittingPriority: UILayoutPriority, verticalFittingPriority: UILayoutPriority) -> CGSize {
        return measuredSize(sizeSpec: targetSize, widthSpec: horizontalFittingPriority == UILayoutPriority.required ? UniMeasureSpec.limitSize : UniMeasureSpec.unspecified, heightSpec: verticalFittingPriority == UILayoutPriority.required ? UniMeasureSpec.limitSize : UniMeasureSpec.unspecified)
    }
    
    internal override func layoutSubviews() {
        // Layout on base logo
        var scaleFactor: CGFloat = 1
        var scaledLogoHeight: CGFloat = 0
        let centerY = padding.top + (frame.size.height - padding.top - padding.bottom) / 2
        if let baseLogoImage = baseLogo.image {
            scaleFactor = (frame.size.width - padding.left - padding.right) / baseLogoImage.size.width
            let scaledLogoWidth = frame.size.width - padding.left - padding.right
            scaledLogoHeight = baseLogoImage.size.height * scaleFactor
            UniLayout.setFrame(view: baseLogo, frame: CGRect(x: padding.left, y: centerY - scaledLogoHeight / 2, width: scaledLogoWidth, height: scaledLogoHeight))
        }
        
        // Layout on glow effect
        if let logoEffectImage = logoEffect.image {
            let scaledEffectWidth = logoEffectImage.size.width * scaleFactor
            let scaledEffectHeight = logoEffectImage.size.height * scaleFactor
            let scaledUpOffset = effectUpOffset * scaleFactor
            let scaledLeftOffset = effectLeftOffset * scaleFactor
            UniLayout.setFrame(view: logoEffect, frame: CGRect(x: padding.left + scaledLeftOffset, y: centerY - scaledLogoHeight / 2 + scaledUpOffset, width: scaledEffectWidth, height: scaledEffectHeight))
        }

        // Layout on flash effect
        if let logoFlashEffectImage = logoFlashEffect.image {
            let scaledEffectWidth = logoFlashEffectImage.size.width * scaleFactor
            let scaledEffectHeight = logoFlashEffectImage.size.height * scaleFactor
            let scaledUpOffset = effectUpOffset * scaleFactor
            let scaledLeftOffset = effectLeftOffset * scaleFactor
            UniLayout.setFrame(view: logoFlashEffect, frame: CGRect(x: padding.left + scaledLeftOffset, y: centerY - scaledLogoHeight / 2 + scaledUpOffset, width: scaledEffectWidth, height: scaledEffectHeight))
        }
    }
   
}

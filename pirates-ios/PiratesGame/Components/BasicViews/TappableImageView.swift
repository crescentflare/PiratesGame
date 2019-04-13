//
//  TappableImageView.swift
//  Basic view: an image view with tap interaction
//

import UIKit
import UniLayout
import JsonInflator

class TappableImageView: FrameContainerView {

    // --
    // MARK: Members
    // --
    
    private let normalImageView = UniImageView()
    private let highlightedImageView = UniImageView()
    
    
    // --
    // MARK: Viewlet integration
    // --
    
    override class func viewlet() -> JsonInflatable {
        return ViewletClass()
    }
    
    private class ViewletClass: JsonInflatable {
        
        func create() -> Any {
            return TappableImageView()
        }
        
        func update(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any], parent: Any?, binder: InflatorBinder?) -> Bool {
            if let imageView = object as? TappableImageView {
                // Apply image states
                imageView.image = ImageSource(value: attributes["image"])
                imageView.highlightedImage = ImageSource(value: attributes["highlightedImage"])
                
                // Apply separate colorization
                imageView.highlightedColor = convUtil.asColor(value: attributes["highlightedColor"])

                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(convUtil: convUtil, view: imageView, attributes: attributes)

                // Event handling
                imageView.tapEvent = AppEvent(value: attributes["tapEvent"])

                // Forward event observer
                if let eventObserver = parent as? AppEventObserver {
                    imageView.eventObserver = eventObserver
                }
                return true
            }
            return false
        }
        
        func canRecycle(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any]) -> Bool {
            return object is TappableImageView
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
        // Add views
        addSubview(normalImageView)
        addSubview(highlightedImageView)
        
        // Apply image settings
        for imageView in [normalImageView, highlightedImageView] {
            imageView.internalImageView.tintAdjustmentMode = .normal
            imageView.layoutProperties.horizontalGravity = 0.5
            imageView.layoutProperties.verticalGravity = 0.5
        }

        // Default state
        highlightedImageView.isHidden = true
    }
    

    // --
    // MARK: Configurable values
    // --
    
    var image: ImageSource? {
        didSet {
            ImageViewlet.applyImageSource(imageView: normalImageView, source: image)
        }
    }

    var highlightedImage: ImageSource? {
        didSet {
            ImageViewlet.applyImageSource(imageView: highlightedImageView, source: highlightedImage)
            updateState()
        }
    }
    
    var highlightedColor: UIColor? {
        didSet {
            highlightedImageView.internalImageView.tintColor = highlightedColor ?? highlightedImage?.tintColor
            updateState()
        }
    }


    // --
    // MARK: Interaction
    // --

    override var isHighlighted: Bool {
        didSet {
            updateState()
        }
    }
   

    // --
    // MARK: Helper
    // --
    
    private func updateState() {
        normalImageView.isHidden = isHighlighted && highlightedImage != nil
        highlightedImageView.isHidden = !isHighlighted || highlightedImage == nil
        normalImageView.internalImageView.tintColor = isHighlighted ? highlightedColor ?? image?.tintColor : image?.tintColor
    }

}

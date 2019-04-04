//
//  ImageViewlet.swift
//  Basic view viewlet: an image view
//

import UIKit
import UniLayout
import JsonInflator
import SDWebImage

enum ImageScaleType: String {
    
    case center = "center"
    case stretch = "stretch"
    case scaleFit = "scaleFit"
    case scaleCrop = "scaleCrop"
    
    func toContentMode() -> UIView.ContentMode {
        switch self {
        case .stretch:
            return .scaleToFill
        case .scaleFit:
            return .scaleAspectFit
        case .scaleCrop:
            return .scaleAspectFill
        case .center:
            return .center
        }
    }

}

class ImageViewlet {
    
    // --
    // MARK: Viewlet instance
    // --
    
    class func viewlet() -> JsonInflatable {
        return ViewletClass()
    }
    
    private class ViewletClass: JsonInflatable {
        
        func create() -> Any {
            return UniImageView()
        }
        
        func update(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any], parent: Any?, binder: InflatorBinder?) -> Bool {
            if let imageView = object as? UniImageView {
                // Image
                ImageViewlet.applyImageSource(imageView: imageView, source: ImageSource(value: attributes["source"]))
                
                // Scale factor
                let scaleType = ImageScaleType(rawValue: convUtil.asString(value: attributes["scaleType"]) ?? "") ?? .center
                imageView.internalImageView.contentMode = scaleType.toContentMode()
                
                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(convUtil: convUtil, view: imageView, attributes: attributes)
                return true
            }
            return false
        }
        
        func canRecycle(convUtil: InflatorConvUtil, object: Any, attributes: [String: Any]) -> Bool {
            return object is UniImageView
        }
        
    }
    
    
    // --
    // MARK: Helper
    // --
    
    @discardableResult
    static func applyImageSource(imageView: UniImageView?, source: ImageSource?) -> Bool {
        if let onlinePath = source?.onlinePath {
            if let imageUrl = URL(string: onlinePath) {
                imageView?.internalImageView.sd_setImage(with: imageUrl)
            }
        } else if var image = source?.getImage() {
            let tintColor = source?.tintColor
            if tintColor != nil {
                image = image.withRenderingMode(.alwaysTemplate)
            }
            if let threePatch = source?.threePatch {
                image = image.stretchableImage(withLeftCapWidth: Int(threePatch), topCapHeight: 0)
            } else if let ninePatch = source?.ninePatch {
                image = image.stretchableImage(withLeftCapWidth: Int(ninePatch), topCapHeight: Int(ninePatch))
            }
            imageView?.tintColor = tintColor ?? UIColor.clear
            imageView?.image = image
            return true
        }
        imageView?.image = nil
        return false
    }
    
}

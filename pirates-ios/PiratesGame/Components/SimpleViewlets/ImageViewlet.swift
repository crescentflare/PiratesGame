//
//  ImageViewlet.swift
//  Basic view viewlet: an image view
//

import UIKit
import UniLayout
import ViewletCreator
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
    
    class func viewlet() -> Viewlet {
        return ViewletClass()
    }
    
    private class ViewletClass: Viewlet {
        
        func create() -> UIView {
            return UniImageView()
        }
        
        func update(view: UIView, attributes: [String : Any], parent: UIView?, binder: ViewletBinder?) -> Bool {
            if let imageView = view as? UniImageView {
                // Image
                ImageViewlet.applyImageSource(imageView: imageView, source: ImageSource(value: attributes["source"]))
                
                // Scale factor
                let scaleType = ImageScaleType(rawValue: ViewletConvUtil.asString(value: attributes["scaleType"]) ?? "") ?? .center
                imageView.internalImageView.contentMode = scaleType.toContentMode()
                
                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(view: view, attributes: attributes)
                return true
            }
            return false
        }
        
        func canRecycle(view: UIView, attributes: [String : Any]) -> Bool {
            return view is UniImageView
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

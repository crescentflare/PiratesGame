//
//  ImageViewlet.swift
//  Basic view viewlet: an image view
//

import UIKit
import UniLayout
import JsonInflator
import AlamofireImage

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

fileprivate struct AlamofireViewletFilter: ImageFilter {

    private let scale: CGFloat
    private let withRenderingTemplate: Bool
    
    public init(scale: CGFloat = 1, withRenderingTemplate: Bool = false) {
        self.scale = scale
        self.withRenderingTemplate = withRenderingTemplate
    }
    
    public var filter: (Image) -> Image {
        return { image in
            let result = self.scale == 1 ? image : image.af_imageScaled(to: CGSize(width: image.size.width * self.scale, height: image.size.height * self.scale))
            if self.withRenderingTemplate {
                return result.withRenderingMode(.alwaysTemplate)
            }
            return result
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
                imageView.clipsToBounds = true
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
        return applyImageSource(imageView: imageView?.internalImageView, source: source)
    }
    
    @discardableResult
    static func applyImageSource(imageView: UIImageView?, source: ImageSource?) -> Bool {
        if let onlineUri = source?.onlineUri {
            if let imageUrl = URL(string: onlineUri) {
                let tintColor = source?.tintColor
                let filter = AlamofireViewletFilter(scale: source?.type == .devServerImage ? UIScreen.main.scale / 4 : 1, withRenderingTemplate: tintColor != nil)
                imageView?.af_setImage(withURL: imageUrl, filter: filter)
                imageView?.tintColor = tintColor ?? UIColor.clear
                return true
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

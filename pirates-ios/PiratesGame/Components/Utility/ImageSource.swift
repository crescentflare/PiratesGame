//
//  ImageSource.swift
//  Component utility: defines the source of an image (an internal or external image)
//

import UIKit
import UniLayout
import ViewletCreator

enum ImageSourceType: String {
    
    case unknown = "unknown"
    case onlineImage = "http"
    case secureOnlineImage = "https"
    case internalImage = "app"
    case systemImage = "system"
    case generate = "generate"
    
}

enum ImageSourceGenerateType: String {
    
    case unknown = "unknown"
    case filledRect = "filledRect"
    case filledOval = "filledOval"
    
}

class ImageSource {
    
    // --
    // MARK: Members
    // --

    var type = ImageSourceType.unknown
    var parameters = [String: Any]()
    var pathComponents = [String]()
    var otherSources = [ImageSource]()
    

    // --
    // MARK: Initialization
    // --
    
    convenience init?(value: Any?) {
        if let stringValue = value as? String {
            self.init(string: stringValue)
        } else if let dictValue = value as? [String: Any] {
            self.init(dict: dictValue)
        } else {
            return nil
        }
    }

    init?(string: String?) {
        if let string = string {
            // Extract scheme
            var checkString = string
            if let schemeMarker = checkString.range(of: "://") {
                type = ImageSourceType(rawValue: String(checkString[..<schemeMarker.lowerBound])) ?? ImageSourceType.unknown
                checkString = String(checkString[schemeMarker.upperBound...])
            }
            
            // Extract parameters
            if let paramMarker = checkString.range(of: "?") {
                // Get parameter string
                let paramString = String(checkString[paramMarker.upperBound...])
                checkString = String(checkString[..<paramMarker.lowerBound])
                
                // Split into separate parameters and fill dictionary
                let paramItems = paramString.split(separator: "&").map(String.init)
                for paramItem in paramItems {
                    let paramSet = paramItem.split(separator: "=").map(String.init)
                    if paramSet.count == 2 {
                        parameters[paramSet[0].urlDecode()] = paramSet[1].urlDecode()
                    }
                }
            }
            
            // Finally set path to the remaining string
            pathComponents = checkString.split(separator: "/").map(String.init)
        } else {
            return nil
        }
    }
    
    init(dict: [String: Any]) {
        type = ImageSourceType(rawValue: ViewletConvUtil.asString(value: dict["type"]) ?? "unknown") ?? .unknown
        if let path = ViewletConvUtil.asString(value: dict["path"]) ?? ViewletConvUtil.asString(value: dict["name"]) {
            pathComponents = path.split(separator: "/").map(String.init)
        }
        if let otherSources = dict["otherSources"] as? [Any] {
            for otherSource in otherSources {
                if let source = ImageSource(value: otherSource) {
                    self.otherSources.append(source)
                }
            }
        }
        for (key, value) in dict {
            if key != "type" && key != "path" && key != "name" && key != "otherSources" {
                parameters[key] = value
            }
        }
    }

    
    // --
    // MARK: Extract values
    // --
    
    var fullURI: String {
        get {
            var uri = "\(type)://\(fullPath)"
            if parameters.count > 0 {
                var firstParam = true
                for key in parameters.keys {
                    if let value = ViewletConvUtil.asString(value: parameters[key]) {
                        uri += firstParam ? "?" : "&"
                        uri += key.urlEncode() + "=" + value.urlEncode()
                        firstParam = false
                    }
                }
            }
            return uri
        }
    }

    var fullPath: String {
        get {
            return pathComponents.joined(separator: "/")
        }
    }
    
    var tintColor: UIColor? {
        get {
            if let colorizeString = parameters["colorize"] {
                return ViewletConvUtil.asColor(value: colorizeString)
            }
            return nil
        }
    }
    
    var threePatch: CGFloat {
        get {
            return ViewletConvUtil.asDimension(value: parameters["threePatch"]) ?? -1
        }
    }
    
    var ninePatch: CGFloat {
        get {
            return ViewletConvUtil.asDimension(value: parameters["ninePatch"]) ?? -1
        }
    }
    
    var onlinePath: String? {
        get {
            return type == .onlineImage || type == .secureOnlineImage ? fullURI : nil
        }
    }

    
    // --
    // MARK: Helper
    // --

    func getImage() -> UIImage? {
        var result: UIImage?
        if type == .internalImage {
            let bundle = Bundle(for: ImageSource.self)
            let path = fullPath
            if path.hasPrefix("unilayout-") {
                result = UniAssets.obtainAsset(name: path.replacingOccurrences(of: "unilayout-", with: ""))
            } else {
                result = UIImage(named: path, in: bundle, compatibleWith: nil)
            }
        } else if type == .systemImage {
            switch fullPath {
            case "spinner":
                let bundle = Bundle(for: ImageSource.self)
                result = UIImage(named: "icon_spinner", in: bundle, compatibleWith: nil)
            default:
                break
            }
        } else if type == .generate {
            result = getGeneratedImage()
        }
        for otherSource in otherSources {
            if let generatedImage = otherSource.getGeneratedImage(onImage: result) {
                result = generatedImage
            }
        }
        return result
    }
    
    private func getGeneratedImage(onImage: UIImage? = nil) -> UIImage? {
        if let generateType = ImageSourceGenerateType(rawValue: fullPath) {
            switch generateType {
            case .filledRect:
                return FilledRectGenerator().generate(attributes: parameters, onImage: onImage)
            case .filledOval:
                return FilledOvalGenerator().generate(attributes: parameters, onImage: onImage)
            default:
                break
            }
        }
        return nil
    }
    
}

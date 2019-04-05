//
//  ImageSource.swift
//  Component utility: defines the source of an image (an internal or external image)
//

import UIKit
import UniLayout

enum ImageSourceType: String {
    
    case unknown = "unknown"
    case onlineImage = "http"
    case secureOnlineImage = "https"
    case internalImage = "app"
    case systemImage = "system"
    case generate = "generate"
    
}

enum ImageSourceCaching: String {
    
    case unknown = "unknown"
    case always = "always"
    case never = "never"
    
}

enum ImageSourceGenerateType: String {
    
    case unknown = "unknown"
    case filledRect = "filledRect"
    case filledOval = "filledOval"
    case strokedPath = "strokedPath"
    
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
            
            // Convert other sources parameter to objects
            if let otherSourcesStringList = parameters["otherSources"] as? String {
                parameters.removeValue(forKey: "otherSources")
                let otherSourcesStringArray = otherSourcesStringList.split(separator: ",").map(String.init)
                for otherSourcesString in otherSourcesStringArray {
                    if let otherSource = ImageSource(string: otherSourcesString) {
                        otherSources.append(otherSource)
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
        type = ImageSourceType(rawValue: Inflators.viewlet.convUtil.asString(value: dict["type"]) ?? "unknown") ?? .unknown
        if let path = Inflators.viewlet.convUtil.asString(value: dict["path"]) ?? Inflators.viewlet.convUtil.asString(value: dict["name"]) {
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
    
    var fullPath: String {
        get {
            return pathComponents.joined(separator: "/")
        }
    }
    
    var onlineUri: String? {
        get {
            if type == .onlineImage || type == .secureOnlineImage {
                var uri = "\(type)://\(fullPath)"
                if let paramString = getParameterString(ignoreParams: ["caching", "threePatch", "ninePatch"], ignoreOtherSources: true) {
                    uri += "?" + paramString
                }
                return uri
            }
            return nil
        }
    }

    var tintColor: UIColor? {
        get {
            if let colorizeString = parameters["colorize"] {
                return Inflators.viewlet.convUtil.asColor(value: colorizeString)
            }
            return nil
        }
    }
    
    var threePatch: CGFloat? {
        get {
            return Inflators.viewlet.convUtil.asDimension(value: parameters["threePatch"])
        }
    }
    
    var ninePatch: CGFloat? {
        get {
            return Inflators.viewlet.convUtil.asDimension(value: parameters["ninePatch"])
        }
    }
    

    // --
    // MARK: Caching
    // --

    var caching: ImageSourceCaching {
        get {
            return ImageSourceCaching(rawValue: Inflators.viewlet.convUtil.asString(value: parameters["caching"]) ?? "") ?? .unknown
        }
    }
    
    var cacheKey: String {
        get {
            var uri = "\(type)://\(fullPath)"
            if let paramString = getParameterString(ignoreParams: ["caching", "threePatch", "ninePatch"]) {
                uri += "?" + paramString
            }
            return uri
        }
    }
    

    // --
    // MARK: Conversion
    // --

    var uri: String {
        get {
            var uri = "\(type)://\(fullPath)"
            if let paramString = getParameterString() {
                uri += "?" + paramString
            }
            return uri
        }
    }
    
    var dictionary: [String: Any] {
        get {
            var dictionary: [String: Any] = ["type": type.rawValue, "path": fullPath]
            for (key, value) in parameters {
                dictionary[key] = value
            }
            if otherSources.count > 0 {
                dictionary["otherSources"] = otherSources.map { $0.dictionary }
            }
            return dictionary
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
            case .strokedPath:
                return StrokedPathGenerator().generate(attributes: parameters, onImage: onImage)
            default:
                break
            }
        }
        return nil
    }
    
    private func getParameterString(ignoreParams: [String] = [], ignoreOtherSources: Bool = false) -> String? {
        if parameters.count > 0 {
            var parameterString = ""
            for key in parameters.keys {
                var ignore = false
                for ignoreParam in ignoreParams {
                    if key == ignoreParam {
                        ignore = true
                        break
                    }
                }
                if !ignore, let value = Inflators.viewlet.convUtil.asString(value: parameters[key]) {
                    if parameterString.count > 0 {
                        parameterString += "&"
                    }
                    parameterString += key.urlEncode() + "=" + value.urlEncode()
                }
            }
            if otherSources.count > 0 && !ignoreOtherSources {
                var otherSourceString = ""
                for otherSource in otherSources {
                    let otherSourceURI = otherSource.uri
                    if otherSourceString.count > 0 {
                        otherSourceString += ","
                    }
                    otherSourceString += otherSourceURI.urlEncode()
                }
                if parameterString.count > 0 {
                    parameterString += "&"
                }
                parameterString += "otherSources=" + otherSourceString
            }
            return parameterString
        }
        return nil
    }
    
}

//
//  AppEvent.swift
//  Event system: defines an event with optional parameters
//

class AppEvent {
    
    // --
    // MARK: Members
    // --

    var standardType = AppEventType.unknown
    var rawType = "unknown"
    var parameters = [String: String]()
    var pathComponents = [String]()
    

    // --
    // MARK: Initialization
    // --
    
    convenience init?(value: Any?) {
        if let stringValue = value as? String {
            self.init(string: stringValue)
        } else if let dictValue = value as? [String: String] {
            self.init(dict: dictValue)
        } else {
            return nil
        }
    }

    init(string: String) {
        // Extract type from scheme
        var checkString = string
        if let schemeMarker = checkString.range(of: "://") {
            rawType = String(checkString[..<schemeMarker.lowerBound])
            standardType = AppEventType(rawValue: rawType) ?? .unknown
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
    }
    
    init(dict: [String: String]) {
        rawType = dict["type"] ?? "unknown"
        standardType = AppEventType(rawValue: rawType) ?? .unknown
        if let path = dict["path"] {
            pathComponents = path.split(separator: "/").map(String.init)
        }
        for (key, value) in dict {
            if key != "type" && key != "path" {
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

}

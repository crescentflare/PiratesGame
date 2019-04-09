//
//  Page.swift
//  Page storage: a single page item
//

import Foundation

class Page {
    
    // --
    // MARK: Members
    // --

    var loadedData = [String: Any]()
    let hash: String
    

    // --
    // MARK: Initialization
    // --
    
    init(jsonString: String) {
        var resultHash = "unknown"
        loadedData = [:]
        if let jsonData = jsonString.data(using: .utf8) {
            if let json = try? JSONSerialization.jsonObject(with: jsonData, options: .allowFragments) {
                if let parsedData = json as? [String: Any] {
                    loadedData = parsedData
                    resultHash = jsonString.md5()
                }
            }
        }
        hash = resultHash
    }
    
    init(jsonData: Data) {
        var resultHash = "unknown"
        loadedData = [:]
        if let json = try? JSONSerialization.jsonObject(with: jsonData, options: .allowFragments) {
            if let parsedData = json as? [String: Any] {
                loadedData = parsedData
                resultHash = jsonData.md5()
            }
        }
        hash = resultHash
    }

    init(dictionary: [String: Any], hash: String = "unknown") {
        loadedData = dictionary
        self.hash = hash
    }


    // --
    // MARK: Extract data
    // --
    
    var modules: [[String: Any]]? {
        get {
            return loadedData["modules"] as? [[String: Any]]
        }
    }

    var layout: [String: Any]? {
        get {
            if let dataSets = loadedData["dataSets"] as? [String: Any] {
                return dataSets["layout"] as? [String: Any]
            }
            return nil
        }
    }

}

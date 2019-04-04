//
//  JsonInflator.swift
//  Json inflator Pod
//
//  Library: json inflator
//  The main interface to register and inflate new objects
//

fileprivate class JsonInflatableItem {

    var inflatable: JsonInflatable?
    var attributeSets = [String: [String: Any]]()

}

open class JsonInflator {
    
    // --
    // MARK: Singleton instance
    // --

    static let shared = JsonInflator(inflatableKey: "inflator", attributeSetKey: "attributeSet")
    
    
    // --
    // MARK: Members
    // --

    public var convUtil = InflatorConvUtil()
    private let inflatableKey: String
    private let attributeSetKey: String
    private var registeredInflators = [String: JsonInflatableItem]()
    private var mergeSubAttributes = [String]()
    private var excludeAttributes = [String]()

    public var colorLookup: InflatorColorLookup? {
        didSet {
            convUtil = InflatorConvUtil(colorLookup: colorLookup, dimensionLookup: dimensionLookup)
        }
    }
    
    public var dimensionLookup: InflatorDimensionLookup? {
        didSet {
            convUtil = InflatorConvUtil(colorLookup: colorLookup, dimensionLookup: dimensionLookup)
        }
    }
    

    // --
    // MARK: Initialization
    // --
    
    public init(inflatableKey: String, attributeSetKey: String = "attributeSet") {
        self.inflatableKey = inflatableKey
        self.attributeSetKey = attributeSetKey
    }


    // --
    // MARK: Attribute inclusion/exclusion
    // --
    
    public func setMergeSubAttributes(_ attributeNames: [String]) {
        mergeSubAttributes = attributeNames
    }
    
    public func setExcludeAttributes(_ attributeNames: [String]) {
        excludeAttributes = attributeNames
    }
    
    
    // --
    // MARK: Inflatable registry
    // --
    
    public func register(name: String, inflatable: JsonInflatable) {
        obtainInflator(name).inflatable = inflatable
    }
    
    public func registerAttributeSet(inflatableName: String, setName: String, setAttributes: [String: Any]?) {
        let inflator = obtainInflator(inflatableName)
        if let setAttributes = setAttributes {
            inflator.attributeSets[setName] = setAttributes
        } else {
            let _ = inflator.attributeSets.removeValue(forKey: setName)
        }
    }
    
    public func registeredInflatableNames() -> [String] {
        return registeredInflators.keys.map { $0 }
    }
    
    private func obtainInflator(_ name: String) -> JsonInflatableItem {
        if let inflator = registeredInflators[name] {
            return inflator
        }
        let addItem = JsonInflatableItem()
        registeredInflators[name] = addItem
        return addItem
    }


    // --
    // MARK: Create and update
    // --

    public func inflate(attributes: [String: Any], parent: Any? = nil, binder: InflatorBinder? = nil) -> Any? {
        if let inflatable = findInflatableInAttributes(attributes), let inflatableName = findInflatableNameInAttributes(attributes) {
            let object = inflatable.create()
            if let mergedAttributes = processedAttributes(given: attributes, fallback: attributesForSet(inflatableName: inflatableName, setName: attributes[attributeSetKey] as? String), mergeSubAttributes: mergeSubAttributes, excludeAttributes: excludeAttributes) {
                inflatable.update(convUtil: convUtil, object: object, attributes: mergedAttributes, parent: parent, binder: binder)
            }
            return object
        }
        return nil
    }
    
    @discardableResult
    public func inflate(onObject: Any, attributes: [String: Any]?, parent: Any? = nil, binder: InflatorBinder? = nil) -> Bool {
        if attributes == nil {
            return false
        }
        if let inflatable = findInflatableInAttributes(attributes ?? [:]), let inflatableName = findInflatableNameInAttributes(attributes ?? [:]) {
            if let mergedAttributes = processedAttributes(given: attributes, fallback: attributesForSet(inflatableName: inflatableName, setName: attributes?[attributeSetKey] as? String), mergeSubAttributes: mergeSubAttributes, excludeAttributes: excludeAttributes) {
                return inflatable.update(convUtil: convUtil, object: onObject, attributes: mergedAttributes, parent: parent, binder: binder)
            }
        }
        return false
    }
    
    public func canRecycle(object: Any?, attributes: [String: Any]?) -> Bool {
        if let object = object {
            if let inflatable = findInflatableInAttributes(attributes ?? [:]) {
                return inflatable.canRecycle(convUtil: convUtil, object: object, attributes: attributes ?? [:])
            }
        }
        return false
    }
    
    public func findInflatableInAttributes(_ attributes: [String: Any]) -> JsonInflatable? {
        if let inflatorName = attributes[inflatableKey] as? String {
            return registeredInflators[inflatorName]?.inflatable
        }
        return nil
    }
    
    public func findInflatableNameInAttributes(_ attributes: [String: Any]) -> String? {
        return attributes[inflatableKey] as? String
    }
    
    
    // --
    // MARK: Nested inflation utilities
    // --
    
    public func attributesForNestedInflatable(_ nestedInflatableItem: Any?) -> [String: Any]? {
        if let attributes = nestedInflatableItem as? [String: Any] {
            if let inflatableName = findInflatableNameInAttributes(attributes) {
                return processedAttributes(given: attributes, fallback: attributesForSet(inflatableName: inflatableName, setName: attributes[attributeSetKey] as? String), mergeSubAttributes: mergeSubAttributes, excludeAttributes: excludeAttributes)
            }
        }
        return nil
    }

    public func attributesForNestedInflatableList(_ nestedInflatableItemList: Any?) -> [[String: Any]] {
        var inflatableItemList: [[String: Any]] = []
        if let itemList = nestedInflatableItemList as? [[String: Any]] {
            for item in itemList {
                if let inflatableName = findInflatableNameInAttributes(item) {
                    if let resultAttributes = processedAttributes(given: item, fallback: attributesForSet(inflatableName: inflatableName, setName: item[attributeSetKey] as? String), mergeSubAttributes: mergeSubAttributes, excludeAttributes: excludeAttributes) {
                        inflatableItemList.append(resultAttributes)
                    }
                }
            }
        }
        return inflatableItemList
    }


    // --
    // MARK: Attribute processing
    // --
    
    private func attributesForSet(inflatableName: String, setName: String?) -> [String: Any]? {
        if let inflator = registeredInflators[inflatableName] {
            if setName == "default" {
                return inflator.attributeSets["default"]
            }
            return mergedAttributes(given: inflator.attributeSets[setName ?? ""], fallback: inflator.attributeSets["default"])
        }
        return nil
    }
    
    private func processedAttributes(given: [String: Any]?, fallback: [String: Any]?, mergeSubAttributes: [String], excludeAttributes: [String]) -> [String: Any]? {
        if var result = mergedAttributes(given: given, fallback: fallback) {
            for mergeSubAttribute in mergeSubAttributes {
                if let item = result[mergeSubAttribute] as? [String: Any] {
                    result = mergedAttributes(given: item, fallback: result) ?? result
                }
            }
            for excludeAttribute in excludeAttributes {
                result.removeValue(forKey: excludeAttribute)
            }
            return result
        }
        return nil
    }
    
    private func mergedAttributes(given: [String: Any]?, fallback: [String: Any]?) -> [String: Any]? {
        // Just return one of the attributes if the other is null
        if fallback == nil {
            return given
        } else if given == nil {
            return fallback
        }
        
        // Merge and return without modifying the originals
        var merged: [String: Any] = [:]
        for (key, value) in given! {
            merged[key] = value
        }
        for (key, value) in fallback! {
            if merged[key] == nil {
                merged[key] = value
            }
        }
        return merged
    }

}

//
//  String+Localization.swift
//  Core extension: easily localize strings with reference forwarding
//  Note: use reference forwarding to point one localized string key to another in the localizable strings file, use this format: REF/KEY_STRING
//

import Foundation

extension String {
    
    func localized() -> String {
        let key = self.uppercased()
        let value = NSLocalizedString(key, comment: "")
        if value != key {
            if value.hasPrefix("REF/") {
                return String(value[value.index(value.startIndex, offsetBy: 4)..<value.endIndex]).localized()
            }
            return value
        }
        return self
    }

}

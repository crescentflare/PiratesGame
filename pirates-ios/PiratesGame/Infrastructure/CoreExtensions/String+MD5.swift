//
//  String+MD5.swift
//  Core extension: extends string to calculate MD5
//

extension String {
    
    func md5() -> String {
        if let messageData = self.data(using: .utf8) {
            return messageData.md5()
        }
        return "unavailable"
    }
    
}

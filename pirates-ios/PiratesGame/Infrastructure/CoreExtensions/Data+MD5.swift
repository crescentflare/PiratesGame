//
//  Data+MD5.swift
//  Core extension: extends data to calculcate MD5
//

import Foundation
import CommonCrypto

extension Data {
    
    func md5() -> String {
        // Setup data variable to hold the MD5 hash
        var digest = Data(count: Int(CC_MD5_DIGEST_LENGTH))
        
        // Generate hash
        _ = digest.withUnsafeMutableBytes { (digestBytes: UnsafeMutablePointer<UInt8>) in
            self.withUnsafeBytes { (messageBytes: UnsafePointer<UInt8>) in
                let length = CC_LONG(self.count)
                CC_MD5(messageBytes, length, digestBytes)
            }
        }
        
        // Return MD5 hash string formatted as hexadecimal
        return digest.map { String(format: "%02hhx", $0) }.joined()
    }
    
}

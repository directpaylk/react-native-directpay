//
//  DPDigest.swift
//  MGPSDK
//
//  Created by Deeptha Senanayake on 3/22/19.
//

import Foundation

class DPDigest {
    
    static func encode(_ dataString:String?) -> String {
        print("[DPSDK] - ENCODE", dataString ?? "[EMPTY_DATA]")
        
        let appKey = (Constants.SDK.MERCHANT_ID + Constants.SDK.API_KEY).base64Encoded()
        
        print("[DIGEST] - APP_KEY: ", appKey!)
        
        let rawData:String = (dataString ?? "") + Constants.SDK.MERCHANT_ID + Constants.SDK.API_KEY + appKey!
        
        let digest = SHA256(rawData).digestString()
        
        print("[DIGEST] - FORMAT:", rawData)
        
        return digest
    }
}

extension String {
    //: ### Base64 encoding a string
    func base64Encoded() -> String? {
        if let data = self.data(using: .utf8) {
            return data.base64EncodedString()
        }
        return nil
    }
    
    //: ### Base64 decoding a string
    func base64Decoded() -> String? {
        if let data = Data(base64Encoded: self) {
            return String(data: data, encoding: .utf8)
        }
        return nil
    }
}

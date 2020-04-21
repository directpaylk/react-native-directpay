//
//  RNDirectPayCardPayment.swift
//  RNDirectPayCardPayment
//
//  Created by Asela on 4/20/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation

@available(iOS 11.0, *)
@objc(RNDirectPayCardPayment)
class RNDirectPayCardPayment: NSObject {
    
    @objc
    static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    private var count = 0
    @objc
    func addCardToUser(_ callback: @escaping RCTResponseSenderBlock) {
        count += 1
        print("count is \(count)")
        
        
        let directpay:DPSDK = DPSDK(apiKey: "231e23e23e2e3", dpMerchantId: "TP00001", currency:DPSDK.CURRENCY.LKR, uniqueUserId: "USER_ID", userName: "USER NAME", mobile: "+94731234567", email: "emailifavailable@user.com")
        DispatchQueue.main.async{
            let viewController =  UIApplication.shared.keyWindow!.rootViewController
            
            directpay.addCard(viewController!, success: { (card) in
                let cardObj: Card = card as! Card
                let resultsDict = [
                    "id":cardObj.id,
                    "mask":cardObj.mask,
                    "reference":cardObj.reference,
                    "brand":cardObj.brand
                    
                    ] as [String : Any];
                
                print("[NEW CARD ADDED] SUCCESS - CARD_DETAILS: ", card)
                callback([NSNull(),resultsDict])
            }, error: {(code:String, message:String) in
                print("[NEW CARD ADDED] ERROR: CODE - ",code, "MESSAGE - ", message)
                
                let errorDict = [
                    "code":code,
                    "message":message
                    
                    ] as [String : Any];
                callback([errorDict,NSNull()])
            })
        }
    }
}

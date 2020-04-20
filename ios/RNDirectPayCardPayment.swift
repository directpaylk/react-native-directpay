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
    func increment() {
        count += 1
        print("count is \(count)")
        
        let directpay:DPSDK = DPSDK(apiKey: "API_KEY", dpMerchantId: "DP00001", currency:DPSDK.CURRENCY.LKR, uniqueUserId: "USER_ID", userName: "USER NAME", mobile: "+94731234567", email: "emailifavailable@user.com")
        
        let viewController =  UIApplication.shared.keyWindow!.rootViewController as! UIViewController
        
        directpay.addCard(viewController, success: { (card) in
            print("[NEW CARD ADDED] SUCCESS - CARD_DETAILS: ", card)
        }, error: {(code:String, message:String) in
            print("[NEW CARD ADDED] ERROR: CODE - ",code, "MESSAGE - ", message)
        })
    }
}

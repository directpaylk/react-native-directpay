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
    

    @objc
    func addCardToUser(_ env : String, apiKey : String, mid : String, uid : String, firstName : String, lastName :String, email : String, phoneNumber : String,  callback: @escaping RCTResponseSenderBlock) {
     
        if(env == "dev"){
            Constants.DEBUG = true
            Constants.API = Constants.DEV_URL
            Constants.ENV = Constants.ENVIRONMENT.DEV
        }
        else{
            Constants.DEBUG = false
            Constants.API = Constants.PROD_URL
            Constants.ENV = Constants.ENVIRONMENT.PROD
        }
        
        
        let directpay:DPSDK = DPSDK(apiKey: apiKey, dpMerchantId: mid, currency:DPSDK.CURRENCY.LKR, uniqueUserId: uid, userName: firstName + " " + lastName, mobile:phoneNumber, email: email)
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

//
//  DirectPay.swift
//  DPSDK-iOS
//
//  Created by Deeptha Senanayake on 3/13/19.
//  Copyright © 2019 {ORGANIZATION}. All rights reserved.
//

import Foundation
import UIKit

open class DPSDK {
    
    let http:HttpController = HttpController()
    
    
    public struct PAYMENT_METHOD {
        private init(){}
        
        public static let ONE_TIME = "ONE_TIME_PAYMENT"
        public static let RECURRING = "RECURRING"
    }
    
    public struct CURRENCY {
        private init(){}
        
        public static let LKR = "LKR"
        public static let USD = "USD"
    }
    
    public struct INTERVAL {
        private init(){}
        
        public static let DAILY = "DAILY"
        public static let WEEKLY = "WEEKLY"
        public static let MONTHLY = "MONTHLY"
        public static let QUARTERLY = "QUARTERLY"
        public static let YEARLY = "YEARLY"
    }
    
    let dpMerchantId:String
    let currency:String
    
    public init(apiKey:String, dpMerchantId:String, currency:String, uniqueUserId:String, userName:String, mobile:String, email:String?) {
        print("[DPSDK] - MERCHANT: ",dpMerchantId)
        
        self.dpMerchantId = dpMerchantId
        self.currency = currency
        
        Constants.SDK.API_KEY = apiKey
        Constants.SDK.MERCHANT_ID = dpMerchantId
        
        ThirdPartyUser.uniqueUserId = uniqueUserId
        ThirdPartyUser.userName = userName
        ThirdPartyUser.mobile = mobile
        ThirdPartyUser.email = email
    }
    
    open func addCard(_ viewController: UIViewController, success: @escaping (_ card:Any) ->(), error: @escaping (_ code:String, _ message:String) -> ()) {
        let cardModal = DPAddCardView(frame: viewController.view.bounds)
        cardModal.setup(success, error)
        viewController.view.addSubview(cardModal)
    }
    
    
}

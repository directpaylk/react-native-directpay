//
//  RNDirectPayCardPayment.swift
//  RNDirectPayCardPayment
//
//  Created by Asela on 4/20/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation

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
    }
}

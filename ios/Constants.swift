//
//  Constants.swift
//  MGPSDK
//
//  Created by Deeptha Senanayake on 3/15/19.
//

import Foundation

internal class Constants{
    private static let API = "https://dev.directpay.lk/v1/mpg/api/"
    
    struct ROUTES {
        static let RETRIEVE_SESSION = Constants.API + "sdk/session"
        static let CARD_ADD = Constants.API + "sdk/cardAdd"
        static let CARD_LIST = Constants.API + "sdk/cardList"
        static let CARD_DELETE = Constants.API + "sdk/cardDelete"
        static let CARD_PAY = Constants.API + "sdk/cardPay"
    }
    
    struct SDK {
        static var API_KEY = ""
        static var MERCHANT_ID = ""
        static let CONTENT_TYPE = "application/json"
    }
    
    struct KEYS {
        static let REFERENCE = "reference"
        static let SERVICE_UNAVAILABLE = "serviceUnavailble"
        static let CARD_ID = "cardId"
        static let AMOUNT = "amount"
        static let CURRENCY = "currency"
        static let EMAIL = "email"
        static let PAYMENT_TYPE = "type"
        static let INTERVAL = "interval"
        static let START_PAYMENT_DATE = "startPaymentDate"
        static let RETRY_ATTEMPTS = "retryAttempts"
        static let IS_RETRY = "isRetry"
        static let DO_FIRST_PAYMENT = "doFirstPayment"
        static let LAST_PAYMENT_DATE = "lastPaymentDate"
        static let TRANSACTION_ID = "transactionId"
        static let DATE_TIME = "dateTime"
        static let DESCRIPTION = "description"
        static let CARD = "card"
        static let NUMBER = "number"
        static let STATUS = "status"
        static let SCHEDULED_ID = "scheduledId"
    }
    
    struct ERROR {
        struct DIGEST_NOT_MATCHED {
            static let CODE = "567"
            static let MESSAGE = "CANNOT_VERIFY_THE_REQUEST"
        }
        struct CANNOT_PROCESS {
            static let CODE = "568"
            static let MESSAGE = "CANNOT_PROCESS"
        }
        struct DIGEST_NOT_FOUND {
            static let CODE = "569"
            static let MESSAGE = "INVALID_SERVER_RESPONSE"
        }
        struct SERVICE_UNAVAILABLE {
            static let CODE = "570"
            static let MESSAGE = "SERVICE_UNAVAILABLE"
        }
        struct INTERNAL_ERROR {
            static let CODE = "571"
            static let MESSAGE = "INTERNAL_ERROR"
        }
        struct USER_ACTION_CLOSE {
            static let CODE = "600"
            static let MESSAGE = "USER_ACTION_CLOSE"
        }
        struct TOO_LARGE_RETRY_ATTEMPTS {
            static let CODE = "601"
            static let MESSAGE = "TOO_LARGE_RETRY_ATTEMPTS"
        }
    }
}

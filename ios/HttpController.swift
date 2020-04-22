//
//  HttpController.swift
//  MGPSDK
//
//  Created by Deeptha Senanayake on 3/15/19.
//

import Foundation

internal class HttpController {
    static let contentType = "Content-Type"
    static let authorization = "Authorization"
    static let directpayMerchant = "Directpay-Merchant"
    static let xApiKey = "x-api-key"
    static let digest = "Digest"
    
    func post(url:String,parameters:[String:Any]?,completion:  @escaping (_ result: [String :Any],_ response:URLResponse)->(), handleError: @escaping (_ error: Error)->()) {
        let url = URL(string: url)
        
        var request:URLRequest = URLRequest(url: url!)
        
        request.httpMethod = HTTPMethod.post.rawValue
        
        var rawData:String?
        
        if(parameters != nil){
            do{
                let jsonData = try JSONSerialization.data(withJSONObject: parameters!, options: [])
                let jsonString = String(data: jsonData, encoding: String.Encoding.ascii)!
                if(Constants.DEBUG == true){print ("[DPSK] - HTTP_REQUEST: ", jsonString)}
                rawData = jsonString
                request.httpBody = jsonData
            }catch{}
        }
        
        let digest = DPDigest.encode(rawData ?? nil)
        
        if(Constants.DEBUG == true){print("[DPSDK] - DIGEST: \(digest)")}
        
        //COMPULSORY HEADER
        request.addValue(Constants.SDK.CONTENT_TYPE, forHTTPHeaderField: HttpController.contentType)
        request.addValue(Constants.SDK.API_KEY, forHTTPHeaderField: HttpController.xApiKey)
        request.addValue(Constants.SDK.MERCHANT_ID, forHTTPHeaderField: HttpController.directpayMerchant)
        request.addValue(digest, forHTTPHeaderField: HttpController.digest)
        
        if(Constants.DEBUG == true){
            print("[DPSDK] REQUEST: ", request)
        }
        let task = URLSession.shared.dataTask(with: request) { (data, response, error) in
            if let data = data {
                do {
                    if(response != nil && Constants.DEBUG == true){
                        print("[DPSDK] HTTP_RESPONSE: ", response!)
                    }
                    let jsonSerialized = try JSONSerialization.jsonObject(with: data, options: []) as? [String : Any]
                    if jsonSerialized != nil{
                        completion(jsonSerialized!, response!)
                    }else{
                        completion([:], response!)
                    }
                }  catch let error as NSError {
                    if(Constants.DEBUG == true){print(error.localizedDescription)}
                    handleError(error)
                }
            } else if let error = error {
                if(Constants.DEBUG == true){print(error.localizedDescription)}
                handleError(error)
            }
        }
        
        task.resume()
    }
    
    func post(proccessed:Bool,url:String,parameters:[String:Any]?, success:  @escaping (_ data: NSDictionary)->(), handleError: @escaping (_ code:String,_ message:String)->()){
        self.post(url: url, parameters: parameters, completion:{(responseJson:[String:Any], response:URLResponse) in
            if let status:Int = responseJson["status"] as? Int {
                let data:NSDictionary = responseJson["data"] as! NSDictionary
                if(status == 200){
                    if(Constants.DEBUG == true){print("[DPSDK] STATUS : 200")}
                    if let httpResponse = response as? HTTPURLResponse {
                        if let serverDigest = httpResponse.allHeaderFields["Digest"] as? String {
                            if(Constants.DEBUG == true){print("[DATA JSON] : \(data)")}
                            do{
                                var jsonData:Data? = nil
                                if #available(iOS 11.0, *) {
                                    jsonData = try JSONSerialization.data(withJSONObject: data, options: .sortedKeys)
                                } else {
                                    success(data)
                                    return
                                }
                                
                                let digest = DPDigest.encode(String(data: jsonData!, encoding: .utf8))
                                if(Constants.DEBUG == true){print("[DIGEST] - FROM RESPONSE: ", serverDigest, "LOCAL_DIGEST: \(digest)")}
                                
                                if(digest == serverDigest){
                                    if(Constants.DEBUG == true){print("[_DIGEST] MATCHED")}
                                    success(data)
                                }else{
                                    if(Constants.DEBUG == true){print("[_DIGEST] NOT MATCHED")}
                                    handleError(aConstants.ERROR.DIGEST_NOT_MATCHED.CODE, Constants.ERROR.DIGEST_NOT_MATCHED.MESSAGE)
                                }
                            }catch {
                                handleError(Constants.ERROR.CANNOT_PROCESS.CODE, Constants.ERROR.CANNOT_PROCESS.MESSAGE)
                            }
                        }else{
                            if(url.split(separator: "/").last == "check3ds"){
                                success(data)
                                return
                            }
                            handleError(Constants.ERROR.DIGEST_NOT_FOUND.CODE, Constants.ERROR.DIGEST_NOT_FOUND.MESSAGE)
                        }
                    }
                }else if(status == 400){
                    let message = data.value(forKey: "message") as! String
                    let errorCode = (data.value(forKey: "error") ?? data.value(forKey: "code")) as! String
                    
                    handleError(errorCode, message)
                }else{
                    handleError(Constants.ERROR.INTERNAL_ERROR.CODE, Constants.ERROR.INTERNAL_ERROR.MESSAGE)
                }
            }
        }, handleError: { (nserror:Error) in
            if(Constants.DEBUG == true){print("[DPSDK] - ERROR:", nserror.localizedDescription)}
            handleError(Constants.ERROR.SERVICE_UNAVAILABLE.CODE, Constants.ERROR.SERVICE_UNAVAILABLE.MESSAGE)
        })
    }
}

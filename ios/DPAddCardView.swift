//
//  DPAddCardView.swift
//  MGPSDK
//
//  Created by Deeptha Senanayake on 3/14/19.
//

import UIKit

internal class DPAddCardView: UIView, UITextFieldDelegate {
    let httpController:HttpController = HttpController()
    
    @IBOutlet private weak var textCardholderName: UITextField!
    @IBOutlet private weak var textCardNumber: UITextField!
    @IBOutlet private weak var textMonth: UITextField!
    @IBOutlet private weak var textYear: UITextField!
    @IBOutlet private weak var textCVV: UITextField!
    @IBOutlet private weak var buttonClose: UIButton!
    @IBOutlet private weak var buttonAddCard: UIButton!
    @IBOutlet weak var labelWarnings: UILabel!
    /*
     // Only override draw() if you perform custom drawing.
     // An empty implementation adversely affects performance during animation.
     override func draw(_ rect: CGRect) {
     // Drawing code
     }
     */
    
    let nibName = "DPAddCardView"
    var contentView: UIView!
    
    var gatewayId:String?
    var check3ds:Bool?
    
    internal override init(frame: CGRect) {
        super.init(frame: frame)
        self.setupView()
    }
    
    var successCallback: (_ card:Card) -> () = {_ in }
    var errorCallback: (_ code:String, _ message:String) -> () = {(_,_) in }
    var viewController: UIViewController?
    
    internal func setup(_ success: @escaping (_ card:Card) ->(), _ error: @escaping (_ code:String, _ message:String) ->(),_ viewController: UIViewController){
        self.successCallback = success
        self.errorCallback = error
        self.viewController = viewController
    }
    
    internal override func layoutSubviews() {
        // Rounded corners
        self.layoutIfNeeded()
        self.contentView.layer.masksToBounds = true
        self.contentView.clipsToBounds = true
        self.contentView.layer.cornerRadius = 10
    }
    
    internal override func didMoveToSuperview() {
        self.contentView.transform = CGAffineTransform(scaleX: 0.5, y: 0.5)
        UIView.animate(withDuration: 0.15) {
            self.contentView.alpha = 1.0
            self.contentView.transform = CGAffineTransform.identity
        }
    }
    
    override func canPerformAction(_ action: Selector, withSender sender: Any?) -> Bool {
        return super.canPerformAction(action, withSender: sender)
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        let currentText = textField.text ?? ""
        guard let stringRange = Range(range, in: currentText) else { return false }
        
        let updatedText = currentText.replacingCharacters(in: stringRange, with: string)
        
        if(textField == textCardNumber){
            return updatedText.count <= 19
        }
        
        if(textField == textMonth || textField == textYear){
            return updatedText.count <= 2
        }
        
        if(textField == textCVV){
            return updatedText.count <= 3
        }
        
        return true
    }
    
    //    internal override textFi
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        self.setupView()
    }
    
    @IBAction func actionClose(_ sender: UIButton) {
        UIView.animate(withDuration: 0.15
            , animations: {
                self.contentView.alpha = 0.0
                self.contentView.transform = CGAffineTransform(scaleX: 0.5, y: 0.5)
        }) { (done:Bool) in
            self.removeFromSuperview()
            self.errorCallback(Constants.ERROR.USER_ACTION_CLOSE.CODE, Constants.ERROR.USER_ACTION_CLOSE.MESSAGE)
        }
    }
    
    @IBAction func actionAddCard(_ sender: UIButton) {
        print("[DPSDK] - ADD CARD ACTION")
        self.dismissKeyboard()

        if(self.validateFields()){
            self.showSpinner(onView: self)

            self.name = textCardholderName.text
            self.number = textCardNumber.text
            self.month = textMonth.text
            self.year = textYear.text
            self.securityCode = textCVV.text

            self.getSession()
        }
    }
    
    private var sessionId:String?
    private var apiVersion:String? = "46"
    private var name:String?
    private var number:String?
    private var month:String?
    private var year:String?
    private var securityCode:String?
    
    @objc func getSession() {
        httpController.post(proccessed: true,url: Constants.ROUTES.RETRIEVE_SESSION, parameters: nil, success:  { (data: NSDictionary) in
             if(Constants.DEBUG == true){print("[DATA] : \(data)")}
            
            if let session:[String:Any] = data["session"] as? [String : Any]{
                self.sessionId = session["id"] as? String
                
                 if(Constants.DEBUG == true){print("[DPSDK] - SESSION ID:\(self.sessionId!)")}
                let merchant:[String:Any]? = data["merchant"] as? [String : Any]
                let apiVersion:String? = data["apiVersion"] as? String
                
                
                if(merchant != nil && apiVersion != nil){
                    self.apiVersion = apiVersion
                    self.gatewayId = merchant!["gid"] as? String
                    self.check3ds = merchant!["isEnable3ds"] as? Bool

                    self.updateSession()
                }
            }
        }, handleError: {(error: String, message:String) in
             if(Constants.DEBUG == true){print("[DPSDK] - GET SESSION ERROR: " + message)}
            self.showRetryWithWarnings(warning: "Service unavailable!.", UITapGestureRecognizer(target: self, action: #selector(self.resetFields)))
        })
    }
    
    func updateSession() {
        if(sessionId != nil){
            var request = GatewayMap()
            
            request[at: "sourceOfFunds.provided.card.nameOnCard"] = self.name
            request[at: "sourceOfFunds.provided.card.number"] = self.number
            request[at: "sourceOfFunds.provided.card.securityCode"] = self.securityCode
            request[at: "sourceOfFunds.provided.card.expiry.month"] = self.month
            request[at: "sourceOfFunds.provided.card.expiry.year"] = self.year
            
            let gateway:Gateway = Gateway(
                region: Constants.ENV == Constants.ENVIRONMENT.PROD ? GatewayRegion.asiaPacific : GatewayRegion.mtf,
                merchantId: self.gatewayId!
            )
            
            gateway.updateSession(self.sessionId!, apiVersion: self.apiVersion!, payload: request) { (result: GatewayResult<GatewayMap>) in
                 if(Constants.DEBUG == true){print(result)}
                switch result {
                case .success(let response):
                     if(Constants.DEBUG == true){print("[DPSDK] _MP_RESPONSE", response.description)}
                     if(self.check3ds!){
                        self.check3dsStatus()
                     }else{
                        self.addCard()
                     }
                case .error(let error):
                     if(Constants.DEBUG == true){print(error)}
                    self.showRetryWithWarnings(warning: "Sorry! Service is unavailable!", UITapGestureRecognizer(target: self, action: #selector(self.resetFields)))
                }
            }
        }else{
            self.removeSpinner()
        }
    }
    
    func check3dsStatus() {
        if(Constants.DEBUG) {print("CHECKING_3DS")}
        
        let parameters:Dictionary = [
            "merchantId" : Constants.SDK.MERCHANT_ID,
            "_sessionId": self.sessionId!,
            "amount":  Constants.SDK.THREE_D_S_AMOUNT,
            ] as [String : Any]
        
        httpController.post(proccessed:true, url: Constants.ROUTES.CHECK_3DS, parameters: parameters, success: { (data:NSDictionary) in
            if let html:String = data.value(forKey: "html") as? String {
                DispatchQueue.main.async {
                    self.begin3DSAuth(simple: html)
                }
            }
        }, handleError: { (error:String, message:String) in
            if(Constants.DEBUG == true){print("[DPSDK] - ADD CARD ERROR: " + message)}
            if(error == Constants.ERROR.CARD_NOT_ENROLLED_EXCEPTION.CODE){
                self.addCard()
            }else{
                self.showRetryWithWarnings(warning:  message, UITapGestureRecognizer(target: self, action: #selector(self.resetFields)))
            }
        })
    }
    
    func addCard() {
        let parameters:Dictionary = [
            "reference" : ThirdPartyUser.uniqueUserId,
            "session": self.sessionId!,
            "customerName":  ThirdPartyUser.userName,
            "customerEmail": ThirdPartyUser.email ?? "",
            "customerMobile": ThirdPartyUser.mobile
        ]
        
        httpController.post(proccessed:true, url: Constants.ROUTES.CARD_ADD, parameters: parameters, success: { (data:NSDictionary) in
            if let newCard:NSDictionary = data.value(forKey: "newCard") as? NSDictionary {
                let card = Card()
                
                card.id = newCard["id"] as! Int
                card.brand = newCard["brand"] as! String
                card.mask = newCard["mask"] as! String
                card.reference = newCard["reference"] as! String
                
                self.newCard = card
                
                self.showSuccess(message: "Card successfully added.", UITapGestureRecognizer(target: self, action: #selector(self.actionDone)))
            }
            
        }, handleError: { (error:String, message:String) in
             if(Constants.DEBUG == true){print("[DPSDK] - ADD CARD ERROR: " + message)}
            self.showRetryWithWarnings(warning:  message, UITapGestureRecognizer(target: self, action: #selector(self.resetFields)))
        })
    }
    
    var newCard:Card?
    
    @objc func actionDone() {
        UIView.animate(withDuration: 0.15
            , animations: {
                DispatchQueue.main.async {
                    self.contentView.alpha = 0.0
                    self.contentView.transform = CGAffineTransform(scaleX: 0.5, y: 0.5)
                }
        }) { (done:Bool) in
            DispatchQueue.main.async {
                self.removeFromSuperview()
                self.successCallback(self.newCard != nil ? self.newCard! : Card())
            }
        }
    }
    
    @objc func resetFields() {
        self.removeSpinner()
        self.textCVV.text = ""
        self.textYear.text = ""
        self.textMonth.text = ""
        self.textCardNumber.text = ""
        self.textCardholderName.text = ""
    }
    
    func validateFields() -> Bool {
        labelWarnings.isHidden = true
        
        if((textCardholderName.text?.isEmpty)!){
            self.showWarning(component:textCardholderName, warning: "Cardholder name is required.")
            return false
        }
        
        if((textCardNumber.text?.isEmpty)!){
            self.showWarning(component:textCardNumber, warning: "Card number is required.")
            return false
        }
        
        if((textCardNumber.text?.count)! < 12 || (textCardNumber.text?.count)! > 19){
            self.showWarning(component:textCardNumber, warning: "Invalid credit card number.")
            return false
        }
        
        if((textMonth.text?.isEmpty)!){
            self.showWarning(component:textMonth, warning: "Expiry month required.")
            return false
        }
        
        if((textMonth.text?.count)! < 2){
            self.showWarning(component:textMonth, warning: "Invalid month!, Month should be in XX format.")
            return false
        }
        
        let month:Int = Int(textMonth.text!)!
        if(month>12 || month<1){
            self.showWarning(component:textMonth, warning: "Invalid month!, Month should be between 1 to 12.")
            return false
        }
        
        
        if((textYear.text?.isEmpty)!){
            self.showWarning(component:textYear, warning: "Expiry year required.")
            return false
        }
        
        if((textYear.text?.count)! < 2){
            self.showWarning(component:textYear, warning: "Invalid year!, Year should be in XX format.")
            return false
        }
        
        let year:Int = Int("20" + textYear.text!)!
        let currentYear = Calendar.current.component(.year, from: Date())
        let currentMonth = Calendar.current.component(.month, from: Date())
        
        if(Constants.DEBUG == true){print("YEAR: \(year), CURRENT_YEAR: \(currentYear)", "MONTH: \(month), CURRENT_MONTH: \(currentMonth)")}
        
        if(year < currentYear && month < currentMonth){
            self.showWarning(component:textMonth, warning: "This card is expired!")
            return false
        }
        
        if((textCVV.text?.isEmpty)!){
            self.showWarning(component:textCVV, warning: "Security code required.")
            return false
        }
        
        return true
    }
    
    let brandColor = UIColor.blue
    
    fileprivate func begin3DSAuth(simple: String) {
        // instatniate the Gateway 3DSecureViewController and present it
        let threeDSecureView = Gateway3DSecureViewController(nibName: nil, bundle: nil)
        viewController?.present(threeDSecureView, animated: true)
        
        // Optionally customize the presentation
        threeDSecureView.title = "3-D Secure Auth"
        threeDSecureView.navBar.tintColor = brandColor
        
        // Start 3D Secure authentication by providing the view with the HTML content provided by the check enrollment step
        threeDSecureView.authenticatePayer(htmlBodyContent: simple, handler: handle3DS(authView:result:))
    }
    
    func handle3DS(authView: Gateway3DSecureViewController, result: Gateway3DSecureResult) {
        // dismiss the 3DSecureViewController
        authView.dismiss(animated: true, completion: {
            print("3DS_RESULT: ", result)
            switch result {
            case .error(_):
                self.showRetryWithWarnings(warning:  "3DS Authentication Failed", UITapGestureRecognizer(target: self, action: #selector(self.resetFields)))
            case .completed(gatewayResult: let response):

                // check for version 46 and earlier api authentication failures and then version 47+ failures
                if Int(self.apiVersion!)! <= 46, let status = response[at: "3DSecure.summaryStatus"] as? String , status == "AUTHENTICATION_FAILED" {
                    self.showRetryWithWarnings(warning:  "3DS Authentication Failed", UITapGestureRecognizer(target: self, action: #selector(self.resetFields)))

                } else if let status = response[at: "response.gatewayRecommendation"] as? String, status == "DO_NOT_PROCEED"  {
                    self.showRetryWithWarnings(warning:  "3DS Authentication Failed", UITapGestureRecognizer(target: self, action: #selector(self.resetFields)))
                } else {
                    self.addCard()
                }
            default:
                self.showRetryWithWarnings(warning:  "3DS Authentication Cancelled", UITapGestureRecognizer(target: self, action: #selector(self.resetFields)))
            }
        })
    }
    
    func showWarning(component: UIView, warning: String, level:WarningLevel = .error) {
        component.becomeFirstResponder()
        labelWarnings.isHidden = false
        
        if(level == .error){
            labelWarnings.textColor = .red
        }else if(level  == .warning){
            labelWarnings.textColor = .yellow
        }
        
        labelWarnings.text = warning
    }
    
    func slideForKeyboard(_ type:String) {
        UIView.animate(withDuration: 0.15) {
            if(type == "up"){
                self.contentView.transform = CGAffineTransform(translationX: CGAffineTransform.identity.a, y: -50)
            }else{
                self.contentView.transform = CGAffineTransform.identity
            }
        }
    }
    
    @objc func keyboardWillShow(_ notification: Notification) {
        if let keyboardFrame: NSValue = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue {
            let keyboardRectangle = keyboardFrame.cgRectValue
            let keyboardHeight = keyboardRectangle.height
             if(Constants.DEBUG == true){print("[DPSDK] KEYBOARD HEIGHT",keyboardHeight)}
            self.slideForKeyboard("up")
        }
    }
    
    @objc func dismissKeyboard() {
        self.contentView.endEditing(true)
        self.slideForKeyboard("down")
    }
    
    private func setupView(){
        let bundle = Bundle(for: type(of: self))
        let nib = UINib(nibName: self.nibName, bundle: bundle)
        self.contentView = nib.instantiate(withOwner: self, options: nil).first as? UIView
        addSubview(contentView)
        
        contentView.center = self.center
        contentView.autoresizingMask = []
        contentView.translatesAutoresizingMaskIntoConstraints = true
//        contentView.layer.cornerRadius = 5
        contentView.layer.borderWidth = 2
        contentView.layer.borderColor = UIColor(red:0.00, green:0.13, blue:0.48, alpha:1.0).cgColor
        
        self.textCardholderName.text = ""
        self.textCardNumber.text = ""
        self.textCVV.text = ""
        self.textYear.text = ""
        self.textMonth.text = ""
        
        self.textCardholderName.delegate = self
        self.textCardNumber.delegate = self
        self.textCVV.delegate = self
        self.textMonth.delegate = self
        self.textYear.delegate = self
        
        self.textCardholderName.becomeFirstResponder()
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(self.keyboardWillShow),
            name: UIResponder.keyboardWillShowNotification,
            object: nil
        )
        
        self.contentView.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(self.dismissKeyboard)))
        self.removeSpinner()
    }
}

var vSpinner : UIView?

extension UIView{
    
    func showRetryWithWarnings(warning:String, _ tapRecog:UITapGestureRecognizer, level:WarningLevel = .error) {
        if(vSpinner != nil){
            DispatchQueue.main.async {
                vSpinner?.endEditing(true)
                UIView.animate(withDuration: 0.15, animations: {
                    switch level{
                    case .error:
                        vSpinner?.backgroundColor = UIColor(red:0.89, green:0.27, blue:0.27, alpha:1.0)
                        break
                    case .warning:
                        vSpinner?.backgroundColor = UIColor(red:0.86, green:0.44, blue:0.09, alpha:1.0)
                        break
                    case .info:
                        vSpinner?.backgroundColor = UIColor(red:0.12, green:0.55, blue:0.86, alpha:1.0)
                        break
                    case .debug:
                        vSpinner?.backgroundColor = UIColor(red:0.37, green:0.37, blue:0.37, alpha:1.0)
                        break
                    }
                
                }, completion: { (completed:Bool) in
                    let bounds:CGRect = (vSpinner?.bounds)!
                    
                    let buttonRetry:UIButton = UIButton(frame: CGRect(x: 0, y: (bounds.height/2)-25, width: bounds.width, height: 50))
                    buttonRetry.setTitle("Retry", for: .normal)
                    buttonRetry.addGestureRecognizer(tapRecog)
                    buttonRetry.center = (vSpinner?.center)!
                    buttonRetry.setTitleColor(.white , for: .normal)
                    
                    let labelWarning:UILabel = UILabel(frame: CGRect(x: 5, y: (bounds.height/2)-100, width: bounds.width - 5, height: 100))
                    labelWarning.text = warning
                    labelWarning.textAlignment = .center
                    labelWarning.lineBreakMode = .byWordWrapping
                    labelWarning.numberOfLines = 5
                    labelWarning.textColor = .white
                    labelWarning.font = UIFont(name: "System Bold", size: 15)
                    
                    if(level == .debug){
                        labelWarning.layer.borderWidth = 2
                        labelWarning.layer.borderColor = UIColor.black.cgColor
                    }
                    
                    for view:UIView in (vSpinner?.subviews)! {
                        view.removeFromSuperview()
                    }
                    vSpinner?.addSubview(buttonRetry)
                    vSpinner?.addSubview(labelWarning)
                })
            }
        }
    }
    
    func showSuccess(message:String, _ tapRecog:UITapGestureRecognizer) {
        if(vSpinner != nil){
            DispatchQueue.main.async {
                vSpinner?.endEditing(true)

                UIView.animate(withDuration: 0.20, animations: {
                    vSpinner?.backgroundColor = UIColor(red:0.37, green:0.70, blue:0.26, alpha:1.0)
                }, completion: { (completed:Bool) in
                    let bounds:CGRect = (vSpinner?.bounds)!
                    
                    let buttonDone:UIButton = UIButton(frame: CGRect(x: 0, y: (bounds.height/2)-25, width: bounds.width, height: 50))
                    buttonDone.setTitle("Done", for: .normal)
                    buttonDone.addGestureRecognizer(tapRecog)
                    buttonDone.center = (vSpinner?.center)!
                    buttonDone.setTitleColor(.white , for: .normal)
                    
                    let labelMessage:UILabel = UILabel(frame: CGRect(x: 5, y: (bounds.height/2)-100, width: bounds.width - 5, height: 100))
                    labelMessage.text = message
                    labelMessage.textAlignment = .center
                    labelMessage.lineBreakMode = .byWordWrapping
                    labelMessage.numberOfLines = 5
                    labelMessage.textColor = .white
                    labelMessage.font = UIFont(name: "System Bold", size: 15)
                    
                    let imageSuccess:UIImageView = UIImageView(image: UIImage(named: "success"))
//                    imageSuccess.contentMode = .scaleAspectFit
                    imageSuccess.frame = CGRect(x: 0, y: 0, width: 150, height: 150)
                    
                    for view:UIView in (vSpinner?.subviews)! {
                        view.removeFromSuperview()
                    }
                    vSpinner?.addSubview(buttonDone)
                    vSpinner?.addSubview(labelMessage)
//                    vSpinner?.addSubview(imageSuccess)
                })
            }
        }
    }
    
    func showSpinner(onView : UIView) {
        let spinnerView = UIView.init(frame: onView.bounds)
        spinnerView.backgroundColor = UIColor.init(red: 0.5, green: 0.5, blue: 0.5, alpha: 0.5)
        let ai = UIActivityIndicatorView.init(style: .whiteLarge)
        ai.startAnimating()
        ai.center = spinnerView.center
        
        DispatchQueue.main.async {
            spinnerView.addSubview(ai)
            onView.addSubview(spinnerView)
        }
        
        vSpinner = spinnerView
        vSpinner?.endEditing(true)
    }
    
    func removeSpinner() {
        DispatchQueue.main.async {
            vSpinner?.removeFromSuperview()
            vSpinner = nil
        }
    }
}

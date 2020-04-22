#import "React/RCTBridgeModule.h"

@interface RCT_EXTERN_MODULE(RNDirectPayCardPayment, NSObject)
    RCT_EXTERN_METHOD(addCardToUser: (NSString)env  
                    apiKey:(NSString)apiKey 
                    mid:(NSString)mid  
                    uid: (NSString)uid  
                    firstName: (NSString)firstName  
                    lastName: (NSString)lastName  
                    email: (NSString)email  
                    phoneNumber: (NSString)phoneNumber  
                    callback: (RCTResponseSenderBlock)callback
                )
@end

#import "React/RCTBridgeModule.h"

@interface RCT_EXTERN_MODULE(RNDirectPayCardPayment, NSObject)
    RCT_EXTERN_METHOD(addCardToUser: (String)env  (String)apiKey (String)mid  (String)uid  (String)firstName  (String)lastName  (String)email  (String)phoneNumber  (RCTResponseSenderBlock)callback )
@end

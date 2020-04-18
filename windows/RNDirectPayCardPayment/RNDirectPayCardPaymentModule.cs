using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;

namespace Direct.Pay.Card.Payment.RNDirectPayCardPayment
{
    /// <summary>
    /// A module that allows JS to share data.
    /// </summary>
    class RNDirectPayCardPaymentModule : NativeModuleBase
    {
        /// <summary>
        /// Instantiates the <see cref="RNDirectPayCardPaymentModule"/>.
        /// </summary>
        internal RNDirectPayCardPaymentModule()
        {

        }

        /// <summary>
        /// The name of the native module.
        /// </summary>
        public override string Name
        {
            get
            {
                return "RNDirectPayCardPayment";
            }
        }
    }
}

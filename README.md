
# react-native-direct-pay-card-payment

## Getting started

`$ npm install react-native-direct-pay-card-payment --save`

`$ cd ios && pod install && cd ..`


### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-direct-pay-card-payment` and add `RNDirectPayCardPayment.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNDirectPayCardPayment.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNDirectPayCardPaymentPackage;` to the imports at the top of the file
  - Add `new RNDirectPayCardPaymentPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-direct-pay-card-payment'
  	project(':react-native-direct-pay-card-payment').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-direct-pay-card-payment/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-direct-pay-card-payment')
  	```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNDirectPayCardPayment.sln` in `node_modules/react-native-direct-pay-card-payment/windows/RNDirectPayCardPayment.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Direct.Pay.Card.Payment.RNDirectPayCardPayment;` to the usings at the top of the file
  - Add `new RNDirectPayCardPaymentPackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import RNDirectPayCardPayment from 'react-native-direct-pay-card-payment';

// TODO: What to do with the module?
RNDirectPayCardPayment.addCardToUser(
                'dev', //env : dev or prod
                'e0e2c6e150775dff22e143af7ba234424', // apiKey
                'FN02385', // mid
                '8c1eeae8-07cb-4c7b-12334422e4q3', //unique id of the user
                'Jhon', // firstname of the user
                'Doe', // lastname of the user
                'jhon@mail.com', // email of the user
                '0712100113', // phone number of the user
                (_err, _r) => {
                  if (_err) {
                    console.log('code: ' + _err.code);
                    console.log('message: ' + _err.message);
                  } else {
                    //successfully added the card
                    console.log('id: ' + _r.id); // id (token) of the added card
                    console.log('mask: ' + _r.mask); // masked card number
                    console.log('reference: ' + _r.reference); // unique user id as the reference
                    console.log('brand: ' + _r.brand); // brand of the card (Visa / Mastercared)
                  }
                },
              );;
```
  

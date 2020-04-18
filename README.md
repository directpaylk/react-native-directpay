
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
RNDirectPayCardPayment;
```
  
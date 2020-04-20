
package com.reactlibrary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;
import com.reactlibrary.Controllers.Constants;
import com.reactlibrary.Controllers.HttpController;
import com.reactlibrary.Models.Card;
import com.reactlibrary.Models.CardAddCallback;

import org.json.JSONObject;

public class RNDirectPayCardPaymentModule extends ReactContextBaseJavaModule  implements ActivityEventListener {

  private final ReactApplicationContext reactContext;
  private static final String TAG = "DPSDK";
  private String sessionId;
//  private Gateway gateway;
  private String currency;

//  private HttpController http;
  private EditText textCardholder, textNickname, textCardnumber, textMonth, textYear, textCVV;
  private Button buttonClose, buttonAdd;
  private ProgressBar progressbar;
//  private Activity activity;
  private TextView labelWarning;
  private String nickname;

  private HttpController http;

  public RNDirectPayCardPaymentModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;

    http = new HttpController();

    Constants.API_KEY = "231e23e23e2e3";
    Constants.MERCHANT_ID = "TP00001";

    this.currency = currency;
//    gateway = new Gateway();
//    gateway.setMerchantId("DPAYMID");
//    gateway.setRegion(Gateway.Region.MTF);
  }

  @Override
  public String getName() {
    return "RNDirectPayCardPayment";
  }

  @ReactMethod
  public void show(String text) {
    final Context context = getReactApplicationContext();
    Toast.makeText(context, text, Toast.LENGTH_LONG).show();

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkDrawOverlayPermission()) {
        showCustomDialog(context);
    }

  }

  private void showCustomDialog(final Context context) {
        UiThreadUtil.runOnUiThread(
        new Runnable() {
public void run(){
  //  this.activity = activity;
    ViewGroup viewGroup = getCurrentActivity().findViewById(android.R.id.content);

    final View dialogView = LayoutInflater.from(context).inflate(R.layout.my_dialog, viewGroup, false);


    progressbar = dialogView.findViewById(R.id.progressbar);

    textCardholder = dialogView.findViewById(R.id.textCardholderName);
    textNickname = dialogView.findViewById(R.id.textNickname);
    textCardnumber = dialogView.findViewById(R.id.textCardNumber);
    textMonth = dialogView.findViewById(R.id.textMonth);
    textYear = dialogView.findViewById(R.id.textYear);
    textCVV = dialogView.findViewById(R.id.textCVV);

    labelWarning = dialogView.findViewById(R.id.textWarning);

    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setView(dialogView);
    final AlertDialog alertDialog = builder.create();

    buttonClose = dialogView.findViewById(R.id.buttonClose);
    buttonClose.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        alertDialog.dismiss();
       // callback.onError(Constants.ERRORS.USER_ACTION_CLOSED.CODE, Constants.ERRORS.USER_ACTION_CLOSED.MESSAGE);
      }
    });

    buttonAdd = dialogView.findViewById(R.id.buttonOk);
    buttonAdd.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (validateFields()) {
          progressbar(true);
          CardAddCallback callback = new CardAddCallback() {
            @Override
            public void onSuccess(Card card) {
              Toast.makeText(context, "success", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(int code, String message) {
              Toast.makeText(context, "failed", Toast.LENGTH_LONG).show();

            }
          };
          getSession(alertDialog, callback);
        }
      }
    });

    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    int LAYOUT_FLAG;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
    } else {
      LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
    }
    alertDialog.getWindow().setType(LAYOUT_FLAG);

    alertDialog.show();
    alertDialog.setCancelable(false);

}
        });
  }

  private void progressbar(final boolean show) {
    UiThreadUtil.runOnUiThread(
            new Runnable() {
              public void run(
                      ) { progressbar.setVisibility(show ? View.VISIBLE : View.GONE);}
            }
    );

    setEnabled(!show);
  }
  private void setEnabled(boolean enabled) {
    buttonAdd.setEnabled(enabled);
    buttonClose.setEnabled(enabled);

    textCardholder.setEnabled(enabled);
    textNickname.setEnabled(enabled);
    textCardnumber.setEnabled(enabled);
    textYear.setEnabled(enabled);
    textMonth.setEnabled(enabled);
    textCVV.setEnabled(enabled);
  }


  private boolean validateFields() {
    if (textCardholder.getText().toString().isEmpty()) {
      textCardholder.setError("Cardholder name is required!");
      textCardholder.requestFocus();
      return false;
    }

    if (textCardnumber.getText().toString().isEmpty()) {
      textCardnumber.setError("Card number is required!");
      textCardnumber.requestFocus();
      return false;
    }

    if (textCardnumber.getText().length() < 16) {
      textCardnumber.setError("Invalid Card number !");
      textCardnumber.requestFocus();
      return false;
    }

    if (textMonth.getText().toString().length() < 2) {
      textMonth.setError("Expiry month required!\nE.g. (MM)");
      textMonth.requestFocus();
      return false;
    }

    if (Integer.valueOf(textMonth.getText().toString()) > 12) {
      textMonth.setError("Expiry month in invalid!\nMust be less than 12.");
      textMonth.requestFocus();
      return false;
    }

    if (textYear.getText().toString().length() < 2) {
      textYear.setError("Expiry year required!");
      textYear.requestFocus();
      return false;
    }

    if (textCVV.getText().toString().length() < 3) {
      textCVV.setError("Security code required!");
      textCVV.requestFocus();
      return false;
    }

    return true;
  }

  private void getSession(final AlertDialog dialog, final CardAddCallback callback) {
    nickname = textNickname.getText().toString();

    final String cardholder = textCardholder.getText().toString();
    final String cardNumber = textCardnumber.getText().toString();
    final String year = textYear.getText().toString();
    final String month = textMonth.getText().toString();
    final String cvv = textCVV.getText().toString();

    resetFields();

    http.post(getCurrentActivity(), Constants.ROUTES.GET_SESSION.URL, null, new HttpCallback() {
      @Override
      public void success(JSONObject data) {
        Log.d(TAG, "success() called with: data = [" + data + "]");
        try {
          Constants.API_VERSION = data.getString("apiVersion");
          Log.i(TAG, "success: API_VERSION: " + Constants.API_VERSION);
          JSONObject session = data.getJSONObject("session");
          sessionId = session.getString("id");
          Log.i(TAG, "success: SESSION_ID:" + sessionId);
          progressbar(false);
        //  updateSession(dialog, sessionId, cardholder, cardNumber, year, month, cvv, callback);
        } catch (Exception ignored) {
        }
      }

      @Override
      public void error(int code, String message) {
        Log.d(TAG, "error() called with: code = [" + code + "], message = [" + message + "]");
        progressbar(false);
        callback.onError(Constants.ERRORS.SERVICE_UNAVAILABLE.CODE, Constants.ERRORS.SERVICE_UNAVAILABLE.MESSAGE);
      }
    });
  }

  private void resetFields() {
    textCardholder.setText(null);
    textNickname.setText(null);
    textCardnumber.setText(null);
    textYear.setText(null);
    textMonth.setText(null);
    textCVV.setText(null);
    hideWarnings();
    System.gc();
  }

  private void hideWarnings() {
    labelWarning.setVisibility(View.GONE);
  }

  private void warningMessage(String message, boolean error) {
    labelWarning.setVisibility(View.VISIBLE);
    labelWarning.setText(message);
    if (error) {
      labelWarning.setTextColor(Color.RED);
    } else {
      labelWarning.setTextColor(Color.GREEN);
    }
    textCardholder.requestFocus();
  }

  // code to post/handler request for permission
  public final static int REQUEST_CODE = 100;
  @RequiresApi(api = Build.VERSION_CODES.M)
  public boolean checkDrawOverlayPermission() {
    Log.v("App", "Package Name: " + reactContext.getPackageName());

    // check if we already  have permission to draw over other apps
    if (!Settings.canDrawOverlays(reactContext)) {
      Log.v("App", "Requesting Permission" + Settings.canDrawOverlays(reactContext));
      // if not construct intent to request permission
      Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
              Uri.parse("package:" + reactContext.getPackageName()));
    // request permission via start activity for result
      reactContext.startActivityForResult(intent, REQUEST_CODE,null);
      return false;
    } else {
      return true;
    }
  }

  @Override
  public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
    Log.v("App", "OnActivity Result.");
    //check if received result code
    //  is equal our requested code for draw permission
    if (requestCode == REQUEST_CODE) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (Settings.canDrawOverlays(reactContext)) {
          showCustomDialog(reactContext);
        }
      }
    }
  }

  @Override
  public void onNewIntent(Intent intent) {

  }
}

package com.reactlibrary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableMap;
import com.reactlibrary.Controllers.Constants;
import com.reactlibrary.Controllers.HttpController;
import com.reactlibrary.Models.Card;
import com.reactlibrary.Models.CardAddCallback;
import com.reactlibrary.Models.ThirdPartyUser;

import org.json.JSONObject;

import java.util.Set;

public class RNDirectPayCardPaymentModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    private final ReactApplicationContext reactContext;
    private static final String TAG = "DPSDK";
    static final String REDIRECT_SCHEME = "https";
    static final String REDIRECT_HOST = "prod.directpay.lk";
    private final Gateway gateway;
    private String sessionId;
    private HttpController http;
    //  private HttpController http;
    private EditText textCardholder, textCardnumber, textMonth, textYear, textCVV;
    private LinearLayout layoutCardDetails;

    private CardAddCallback addCallback;
    private Button buttonClose, buttonAdd;
    private ProgressBar progressbar;


    //  private Gateway gateway;
    private String currency;

    private WebView webView;


    private Activity activity;
    private TextView labelWarning;
    private String nickname;


    public RNDirectPayCardPaymentModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;

        http = new HttpController();
        gateway = new Gateway();

    }
//    public RNDirectPayCardPaymentModule(Activity  activity) {
//        super(activity);
//        this.activity = activity;
//
//        http = new HttpController();
//        gateway = new Gateway();
//
//    }

    @Override
    public String getName() {
        return "RNDirectPayCardPayment";
    }

    @ReactMethod
    public void addCardToUser(String env, String apiKey, String mid, String uid, String firstName, String lastName, String email, String phoneNumber, final Callback callback) {

        if (env.equals(Constants.DEV)) {
            Constants.ENV = Constants.DEV;
            gateway.setRegion(Gateway.Region.MTF);
        } else {
            Constants.ENV = Constants.PROD;
            gateway.setRegion(Gateway.Region.ASIA_PACIFIC);
        }
        Constants.API_KEY = apiKey;
        Constants.MERCHANT_ID = mid;

        ThirdPartyUser.uniqueUserId = uid;
        ThirdPartyUser.email = email;
        ThirdPartyUser.mobile = phoneNumber;
        ThirdPartyUser.userName = firstName + " " + lastName;

        this.currency = currency;

        final Context context = getReactApplicationContext();
        Toast.makeText(context, "adding card", Toast.LENGTH_LONG).show();

        addCallback = new CardAddCallback() {
            @Override
            public void onSuccess(Card card) {
                WritableMap resultObj = Arguments.createMap();
                resultObj.putString("id", String.valueOf(card.getId()));
                resultObj.putString("mask", card.getMask());
                resultObj.putString("brand", card.getBrand());
                resultObj.putString("reference", card.getReference());

                callback.invoke(null, resultObj);
            }

            @Override
            public void onError(int code, String message) {
                WritableMap errorObj = Arguments.createMap();
                errorObj.putString("code", String.valueOf(code));
                errorObj.putString("message", message);

                callback.invoke(errorObj, null);
            }
        };

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkDrawOverlayPermission()) {
            showCustomDialog(context, addCallback);
        }


    }


    private WebViewClient buildWebViewClient(AlertDialog dialog, CardAddCallback callback) {
        return new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Constants.debug) {
                    Log.d(TAG, "shouldOverrideUrlLoading() called with: view = [" + view + "], url = [" + url + "]");
                }
                webViewUrlChanges(Uri.parse(url), dialog, callback);
                return true;
            }
        };
    }

    private void webViewUrlChanges(Uri uri, AlertDialog dialog, CardAddCallback callback) {
        String scheme = uri.getScheme();
        final String host = uri.getHost();
        if (Constants.debug) {
            Log.i(TAG, "webViewUrlChanges: " + scheme);
        }
        if (REDIRECT_SCHEME.equalsIgnoreCase(scheme) && REDIRECT_HOST.equalsIgnoreCase(host)) {
            complete(getACSResultFromUri(uri), dialog, callback);
        } else if ("mailto".equalsIgnoreCase(scheme)) {
            intentToEmail(uri);
        } else {
            loadWebViewUrl(uri);
        }
    }

    private void complete(String acsResult, AlertDialog dialog, CardAddCallback callback) {
        webView.setVisibility(View.GONE);
        layoutCardDetails.setVisibility(View.VISIBLE);

        if (Constants.debug) {
            Log.d(TAG, "complete() called with: acsResult = [" + acsResult + "]");
        }

        try {
            JSONObject responseObj = new JSONObject(acsResult);
            JSONObject response = responseObj.getJSONObject("response");

            JSONObject secure3d = response.getJSONObject("3DSecure");
            String gatewayCode = secure3d.getString("gatewayCode");

            if (gatewayCode.equals(Constants.RESPONSES.AUTHENTICATION_SUCCESSFUL.CODE) || gatewayCode.equals(Constants.RESPONSES.AUTHENTICATION_ATTEMPTED.CODE)) {
                addCard(dialog, callback);
            } else {
                progressbar(false);
                Constants.ERRORS authFailed = Constants.ERRORS.AUTHENTICATION_FAILED;
                warningMessage(gatewayCode, true);
            }
        } catch (Exception ex) {
            if (Constants.debug) {
                ex.printStackTrace();
            }
        }
    }

    private void loadWebViewUrl(Uri uri) {
        webView.loadUrl(uri.toString());
    }

    private void intentToEmail(Uri uri) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        intentToEmail(uri, emailIntent);
    }

    // separate for testability
    private void intentToEmail(Uri uri, Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(uri);

        reactContext.startActivity(intent);
    }

    private String getACSResultFromUri(Uri uri) {
        String result = null;

        Set<String> params = uri.getQueryParameterNames();
        for (String param : params) {
            if ("acsResult".equalsIgnoreCase(param)) {
                result = uri.getQueryParameter(param);
            }
        }

        return result;
    }

    private int getNavigationBarHeight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            getCurrentActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            getCurrentActivity().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }

    private DisplayMetrics displayMetrics = new DisplayMetrics();

    private int getScreenHeight() {
        getCurrentActivity().getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);
        return displayMetrics.heightPixels + getNavigationBarHeight();
    }

    private void showCustomDialog(final Context context, final CardAddCallback callback) {
        UiThreadUtil.runOnUiThread(
                new Runnable() {
                    public void run() {
                        //  this.activity = activity;
                        ViewGroup viewGroup = getCurrentActivity().findViewById(android.R.id.content);

                        final View dialogView = LayoutInflater.from(context).inflate(R.layout.my_dialog, viewGroup, false);


                        progressbar = dialogView.findViewById(R.id.progressbar);
                        textCardholder = dialogView.findViewById(R.id.textCardholderName);
                        textCardnumber = dialogView.findViewById(R.id.textCardNumber);
                        textMonth = dialogView.findViewById(R.id.textMonth);
                        textYear = dialogView.findViewById(R.id.textYear);
                        textCVV = dialogView.findViewById(R.id.textCVV);

                        webView = dialogView.findViewById(R.id.webview3ds);
                        webView.setWebChromeClient(new WebChromeClient());
                        webView.getSettings().setDomStorageEnabled(true);
                        webView.getSettings().setJavaScriptEnabled(true);

                        ViewGroup.LayoutParams webviewLayoutParams = webView.getLayoutParams();
                        webviewLayoutParams.height = (int) (getScreenHeight() * 0.9);
                        webView.setLayoutParams(webviewLayoutParams);

                        layoutCardDetails = dialogView.findViewById(R.id.layoutCardDetails);


                        labelWarning = dialogView.findViewById(R.id.textWarning);

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setView(dialogView);
                        final AlertDialog alertDialog = builder.create();

                        buttonClose = dialogView.findViewById(R.id.buttonClose);
                        buttonClose.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();
                                callback.onError(Constants.ERRORS.USER_ACTION_CLOSED.CODE, Constants.ERRORS.USER_ACTION_CLOSED.MESSAGE);
                            }
                        });

                        buttonAdd = dialogView.findViewById(R.id.buttonOk);
                        buttonAdd.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (validateFields()) {
                                    progressbar(true);
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
                    ) {
                        progressbar.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                }
        );

        setEnabled(!show);
    }

    private void setEnabled(boolean enabled) {
        buttonAdd.setEnabled(enabled);
        buttonClose.setEnabled(enabled);

        textCardholder.setEnabled(enabled);

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

                    //retrieve mid from response
                    JSONObject merchant = data.getJSONObject("merchant");
                    String gatewayId = merchant.getString("gid");
                    boolean isEnable3ds = merchant.getBoolean("isEnable3ds");

                    gateway.setMerchantId(gatewayId);


                    progressbar(false);
                    updateSession(dialog, sessionId, cardholder, cardNumber, year, month, cvv, isEnable3ds, callback);
                } catch (Exception ignored) {
                }
            }

            @Override
            public void error(int code, String message) {
                if (Constants.debug)
                    Log.d(TAG, "error() called with: code = [" + code + "], message = [" + message + "]");
                progressbar(false);
                warningMessage(message, true);
            }
        });
    }

    private void resetFields() {

        textCardholder.setText(null);
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
            reactContext.startActivityForResult(intent, REQUEST_CODE, null);
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
                    showCustomDialog(reactContext, addCallback);
                }
            }
        }
    }

    private void updateSession(final AlertDialog dialog, String sessionId, String cardHolder, String cardNumber, String year, String month, String cvv, boolean is3dsEnabled, final CardAddCallback callback) {
        try {
            GatewayMap request = new GatewayMap()
                    .set("sourceOfFunds.provided.card.nameOnCard", cardHolder)
                    .set("sourceOfFunds.provided.card.number", cardNumber)
                    .set("sourceOfFunds.provided.card.securityCode", cvv)
                    .set("sourceOfFunds.provided.card.expiry.month", month)
                    .set("sourceOfFunds.provided.card.expiry.year", year);

            if (Constants.debug)
                Log.i(TAG, "updateSession: called, session_id: " + sessionId + ", api_version: " + Constants.API_VERSION);

            gateway.updateSession(sessionId, Constants.API_VERSION, request, new GatewayCallback() {
                @Override
                public void onSuccess(GatewayMap response) {
                    if (Constants.debug)
                        Log.d(TAG, "onSuccess() called with: response = [" + response + "]");
                    if (is3dsEnabled) {
                        check3ds(sessionId, dialog, callback);
                    } else {
                        addCard(dialog, callback);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    if (Constants.debug)
                        Log.d(TAG, "onError() called with: throwable = [" + throwable + "]");
                    progressbar(false);
                    warningMessage(throwable.getMessage(), true);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            System.gc();
        }
    }

    private void setWebViewHtml(String html) {
        String encodedHtml = Base64.encodeToString(html.getBytes(), Base64.NO_PADDING);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadData(encodedHtml, "text/html", "base64");
    }

    private void check3ds(String sessionId, AlertDialog dialog, CardAddCallback callback) {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("merchantId", Constants.MERCHANT_ID);
            parameters.put("_sessionId", sessionId);
            parameters.put("amount", 5);
        } catch (Exception ex) {
        }

        http.post(getCurrentActivity(), Constants.ROUTES.CHECK_3DS.URL, parameters, new HttpCallback() {
            @Override
            public void success(JSONObject data) {
                if (Constants.debug) Log.d(TAG, "success() called with: data = [" + data + "]");
                try {
                    String html = data.getString("html");
                    String secureId = data.getString("3DSecureId");
                    setWebViewHtml(html);
                    webView.setWebViewClient(buildWebViewClient(dialog, callback));

                    layoutCardDetails.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void error(int code, String message) {
                if (code == Constants.ERRORS.CARD_NOT_ENROLLED_EXCEPTION.CODE) {
                    addCard(dialog, callback);
                } else {
                    if (Constants.debug)
                        Log.d(TAG, "error() called with: code = [" + code + "], message = [" + message + "]");
                    progressbar(false);
                    warningMessage(message, true);
                }
            }
        });
    }


    private void addCard(final AlertDialog dialog, final CardAddCallback callback) {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("cardNickname", nickname);
            parameters.put("reference", ThirdPartyUser.uniqueUserId);
            parameters.put("customerName", ThirdPartyUser.userName);
            parameters.put("customerEmail", ThirdPartyUser.email);
            parameters.put("customerMobile", ThirdPartyUser.mobile);
            parameters.put("session", sessionId);
        } catch (Exception ex) {
        }

        http.post(getCurrentActivity(), Constants.ROUTES.ADD_CARD.URL, parameters, new HttpCallback() {
            @Override
            public void success(JSONObject data) {
                if (Constants.debug) Log.d(TAG, "success() called with: data = [" + data + "]");
                progressbar(false);

                Card card = new Card();
                try {
                    JSONObject newCard = data.getJSONObject("newCard");

                    card.setId(newCard.getInt("id"));
                    card.setMask(newCard.getString("mask"));
                    card.setBrand(newCard.getString("brand"));
                    card.setReference(newCard.getString("reference"));
                    card.setNickname(newCard.getString("nickName"));
                } catch (Exception ignored) {
                }

                warningMessage("Credit/Debit card successfully Added!", false);
                buttonClose.setVisibility(View.GONE);
                buttonAdd.setText(R.string.done);
                buttonAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                callback.onSuccess(card);
            }

            @Override
            public void error(int code, String message) {
                if (Constants.debug)
                    Log.d(TAG, "error() called with: code = [" + code + "], message = [" + message + "]");

                progressbar(false);
                Constants.ERRORS cannotProcess = Constants.ERRORS.CANNOT_PROCESS;
                warningMessage(message, true);
            }
        });
    }

    @Override
    public void onNewIntent(Intent intent) {

    }

}
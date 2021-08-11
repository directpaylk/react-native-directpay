package com.reactlibrary.Controllers;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.reactlibrary.HttpCallback;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpController {
    private static final String TAG = "HTTP_CONTROLLER";

    public void post(Activity activity, String url, JSONObject parameters, HttpCallback httpCallback) {
        String jsonString = parameters != null ? parameters.toString() : "";
        String digest = DPDigest.encode(jsonString);

        new HttpRequestTask().execute(activity, url, jsonString, digest, httpCallback);
    }

    private class HttpRequestTask extends AsyncTask<Object, Integer, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            Activity activity = (Activity) params[0];
            String my_url = params[1].toString();
            String my_data = params[2].toString();
            String digest = params[3].toString();
            HttpCallback callback = (HttpCallback) params[4];

            if (Constants.debug)
                Log.d(TAG, "doInBackground: URL: " + my_url + ", DATA: " + my_data + ", DIGEST: " + digest);

            if (Constants.debug) Log.d(TAG, "doInBackground: DIGEST:" + digest);

            try {
                URL url = new URL(my_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                // setting the  Request Method Type
                httpURLConnection.setRequestMethod("POST");
                // adding the headers for request
                httpURLConnection.setRequestProperty(Constants.HEADERS.CONTENT_TYPE.KEY, "application/json");
                httpURLConnection.setRequestProperty(Constants.HEADERS.DP_MERCHANT.KEY, Constants.MERCHANT_ID);
                httpURLConnection.setRequestProperty(Constants.HEADERS.X_API_KEY.KEY, Constants.API_KEY);
                httpURLConnection.setRequestProperty(Constants.HEADERS.DIGEST.KEY, digest);

                try {
                    //to tell the connection object that we will be wrting some data on the server and then will fetch the output result
                    httpURLConnection.setDoOutput(true);
                    // this is used for just in case we don't know about the data size associated with our request
                    httpURLConnection.setChunkedStreamingMode(0);

                    // to write tha data in our request
                    OutputStream outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
                    outputStreamWriter.write(my_data);
                    outputStreamWriter.flush();
                    outputStreamWriter.close();

                    // to log the response code of your request
                    if (Constants.debug)
                        Log.d(TAG, "MyHttpRequestTask doInBackground : " + httpURLConnection.getResponseCode());
                    // to log the response message from your server after you have tried the request.
                    if (Constants.debug)
                        Log.d(TAG, "MyHttpRequestTask doInBackground [MESSAGE]: " + httpURLConnection.getResponseMessage());
                    InputStream inputStream;

                    if (httpURLConnection.getResponseCode() == Constants.HTTP_CODE.OK.CODE) {
                        inputStream = httpURLConnection.getInputStream();
                    } else {
                        inputStream = httpURLConnection.getErrorStream();
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    if (Constants.debug)
                        Log.i(TAG, "doInBackground: RESPONSE: " + response.toString());
                    JSONObject jsonResponse = new JSONObject(response.toString());

                    if (jsonResponse.has("status")) {
                        int status = jsonResponse.getInt("status");
                        JSONObject data = jsonResponse.getJSONObject("data");

                        if (status == Constants.HTTP_CODE.OK.CODE) {
                            String serverDigest = httpURLConnection.getHeaderField(Constants.HEADERS.DIGEST.KEY);
                            if (serverDigest == null) {
                                activity.runOnUiThread(() -> callback.success(data));
                                return null;
                            }
//                            String sdkDigest = DPDigest.encode(data.toString());
//                            if (serverDigest.equals(sdkDigest)) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.success(data);
                                }
                            });
                            return null;
//                            } else {
//                                Constants.ERRORS error = Constants.ERRORS.INVALID_SERVER_RESPONSE;
//                                activity.runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        callback.error(error.CODE, error.MESSAGE);
//                                    }
//                                });
//
//                                return null;
//                            }
                        } else if (status == Constants.HTTP_CODE.BAD_REQUEST.CODE) {
                            String code = data.has("error") ? data.getString("error") : data.getString("code");
                            String message = data.getString("message");

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (Constants.CARD_NOT_ENROLLED_EXCEPTION.equals(code)) {
                                        Constants.ERRORS error = Constants.ERRORS.CARD_NOT_ENROLLED_EXCEPTION;
                                        callback.error(error.CODE, message);
                                    } else {
                                        Constants.ERRORS error = Constants.ERRORS.CANNOT_PROCESS;
                                        callback.error(error.CODE, message);
                                    }
                                }
                            });

                            return null;
                        }
                    }

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //error callback
                            Constants.ERRORS error = Constants.ERRORS.SERVICE_UNAVAILABLE;
                            callback.error(error.CODE, error.MESSAGE);
                        }
                    });
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // this is done so that there are no open connections left when this task is going to complete
                    httpURLConnection.disconnect();
                    this.cancel(true);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}

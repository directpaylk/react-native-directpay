package com.reactlibrary.Models;

public interface PaymentCallback {
    public void onSuccess(PaymentInfo info);

    public void onError(int code, String message);
}

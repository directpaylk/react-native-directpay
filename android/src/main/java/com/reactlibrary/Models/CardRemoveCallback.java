package com.reactlibrary.Models;

public interface CardRemoveCallback {
    public void onSuccess();

    public void onError(int code, String message);
}

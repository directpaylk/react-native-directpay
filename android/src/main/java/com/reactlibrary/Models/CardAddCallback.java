package com.reactlibrary.Models;

public interface CardAddCallback {
    public void onSuccess(Card card);

    public void onError(int code, String message);
}

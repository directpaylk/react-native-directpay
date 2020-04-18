package com.reactlibrary.Models;

import java.util.ArrayList;

public interface CardListCallback {
    public void onSuccess(ArrayList<Card> cards);

    public void onError(int code, String message);
}

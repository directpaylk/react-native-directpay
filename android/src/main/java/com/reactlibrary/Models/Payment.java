package com.reactlibrary.Models;

public class Payment {
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public String getTxReference() {
        return txReference;
    }

    public void setTxReference(String txReference) {
        this.txReference = txReference;
    }

    double amount;
    int cardId;
    String txReference;
}

package de.julianhofmann.h_bank.api.models;

import com.google.gson.annotations.Expose;

public class CashModel {
    @Expose
    private String cash;

    public CashModel(String cash) {
        this.cash = cash;
    }

    public String getCash() {
        return cash;
    }

    public void setCash(String cash) {
        this.cash = cash;
    }
}

package de.julianhofmann.h_bank.api.models;

import com.google.gson.annotations.Expose;

public class TransferMoneyModel {
    @Expose
    private String receiver;
    @Expose
    private String amount;
    @Expose
    private String description;

    public TransferMoneyModel(String receiver, String amount, String description) {
        this.receiver = receiver;
        this.amount = amount;
        this.description = description;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

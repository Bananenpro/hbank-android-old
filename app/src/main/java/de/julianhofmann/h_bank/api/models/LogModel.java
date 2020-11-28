package de.julianhofmann.h_bank.api.models;

import com.google.gson.annotations.SerializedName;

public class LogModel {
    private int id;
    private String username;
    private String amount;
    @SerializedName("new_balance")
    private String newBalance;
    private String date;
    private String description;

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getAmount() {
        return amount;
    }

    public String getNewBalance() {
        return newBalance;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

}

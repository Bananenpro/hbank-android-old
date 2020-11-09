package de.julianhofmann.h_bank.api.models;

import com.google.gson.annotations.SerializedName;

public class UserModel {
    private String name;
    @SerializedName("is_parent")
    private Boolean isParent;
    private String balance;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsParent() {
        return isParent;
    }

    public void setIsParent(Boolean isParent) {
        this.isParent = isParent;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }
}

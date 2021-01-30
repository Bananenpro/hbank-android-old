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
    @SerializedName("is_payment_plan")
    private boolean isPaymentPlan;
    @SerializedName("payment_plan_id")
    private Integer paymentPlanId;

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

    public boolean isPaymentPlan() {
        return isPaymentPlan;
    }

    public Integer getPaymentPlanId() {
        return paymentPlanId;
    }
}

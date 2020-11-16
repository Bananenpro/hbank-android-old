package de.julianhofmann.h_bank.api.models;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PaymentPlanModel {
    @Expose
    private String receiver;
    @Expose
    private String amount;
    @Expose
    private int schedule;
    @Expose
    private String description;
    private int id;
    @SerializedName("days_left")
    private int daysLeft;

    public PaymentPlanModel(String receiver, String amount, int schedule, String description) {
        this.receiver = receiver;
        this.amount = amount;
        this.schedule = schedule;
        this.description = description;
    }

    public int getDaysLeft() {
        return daysLeft;
    }

    public void setDaysLeft(int daysLeft) {
        this.daysLeft = daysLeft;
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

    public int getSchedule() {
        return schedule;
    }

    public void setSchedule(int schedule) {
        this.schedule = schedule;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

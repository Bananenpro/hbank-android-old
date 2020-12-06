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
    @SerializedName("left")
    private int left;
    private String user;
    @SerializedName("schedule_unit")
    private String scheduleUnit;

    public PaymentPlanModel(String receiver, String amount, int schedule, String scheduleUnit, String description) {
        this.receiver = receiver;
        this.amount = amount;
        this.schedule = schedule;
        this.scheduleUnit = scheduleUnit;
        this.description = description;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getScheduleUnit() {
        return scheduleUnit;
    }

    public void setScheduleUnit(String scheduleUnit) {
        this.scheduleUnit = scheduleUnit;
    }
}

package de.julianhofmann.h_bank.api.models;

import com.google.gson.annotations.SerializedName;

public class InfoModel {
    @SerializedName("payment_plans")
    private boolean paymentPlans;
    private boolean backups;
    private String cpu;
    private String ram;
    private String disk;
    private String temperature;

    public boolean isPaymentPlans() {
        return paymentPlans;
    }

    public boolean isBackups() {
        return backups;
    }

    public String getCpu() {
        return cpu;
    }

    public String getRam() {
        return ram;
    }

    public String getDisk() {
        return disk;
    }

    public String getTemperature() {
        return temperature;
    }
}

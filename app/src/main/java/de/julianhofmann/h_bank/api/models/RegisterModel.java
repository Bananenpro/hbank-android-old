package de.julianhofmann.h_bank.api.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RegisterModel {

    @Expose
    private String name;

    @Expose
    private String password;

    @SerializedName("is_parent")
    @Expose
    private Boolean isParent;


    public RegisterModel(String name, String password, Boolean isParent) {
        this.name = name;
        this.password = password;
        this.isParent = isParent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getIsParent() {
        return isParent;
    }

    public void setIsParent(Boolean isParent) {
        this.isParent = isParent;
    }

}

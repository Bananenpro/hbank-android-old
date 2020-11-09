package de.julianhofmann.h_bank.api.models;

import com.google.gson.annotations.Expose;

public class LoginModel {
    @Expose
    private String name;
    @Expose
    private String password;

    public LoginModel(String name, String password) {
        this.name = name;
        this.password = password;
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
}

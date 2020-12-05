package de.julianhofmann.h_bank.api.models;

import com.google.gson.annotations.Expose;

@SuppressWarnings("FieldCanBeLocal")
public class LoginModel {
    @Expose
    private final String name;
    @Expose
    private final String password;

    public LoginModel(String name, String password) {
        this.name = name;
        this.password = password;
    }

}

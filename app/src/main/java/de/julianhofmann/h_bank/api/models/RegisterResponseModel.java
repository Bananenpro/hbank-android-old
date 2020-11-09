package de.julianhofmann.h_bank.api.models;

import com.google.gson.annotations.SerializedName;

public class RegisterResponseModel {
    @SerializedName("name_length")
    private Boolean nameLength;

    @SerializedName("password_length")
    private Boolean passwordLength;

    @SerializedName("required_password_length")
    private Integer requiredPasswordLength;

    @SerializedName("already_exists")
    private Boolean alreadyExists;

    public Boolean getAlreadyExists() {
        return alreadyExists;
    }

    public void setAlreadyExists(Boolean alreadyExists) {
        this.alreadyExists = alreadyExists;
    }

    public Boolean getNameLength() {
        return nameLength;
    }

    public void setNameLength(Boolean nameLength) {
        this.nameLength = nameLength;
    }

    public Boolean getPasswordLength() {
        return passwordLength;
    }

    public void setPasswordLength(Boolean passwordLength) {
        this.passwordLength = passwordLength;
    }

    public Integer getRequiredPasswordLength() {
        return requiredPasswordLength;
    }

    public void setRequiredPasswordLength(Integer requiredPasswordLength) {
        this.requiredPasswordLength = requiredPasswordLength;
    }
}

package de.julianhofmann.h_bank.api;

import de.julianhofmann.h_bank.api.models.LoginModel;
import de.julianhofmann.h_bank.api.models.LoginResponseModel;
import de.julianhofmann.h_bank.api.models.RegisterModel;
import de.julianhofmann.h_bank.api.models.RegisterResponseModel;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface HBankApi {

    @POST("register")
    Call<RegisterResponseModel> register(@Body RegisterModel model);

    @POST("login")
    Call<LoginResponseModel> login(@Body LoginModel model);

}

package de.julianhofmann.h_bank.api;

import de.julianhofmann.h_bank.api.models.LoginModel;
import de.julianhofmann.h_bank.api.models.LoginResponseModel;
import de.julianhofmann.h_bank.api.models.RegisterModel;
import de.julianhofmann.h_bank.api.models.RegisterResponseModel;
import de.julianhofmann.h_bank.api.models.UserModel;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface HBankApi {

    @GET("user/{name}")
    Call<UserModel> getUser(@Path("name") String name, @Header("Authorization") String authorization);

    @POST("register")
    Call<RegisterResponseModel> register(@Body RegisterModel model);

    @POST("login")
    Call<LoginResponseModel> login(@Body LoginModel model);

}

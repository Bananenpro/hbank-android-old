package de.julianhofmann.h_bank.api;

import java.util.List;

import de.julianhofmann.h_bank.api.models.IntIdModel;
import de.julianhofmann.h_bank.api.models.LogModel;
import de.julianhofmann.h_bank.api.models.LoginModel;
import de.julianhofmann.h_bank.api.models.LoginResponseModel;
import de.julianhofmann.h_bank.api.models.PaymentPlanModel;
import de.julianhofmann.h_bank.api.models.RegisterModel;
import de.julianhofmann.h_bank.api.models.RegisterResponseModel;
import de.julianhofmann.h_bank.api.models.TransferMoneyModel;
import de.julianhofmann.h_bank.api.models.UserModel;
import de.julianhofmann.h_bank.api.models.VersionModel;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface HBankApi {

    @GET("user/{name}")
    Call<UserModel> getUser(@Path("name") String name, @Header("Authorization") String authorization);

    @GET("user")
    Call<List<UserModel>> getUsers();

    @POST("register")
    Call<RegisterResponseModel> register(@Body RegisterModel model);

    @POST("login")
    Call<LoginResponseModel> login(@Body LoginModel model);

    @POST("logout")
    Call<Void> logout(@Header("Authorization") String authorization);

    @POST("transaction")
    Call<Void> transferMoney(@Body TransferMoneyModel model, @Header("Authorization") String authorization);

    @GET("payment_plans/{name}")
    Call<List<PaymentPlanModel>> getPaymentPlans(@Path("name") String name, @Header("Authorization") String authorization);

    @POST("payment_plan")
    Call<Void> createPaymentPlan(@Body PaymentPlanModel model, @Header("Authorization") String authorization);

    @GET("payment_plan/{id}")
    Call<PaymentPlanModel> getPaymentPlan(@Path("id") int id, @Header("Authorization") String authorization);

    @DELETE("payment_plan/{id}")
    Call<Void> deletePaymentPlan(@Path("id") int id, @Header("Authorization") String authorization);

    @GET("log/{page}")
    Call<List<LogModel>> getLog(@Path("page") int page, @Header("Authorization") String authorization);

    @GET("log/item/{id}")
    Call<LogModel> getLogItem(@Path("id") int id, @Header("Authorization") String authorization);

    @GET("version/android")
    Call<VersionModel> getVersion();

    @GET("profile_picture_id/{name}")
    Call<IntIdModel> getProfilePictureId(@Path("name") String name);
}

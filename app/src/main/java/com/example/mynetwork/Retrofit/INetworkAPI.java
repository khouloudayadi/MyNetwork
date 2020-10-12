package com.example.mynetwork.Retrofit;


import com.example.mynetwork.Model.Cell;
import com.example.mynetwork.Model.CellModel;
import com.example.mynetwork.Model.predict;
import com.google.gson.JsonObject;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface INetworkAPI {

    @Headers({"Content-Type:application/json"})
    @POST("predict")
    Observable<predict> getPredictTime(@Body JsonObject precictTime);

    @GET("getCell")
    Call<List<Cell>> getCell();


}

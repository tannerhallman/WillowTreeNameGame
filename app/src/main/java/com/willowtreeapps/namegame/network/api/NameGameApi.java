package com.willowtreeapps.namegame.network.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Retrofit HTTP API into Java interface
 */
public interface NameGameApi {

    //GET requests
    @GET("http://api.namegame.willowtreemobile.com/")
    Call<List<Person>> getPeople();
}

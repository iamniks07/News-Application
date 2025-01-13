package com.example.newsapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApiService {
    @GET("v2/top-headlines")
    Call<NewsResponse> getTopHeadlines(
            @Query("apiKey") String apiKey,
            @Query("language") String language,
            @Query("category") String category

    );

    @GET("v2/everything")
    Call<NewsResponse> getSearchedNews(
            @Query("q") String query,
            @Query("apiKey") String apiKey,
            @Query("language") String language
    );

    @GET("v2/everything")
    Call<NewsResponse> getCricketNews(
            @Query("q") String query,
            @Query("sources") String sources,
            @Query("apiKey") String apiKey,
            @Query("language") String language
    );
}

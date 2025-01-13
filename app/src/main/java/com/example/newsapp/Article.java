package com.example.newsapp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Article implements Serializable {

    @SerializedName("title")
    private String title;

    @SerializedName("source")
    private Source source;

    @SerializedName("description")
    private String description;

    @SerializedName("urlToImage")
    private String urlToImage;

    @SerializedName("url")
    private String url;

    @SerializedName("content")
    private String content;

    @SerializedName("publishedAt")
    private String publishedAt;

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrlToImage() {
        return urlToImage;
    }

    public String getUrl() {
        return url;
    }

    public String getContent() {
        return content;
    }

    public Source getSource() {
        return source;
    }

    public String getPublishedAt() {
        return publishedAt;
    }
}

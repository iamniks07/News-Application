package com.example.newsapp;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class NewsViewModel extends AndroidViewModel {

    private Repository repository;
    private LiveData<List<ArticleEntity>> allArticles;

    public NewsViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
        allArticles = repository.getGetAllArticles();
    }

    public LiveData<List<ArticleEntity>> getAllArticles() {
        return allArticles;
    }
}

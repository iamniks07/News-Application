package com.example.newsapp;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class Repository {

    private ArticleDao articleDao;
    private LiveData<List<ArticleEntity>> AllArticles;

    public Repository(Application application){
        AppDatabase db = AppDatabase.getDatabase(application);
        articleDao = db.articleDao();
        AllArticles = articleDao.getAllArticles();
    }

    public LiveData<List<ArticleEntity>> getGetAllArticles() {
        return AllArticles;
    }
}

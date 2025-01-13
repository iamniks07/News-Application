package com.example.newsapp;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ArticleDao {

    @Query("SELECT * FROM bookmarked_articles")
    LiveData<List<ArticleEntity>> getAllArticles();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertArticle(ArticleEntity articleEntity);

    @Query("SELECT * FROM bookmarked_articles WHERE url = :url LIMIT 1")
    ArticleEntity getArticleByUrl(String url);

    @Query("DELETE FROM bookmarked_articles WHERE url = :url")
    void deleteArticleByUrl(String url);

    @Query("DELETE FROM bookmarked_articles")
    void deleteAllArticles();
}

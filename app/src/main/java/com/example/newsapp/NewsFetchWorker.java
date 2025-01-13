package com.example.newsapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsFetchWorker extends Worker {
    public NewsFetchWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    private static final String API_KEY = "de747590bd1d49378602c92b43eb6a49";

    @NonNull
    @Override
    public Result doWork() {

        fetchNews();

        return Result.success();
    }

    private void fetchNews() {

        NewsApiService newsApiService = ApiClient.getClient().create(NewsApiService.class);
        Call<NewsResponse> call = newsApiService.getTopHeadlines(API_KEY,"en","General");

        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    List<Article> list = response.body().getArticles();

                    if (isNewNewsArrived(list)){
                        sendNotification(list.get(0));
                    }
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {

            }
        });

    }

    private boolean isNewNewsArrived(List<Article> list) {

        if (list == null || list.isEmpty()){
            return false;
        }

        Article latest = list.get(0);

        SharedPreferences sP = getApplicationContext().getSharedPreferences("my_prefs",Context.MODE_PRIVATE);
        String lastNews = sP.getString("Last_News","defaultNews");

        boolean isNewNews = false;

        if (lastNews == null){
            isNewNews = true;
        } else if (!latest.getTitle().equals(lastNews)) {
            isNewNews = true;
        }

        if (isNewNews){
            SharedPreferences.Editor editor = sP.edit();
            editor.putString("Last_News",latest.getTitle());
            editor.apply();
        }

        return isNewNews;
    }

    private void sendNotification(Article article) {
        NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel nc = new NotificationChannel("news_channel","News Channel",NotificationManager.IMPORTANCE_DEFAULT);
            nm.createNotificationChannel(nc);
        }


        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,PendingIntent.FLAG_MUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "news_channel")
                .setSmallIcon(R.drawable.news_paper)
                .setContentTitle("New News Available")
                .setContentText(article.getTitle())
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        nm.notify(1,builder.build());
    }
}

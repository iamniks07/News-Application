package com.example.newsapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private Context context;
    private List<Article> articleList;
    private AppDatabase db;
    private ExecutorService executorService;
    private String imageUrl = null;

    public NewsAdapter(Context context, List<Article> articleList) {
        this.context = context;
        this.articleList = articleList;
        db = AppDatabase.getDatabase(context);
        executorService = Executors.newSingleThreadExecutor();
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.news_card,parent,false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        Article article = articleList.get(position);

        holder.title.setText(article.getTitle());
        holder.source.setText(article.getSource().getName());

        if (article.getDescription() == null) {
            holder.desc.setVisibility(View.GONE);
        }
        else {
            holder.desc.setVisibility(View.VISIBLE);
            holder.desc.setText(article.getDescription());
        }
//        Picasso.get().load(article.getUrlToImage()).into(holder.imageView);
//        Glide.with(this).load(imageUrl).into(newsImageView);

//        executorService.execute(() -> {
//
//            try{
//                Document doc = Jsoup.connect(article.getUrl()).get();
//                Element imageElement = doc.select("img").first();
//
//                if (imageElement != null){
//                    imageUrl = imageElement.absUrl("src");
//                    Log.d("Image",imageUrl);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        });

        if (article.getUrlToImage() == null){
            Glide.with(holder.imageView.getContext()).load(R.drawable.load).into(holder.imageView);
        } else {
            Glide.with(holder.imageView.getContext()).load(article.getUrlToImage()).into(holder.imageView);
        }



        executorService.execute(() -> {
            boolean isBookmarked = isArticleBookmarked(article.getUrl());

            holder.bookmark.post(() -> {
//                holder.bookmark.setImageResource(isBookmarked ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
                if (isBookmarked){
                    holder.bookmark.setImageResource(android.R.drawable.btn_star_big_on);
                } else {
                    holder.bookmark.setImageResource(android.R.drawable.btn_star_big_off);
                }
            });
        });



        holder.bookmark.setOnClickListener(view -> {

            executorService.execute(()-> {
                if (isArticleBookmarked(article.getUrl())){
                    deleteArticleFromDatabase(article.getUrl());
                    holder.bookmark.setImageResource(android.R.drawable.btn_star_big_off);
                } else {
                    saveArticleInDatabase(article);
                    holder.bookmark.setImageResource(android.R.drawable.btn_star_big_on);
                }
            });
//            Log.d("Content",article.getContent());
        });

        holder.newsView.setOnClickListener(view -> {
            NewsDetailFragment newsDetailFragment = new NewsDetailFragment();

            Fragment current = ((MainActivity) context).getSupportFragmentManager().findFragmentById(R.id.frame_layout);

            Bundle bundle = new Bundle();
            bundle.putString("title",article.getTitle());
            bundle.putString("source",article.getSource().getName()+ " "+article.getPublishedAt());
            bundle.putString("content",article.getContent());
            bundle.putString("description",article.getDescription());
            bundle.putString("imageUrl",article.getUrlToImage());
            bundle.putString("url",article.getUrl());
            bundle.putSerializable("article", (Serializable) article);
            bundle.putString("fragment", String.valueOf(current));
            bundle.putString("key","article");
            newsDetailFragment.setArguments(bundle);



            ((MainActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout,newsDetailFragment)
                    .addToBackStack(null)
                    .commit();

        });
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder{

        TextView title, source, desc;
        CardView newsView;
        ImageView imageView, bookmark;
        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.article_title);
            source = itemView.findViewById(R.id.article_source);
            desc = itemView.findViewById(R.id.article_desc);
            imageView = itemView.findViewById(R.id.image);
            bookmark = itemView.findViewById(R.id.bookmark_star);

            newsView = itemView.findViewById(R.id.news);
        }

    }

    private boolean isArticleBookmarked(String url) {
        ArticleEntity articleEntity = db.articleDao().getArticleByUrl(url);
        return articleEntity != null;
    }

    private void saveArticleInDatabase(Article article) {
        ArticleEntity articleEntity = new ArticleEntity();
        articleEntity.setTitle(article.getTitle());
        articleEntity.setSourceName(article.getSource().getName());
        articleEntity.setDescription(article.getDescription());
        articleEntity.setUrlToImage(article.getUrlToImage());
        articleEntity.setUrl(article.getUrl());
        articleEntity.setContent(article.getContent());
        articleEntity.setPublishedAt(article.getPublishedAt());

        db.articleDao().insertArticle(articleEntity);
    }

    private void deleteArticleFromDatabase(String url) {
        db.articleDao().deleteArticleByUrl(url);
    }
}

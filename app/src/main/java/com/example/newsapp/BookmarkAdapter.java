package com.example.newsapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder> {

    private List<ArticleEntity> newsList = new ArrayList<>();
    private Context context;
    private ExecutorService executorService;
    private AppDatabase db;
    private String imageUrl = null;

    private ImageView imageView;
    private TextView textView;

    public BookmarkAdapter(Context context, ImageView imageView, TextView textView) {
        this.context=context;
        this.imageView=imageView;
        this.textView=textView;
        db = AppDatabase.getDatabase(context);
        executorService = Executors.newSingleThreadExecutor();
    }

    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.news_card,parent,false);
        return new BookmarkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
        ArticleEntity articleEntity = newsList.get(position);

        executorService.execute(() ->{
            boolean isBookmark = isArticleBookmarked(articleEntity.getUrl());

            if (isBookmark){
                holder.bookmark.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                holder.bookmark.setImageResource(android.R.drawable.btn_star_big_off);
            }
        });


        holder.title.setText(articleEntity.getTitle());

        if (articleEntity.getSourceName() == null || articleEntity.getSourceName().isEmpty())
            holder.source.setText("Source");
        else
            holder.source.setText(articleEntity.getSourceName());

        if (articleEntity.getDescription() == null || articleEntity.getDescription().isEmpty()){
            holder.desc.setVisibility(View.GONE);
        } else {
            holder.desc.setVisibility(View.VISIBLE);
            holder.desc.setText(articleEntity.getDescription());
        }
//        try{
//            Document doc = Jsoup.connect(articleEntity.getUrl()).get();
//            Element imageElement = doc.select("img").first();
//
//            if (imageElement != null){
//                imageUrl = imageElement.absUrl("src");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        if (articleEntity.getUrlToImage() == null)
            Picasso.get().load(R.drawable.load).into(holder.imageView);
        else
            Picasso.get().load(articleEntity.getUrlToImage()).into(holder.imageView);

        holder.bookmark.setOnClickListener(v -> {
            executorService.execute(() -> {
                if (isArticleBookmarked(articleEntity.getUrl()))
                {
                    deleteArticleFromDatabase(articleEntity.getUrl());
                    holder.bookmark.setImageResource(android.R.drawable.btn_star_big_off);
                }
            });
        });

        holder.newsView.setOnClickListener(view -> {
            NewsDetailFragment newsDetailFragment = new NewsDetailFragment();

            Fragment current = ((MainActivity) context).getSupportFragmentManager().findFragmentById(R.id.frame_layout);

            Bundle bundle = new Bundle();
            bundle.putString("title", articleEntity.getTitle());
            bundle.putString("source", articleEntity.getSourceName()+" "+articleEntity.getPublishedAt());
            bundle.putString("content", articleEntity.getContent());
            bundle.putString("description", articleEntity.getDescription());
            bundle.putString("imageUrl", articleEntity.getUrlToImage());
            bundle.putString("url", articleEntity.getUrl());
            bundle.putString("fragment",String.valueOf(current));
            bundle.putString("key", "newsArticle");
            bundle.putSerializable("newsArticle", (Serializable) articleEntity);
            newsDetailFragment.setArguments(bundle);

            ((MainActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout,newsDetailFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void deleteArticleFromDatabase(String url) {
        db.articleDao().deleteArticleByUrl(url);
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public void setNewsList(List<ArticleEntity> newsList){
        this.newsList = newsList;
        notifyDataSetChanged();

        if (newsList.isEmpty()){
            imageView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
        }else {
            imageView.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
        }
    }

    class BookmarkViewHolder extends RecyclerView.ViewHolder{

        TextView title, source, desc;
        CardView newsView;
        ImageView imageView, bookmark;
        public BookmarkViewHolder(@NonNull View itemView) {
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
}

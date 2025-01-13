package com.example.newsapp;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewsDetailFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public NewsDetailFragment() {
        // Required empty public constructor
    }

    public static NewsDetailFragment newInstance(String param1, String param2) {
        NewsDetailFragment fragment = new NewsDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private TextView newsTitle, newsSource, newsDescription, newsContent;
    private WebView webView;
    private ImageView bookmark;
    private ImageView share;
    private ImageView newsImage;
    private ImageView internet;
    private ProgressBar progressBar;

    String loadingOn;

    private Article article;
    private ArticleEntity articleEntity;
    private AppDatabase db;
    private List<Article> articleList;

    private ExecutorService executorService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_news_detail, container, false);

        webView = view.findViewById(R.id.webview);
        bookmark = view.findViewById(R.id.bookmark_star);
        internet = view.findViewById(R.id.internet);

        progressBar = view.findViewById(R.id.progressBar);

//        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
//        webView.getSettings().setAppCacheEnabled(true);
//        webView.getSettings().setAppCachePath(getCacheDir().getAbsolutePath());


        if (isNetworkAvailable()) {
            webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        }
        else {
            internet.setVisibility(View.GONE);
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }



        progressBar.setVisibility(View.VISIBLE);

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                progressBar.setVisibility(View.GONE);
//                internet.setVisibility(View.VISIBLE);
//                webView.setVisibility(View.GONE);
            }
        });

        loadingOn = getArguments().getString("fragment");
        Log.d("FragmentLoadOn",loadingOn);
//        Toast.makeText(getContext(), loadingOn, Toast.LENGTH_SHORT).show();

        String articleUrl = "";

        if (getArguments() != null){
            internet.setVisibility(View.GONE);
            articleUrl = getArguments().getString("url");
            assert articleUrl != null;

            webView.loadUrl(articleUrl);
        }

        final String Url = articleUrl;

        webView.loadUrl(Url);

        Log.d("Final String",Url);

        db = AppDatabase.getDatabase(getContext());
        executorService = Executors.newSingleThreadExecutor();

        if (getArguments().getString("key") == "article")
        {
            article = (Article) getArguments().getSerializable("article");
        } else {
            articleEntity = (ArticleEntity) getArguments().getSerializable("newsArticle");
        }

        share = view.findViewById(R.id.share);

        share.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");

            String shareText = "Check out this article: " + Url;
            shareIntent.putExtra(Intent.EXTRA_TEXT,shareText);

            getContext().startActivity(Intent.createChooser(shareIntent,"Share News Via"));
        });


//        Log.d("article",article.getUrl());

        executorService.execute(() -> {
            boolean isBookmarked = isArticleBookmarked(Url);

            if (isBookmarked){
                bookmark.setImageResource(android.R.drawable.btn_star_big_on);
            }else {
                bookmark.setImageResource(android.R.drawable.btn_star_big_off);
            }
        });

        bookmark.setOnClickListener(v -> {

            executorService.execute(() -> {
                if (isArticleBookmarked(Url)){
                    deleteArticleFromDatabase(Url);
                    bookmark.setImageResource(android.R.drawable.btn_star_big_off);
                }else {
                    saveArticleInDatabase(article);
                    bookmark.setImageResource(android.R.drawable.btn_star_big_on);
                }
            });

        });

//        newsTitle = view.findViewById(R.id.title);
//        newsSource = view.findViewById(R.id.source);
//        newsContent = view.findViewById(R.id.content);
//        newsDescription = view.findViewById(R.id.Desc);
//        newsImage = view.findViewById(R.id.imageView);
//
//        if (getArguments() != null) {
//            String title = getArguments().getString("title");
//            String source = getArguments().getString("source");
//            String content = getArguments().getString("content");
//            String description = getArguments().getString("description");
//            String imageUrl = getArguments().getString("imageUrl");
//
//            // Set the data to views
//            newsTitle.setText(title);
//            newsSource.setText(source);
//            newsContent.setText(content);
//            newsDescription.setText(description);
//            Picasso.get().load(imageUrl).into(newsImage);
////            Glide.with(this).load(imageUrl).into(newsImageView); // Load image with Glide
//        }

        if (loadingOn.contains("NewsFragment")){
            ((MainActivity) getContext()).updateBottomNavSelection(R.id.navigation_news);
        } else if (loadingOn.contains("BookmarkFragment")) {
            ((MainActivity) getContext()).updateBottomNavSelection(R.id.navigation_bookmarks);
        }


        return view;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void deleteArticleFromDatabase(String url) {
        db.articleDao().deleteArticleByUrl(url);
    }

    private void saveArticleInDatabase(Article article) {
        ArticleEntity articleEntity = new ArticleEntity();
        articleEntity.setTitle(article.getTitle());
        articleEntity.setDescription(article.getDescription());
        articleEntity.setUrlToImage(article.getUrlToImage());
        articleEntity.setUrl(article.getUrl());
        articleEntity.setContent(article.getContent());
        articleEntity.setPublishedAt(article.getPublishedAt());

        db.articleDao().insertArticle(articleEntity);
    }

    private boolean isArticleBookmarked(String articleUrl) {
        ArticleEntity articleEntity = db.articleDao().getArticleByUrl(articleUrl);
        return articleEntity != null;
    }

    public WebView getWebView() {
        return webView;
    }
}
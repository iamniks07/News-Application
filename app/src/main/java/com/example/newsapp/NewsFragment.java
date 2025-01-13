package com.example.newsapp;

import static androidx.core.content.ContextCompat.registerReceiver;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String BASE_URL = "https://inshortsapi.vercel.app/";
    private static final String API_KEY = "de747590bd1d49378602c92b43eb6a49";

    private String mParam1;
    private String mParam2;

    public NewsFragment() {
        // Required empty public constructor
    }

    private ImageView imageView,search;

    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    Button btn1,btn2,btn3,btn4,btn5,btn6,btn7;
    HorizontalScrollView horizontalScrollView;
    SearchView searchView;

    List<Article> articleList;
    private boolean wasClosed;

    private BroadcastReceiver networkReceiver;
    private Button reloadBtn;

    public static NewsFragment newInstance(String param1, String param2) {
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

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
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        recyclerView = view.findViewById(R.id.news_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        swipeRefreshLayout = view.findViewById(R.id.refresh_layout);

        imageView = view.findViewById(R.id.internet);
        search = view.findViewById(R.id.search);

        horizontalScrollView = view.findViewById(R.id.hori_button);

        btn1 = view.findViewById(R.id.b1);
        btn2 = view.findViewById(R.id.b2);
        btn3 = view.findViewById(R.id.b3);
        btn4 = view.findViewById(R.id.b4);
        btn5 = view.findViewById(R.id.b5);
        btn6 = view.findViewById(R.id.b6);
        btn7 = view.findViewById(R.id.b7);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);
        btn6.setOnClickListener(this);
        btn7.setOnClickListener(this);

        searchView = view.findViewById(R.id.searchView);
        reloadBtn = view.findViewById(R.id.reload);

        if (isNetworkAvailable())
        {
            imageView.setVisibility(View.GONE);
            reloadBtn.setVisibility(View.GONE);
            fetchNews("General");
        } else {
            horizontalScrollView.setVisibility(View.GONE);
//            Toast.makeText(getContext(), "NetworkNotAvailable from onCreateView", Toast.LENGTH_SHORT).show();
            imageView.setVisibility(View.VISIBLE);
            reloadBtn.setVisibility(View.VISIBLE);
        }

        reloadBtn.setOnClickListener(v ->{ getActivity().recreate();});

//        networkReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if (isNetworkAvailable())
//                {
//                    imageView.setVisibility(View.GONE);
//                    fetchNews("General");
//                } else {
//                    horizontalScrollView.setVisibility(View.GONE);
//                    Toast.makeText(getContext(), "NetworkNotAvailable from onCreateView", Toast.LENGTH_SHORT).show();
//                    imageView.setVisibility(View.VISIBLE);
//                }
//            }
//        };


        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchNews("General");
            searchView.setVisibility(View.GONE);
            search.setVisibility(View.VISIBLE);
        });

        fetchNewsfromsearch();

        return view;
    }

    private void fetchNewsfromsearch() {
        search.setOnClickListener(v -> {
            searchView.setVisibility(View.VISIBLE);
            search.setVisibility(View.GONE);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    swipeRefreshLayout.setEnabled(false);
                    if (s.equals("Cricket") | s.equals("cricket")){
//                        fetchCricketNews(s);
                        fetchCustomNews(s);
                    } else {
                        fetchCustomNews(s);
                    }

                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return false;
                }
            });
            swipeRefreshLayout.setEnabled(true);
        });
    }

    private void fetchCricketNews(String s) {
        swipeRefreshLayout.setRefreshing(true);
//        imageView.setVisibility(View.GONE);

        swipeRefreshLayout.setEnabled(false);

        NewsApiService newsApiService = ApiClient.getClient().create(NewsApiService.class);

        Call<NewsResponse> call = newsApiService.getCricketNews(s,"bbc-news,espn-cric-info",API_KEY,"en");
        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null){
                    List<Article> list = response.body().getArticles();
                    articleList = new ArrayList<>();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String todayDate = dateFormat.format(new Date());

                    for (Article article: list){
                        String publishedDate = article.getPublishedAt().substring(0, 10);
                        if (!"[Removed]".equalsIgnoreCase(article.getTitle()))
                        {
                            articleList.add(article);
                        }
                        Log.d("News","Title: " + article.getTitle());
                        Log.d("News","Date: "+article.getPublishedAt());
                    }
                    SetAdapter(articleList);
                    Log.d("Error",String.valueOf(response.body().getTotalResults()));
                }
                else {
                    Log.d("Error",response.toString());
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Log.e("API Error",t.getMessage());
            }
        });
    }

    private void fetchCustomNews(String s) {
        swipeRefreshLayout.setRefreshing(true);
//        imageView.setVisibility(View.GONE);

        swipeRefreshLayout.setEnabled(false);

        NewsApiService newsApiService = ApiClient.getClient().create(NewsApiService.class);

        Call<NewsResponse> call = newsApiService.getSearchedNews(s,API_KEY,"en");
        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body()!= null){
                    List<Article> list = response.body().getArticles();

                    Collections.sort(list, new Comparator<Article>() {
                        @Override
                        public int compare(Article a1, Article a2) {
                            return a2.getPublishedAt().compareTo(a1.getPublishedAt());
                        }
                    });

                    articleList = new ArrayList<>();
                    for (Article article: list){
                        if (!"[Removed]".equalsIgnoreCase(article.getTitle()))
                        {
                            articleList.add(article);
                        }
                        Log.d("News","Title: " + article.getTitle());
                    }

                    SetAdapter(articleList);
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Log.e("API Error",t.getMessage());
            }
        });

        SharedPreferences preferences = getContext().getSharedPreferences("my_prefs",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("news_load_type","Search");
        editor.putString("search_query",s);
        editor.apply();

    }

    private void fetchNews(String category) {
        swipeRefreshLayout.setRefreshing(true);
//        imageView.setVisibility(View.GONE);

        NewsApiService newsApiService = ApiClient.getClient().create(NewsApiService.class);

        Call<NewsResponse> call = newsApiService.getTopHeadlines(API_KEY,"en",category);
        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body()!=null){

                    List<Article> list = response.body().getArticles();

                    articleList = new ArrayList<>();

                    for (Article article: list){
                        if (!"[Removed]".equalsIgnoreCase(article.getTitle()))
                        {
                            articleList.add(article);
                        }

                        Log.d("News","Title: " + article.getTitle());
                    }

                    SetAdapter(articleList);
                }
                else {
                    Log.d("Response Status",response.toString());
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Log.e("API Error",t.getMessage());
            }
        });
    }

    private void SetAdapter(List<Article> articleList) {
        newsAdapter = new NewsAdapter(getContext(),articleList);
        recyclerView.setAdapter(newsAdapter);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities =
                        connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                return capabilities != null &&
                        (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
            } else {
                // For devices below Android Q (API 29)
                android.net.NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnected();
            }
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        Button b = (Button) view;
        String category = b.getText().toString();
        swipeRefreshLayout.setEnabled(true);
        searchView.setVisibility(View.GONE);
        search.setVisibility(View.VISIBLE);
        SharedPreferences preferences = getContext().getSharedPreferences("my_prefs",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor1 = preferences.edit();
        editor1.putString("news_load_type","Category");
        editor1.putString("selected_category",category);
        editor1.apply();
        fetchNews(category);

    }

//    @Override
//    public void onStop() {
//        super.onStop();
//
//        SharedPreferences preferences = getContext().getSharedPreferences("my_prefs",Context.MODE_PRIVATE);
//
//    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences preferences = getContext().getSharedPreferences("my_prefs",Context.MODE_PRIVATE);
        String savedCat = preferences.getString("selected_category","general");
        String lastQuery = preferences.getString("search_query","");
        String newsLoadType = preferences.getString("news_load_type","");


        if (newsLoadType.equals("Category")){
            searchView.setVisibility(View.GONE);
            fetchNews(savedCat);
        } else if (newsLoadType.equals("Search")) {
            searchView.setVisibility(View.VISIBLE);
            searchView.setEnabled(true);
            search.setVisibility(View.GONE);
            searchView.getQueryHint();
            fetchCustomNews(lastQuery);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    fetchCustomNews(s);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return false;
                }
            });
        } else {
            if (isNetworkAvailable())
            {
//                searchView.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                fetchNews("General");
            } else {
                horizontalScrollView.setVisibility(View.GONE);
//                Toast.makeText(getContext(), "NetworkNotAvailable from Resume method", Toast.LENGTH_SHORT).show();
                imageView.setVisibility(View.VISIBLE);
            }
        }
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//
//        SharedPreferences preferences = getContext().getSharedPreferences("my_prefs",Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.clear();
//        editor.apply();
//    }
}

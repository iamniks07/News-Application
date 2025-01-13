package com.example.newsapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class BookmarkFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public BookmarkFragment() {
        // Required empty public constructor
    }

    private NewsViewModel newsViewModel;
    private BookmarkAdapter bookmarkAdapter;
    private RecyclerView recyclerView;

    List<ArticleEntity> list;
    private ImageView imageView;
    private TextView textView;

    public static BookmarkFragment newInstance(String param1, String param2) {
        BookmarkFragment fragment = new BookmarkFragment();
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
        View view =  inflater.inflate(R.layout.fragment_bookmark, container, false);

        imageView = view.findViewById(R.id.empty);
        imageView.setVisibility(View.GONE);

        textView = view.findViewById(R.id.book_info);
        textView.setVisibility(View.GONE);

        recyclerView = view.findViewById(R.id.bookmark_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        bookmarkAdapter = new BookmarkAdapter(getContext(),imageView,textView);
        recyclerView.setAdapter(bookmarkAdapter);

        list = new ArrayList<>();

        newsViewModel = new ViewModelProvider(this).get(NewsViewModel.class);


        newsViewModel.getAllArticles().observe(getViewLifecycleOwner(), articles -> {
            bookmarkAdapter.setNewsList(articles);
        });

        return view;
    }


}
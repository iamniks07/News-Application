package com.example.newsapp;

import android.app.AlertDialog;
import android.app.TaskStackBuilder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.newsapp.databinding.ActivityMainBinding;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding =ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout,new NewsFragment())
                    .commit();
        }

        clickListener();
        scheduleNews();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    showExitDialog();
                }

//                showExitDialog();
            }
        });
    }

    private void scheduleNews() {
        PeriodicWorkRequest newsFetchRequest = new PeriodicWorkRequest.Builder(NewsFetchWorker.class,15, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "newsFetch",
                ExistingPeriodicWorkPolicy.REPLACE,
                newsFetchRequest);
    }

    private void clickListener() {
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectFragment = null;
            switch (item.getItemId()){
                case R.id.navigation_news:
                    selectFragment = new NewsFragment();
//                    setFragment(new NewsFragment());
                    break;

                case R.id.navigation_bookmarks:
                    selectFragment = new BookmarkFragment();
//                    setFragment(new BookmarkFragment());
                    break;
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout,selectFragment)
                    .addToBackStack(null)
                    .commit();

            return true;
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
            if (currentFragment instanceof NewsFragment){
                binding.bottomNavigationView.getMenu().findItem(R.id.navigation_news).setChecked(true);
            } else if (currentFragment instanceof BookmarkFragment){
                binding.bottomNavigationView.getMenu().findItem(R.id.navigation_bookmarks).setChecked(true);
            }
        });
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                        SharedPreferences preferences = getSharedPreferences("my_prefs",MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.apply();
                    }
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences preferences = getSharedPreferences("my_prefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }


//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//
//        SharedPreferences preferences = getSharedPreferences("my_prefs",MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.clear();
//        editor.apply();
//    }

    public void updateBottomNavSelection(int navigationBookmarks) {
        binding.bottomNavigationView.getMenu().findItem(navigationBookmarks).setChecked(true);
    }

}
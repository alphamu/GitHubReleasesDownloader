package com.alimuzaffar.demo.githubreleasedownloader;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.List;

public class MainActivity extends AppCompatActivity implements StoragePermissionHelper.StoragePermissionCallback {
    private ReleaseAdapter mAdapter;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        try {
            getReleases();
        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }
        StoragePermissionHelper.attach(this, this);
    }

    public void getReleases() throws Exception {
        Ion.with(this)
                .load(getString(R.string.github_releases_url))
                .setHeader("Authorization", getString(R.string.github_oauth_token))
                //.setHeader("Accept", "application/octet-stream")
                .as(new TypeToken<List<Release>>(){})
                .setCallback(new FutureCallback<List<Release>>() {
                    @Override
                    public void onCompleted(Exception e, List<Release> releases) {
                        if (e == null) {
                            mAdapter = new ReleaseAdapter(MainActivity.this, releases);
                            mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                            mRecyclerView.setHasFixedSize(true);
                            mRecyclerView.setAdapter(mAdapter);
                        } else {
                            Snackbar.make(mRecyclerView, e.getMessage(), Snackbar.LENGTH_LONG).show();
                            Log.e("MainActivity", e.getMessage(), e);
                        }
                    }
                });
    }

    @Override
    public void onStoragePermissionGranted() {

    }

    @Override
    public void onStoragePermissionDenied() {

    }
}

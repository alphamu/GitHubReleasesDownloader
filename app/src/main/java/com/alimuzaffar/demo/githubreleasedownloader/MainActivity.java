package com.alimuzaffar.demo.githubreleasedownloader;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.alimuzaffar.demo.githubreleasedownloader.fragment.StoragePermissionHelper;
import com.alimuzaffar.demo.githubreleasedownloader.model.Release;
import com.alimuzaffar.demo.githubreleasedownloader.network.GitHubService;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@SuppressWarnings("FieldCanBeLocal")
public class MainActivity extends AppCompatActivity implements StoragePermissionHelper.StoragePermissionCallback {
    private final String TAG = getClass().getSimpleName();
    private ReleaseAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ProgressDialog progressBar;
    OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request original = chain.request();

            Request request = original.newBuilder()
                    .header("Authorization", getString(R.string.github_oauth_token))
                    .build();

            return chain.proceed(request);
        }
    }).build();
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build();
    GitHubService service = retrofit.create(GitHubService.class);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        try {
            getReleases();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        StoragePermissionHelper.attach(this, this);
    }

    public void getReleases() throws IOException {
        Call<List<Release>> call = service.listReleases();
        call.enqueue(new Callback<List<Release>>() {
            @Override
            public void onResponse(Call<List<Release>> call, Response<List<Release>> response) {
                if (response.isSuccessful()) {
                    buildReleases(null, response.body());
                } else {
                    Snackbar.make(mRecyclerView, response.errorBody().toString(), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Release>> call, Throwable t) {
                buildReleases(t, null);
            }
        });
    }

    private void buildReleases(Throwable e, List<Release> releases) {
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

    void downloadApk(String url, final String releaseName, final String assetName, final String mimeType) {
        progressBar = new ProgressDialog(MainActivity.this);
        progressBar.setIndeterminate(false);
        progressBar.setTitle("Downloading\n" + releaseName + " " + assetName);
        progressBar.setMessage("Message");
        progressBar.setMax(100);
        progressBar.setProgress(0);
        progressBar.setIndeterminate(false);
        progressBar.show();

        Call<ResponseBody> call = service.downloadApk(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {

                Log.d(TAG, response.message());
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Something's gone wrong");
                    Snackbar.make(mRecyclerView, "Something's gone wrong", Snackbar.LENGTH_LONG).show();
                    return;
                }
                DownloadFileAsyncTask downloadFileAsyncTask = new DownloadFileAsyncTask(releaseName, assetName, mimeType) {
                    @Override
                    void onError(String message) {
                        Snackbar.make(mRecyclerView, message, Snackbar.LENGTH_LONG).show();
                        progressBar.dismiss();
                    }

                    @Override
                    void onFinished(File fileName) {
                        progressBar.dismiss();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Intent install = new Intent(Intent.ACTION_VIEW);
                            install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            Uri apkURI = FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName() + ".provider", filename);
                            install.setDataAndType(apkURI, mimeType);
                            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            MainActivity.this.startActivity(install);
                        } else {
                            Uri apkUri = Uri.fromFile(fileName);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(apkUri, mimeType);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            MainActivity.this.startActivity(intent);
                        }

                    }

                    @Override
                    protected void onProgressUpdate(Long... values) {
                        float d = values[1];
                        float t = values[0];
                        long downloaded = values[1];
                        long total = values[0];
                        progressBar.setMessage(downloaded + " / " + total + " - " + (int) ((d / t) * 100) + "%");
                    }
                };
                downloadFileAsyncTask.execute(response.body());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "error", t);
                Snackbar.make(mRecyclerView, t.getMessage(), Snackbar.LENGTH_LONG).show();
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

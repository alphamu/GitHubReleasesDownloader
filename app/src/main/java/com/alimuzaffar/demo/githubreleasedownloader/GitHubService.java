package com.alimuzaffar.demo.githubreleasedownloader;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface GitHubService {
    @GET("/repos/QLPD/pd-droid/releases")
    Call<List<Release>> listReleases();

    @Streaming
    @Headers("Accept: application/octet-stream")
    @GET
    Call<ResponseBody> downloadApk(@Url String fileUrl);
}

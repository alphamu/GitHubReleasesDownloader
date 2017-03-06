package com.alimuzaffar.demo.githubreleasedownloader.model;

import android.text.TextUtils;

import java.util.ArrayList;

public class Release {
    private ArrayList<Asset> assets;
    private String tag_name;
    private String name;
    private String created_at;
    private String published_at;

    public ArrayList<Asset> getAssets() {
        return assets;
    }

    public String getName() {
        if (TextUtils.isEmpty(name)) {
            return tag_name;
        }
        return name;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getPublished_at() {
        return published_at;
    }
}

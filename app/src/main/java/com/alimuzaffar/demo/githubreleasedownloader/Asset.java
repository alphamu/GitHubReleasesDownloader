package com.alimuzaffar.demo.githubreleasedownloader;

public class Asset {
    private String url;
    private String content_type;
    private String name;
    private String releaseName;
    private long size;

    public String getUrl() {
        return url;
    }

    public String getContent_type() {
        return content_type;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }
}

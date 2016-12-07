package com.alimuzaffar.demo.githubreleasedownloader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReleaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
    private static final int TYPE_RELEASE = 0;
    private static final int TYPE_ASSET = 1;

    private ArrayList<Object> mCombinedData = new ArrayList<>(100);
    private List<Release> mReleases = null;
    private File downloadsDir = new File(Environment.getExternalStorageDirectory(), "Download");
    private Activity mActivity = null;

    public ReleaseAdapter(Activity activity, List<Release> releases) {
        mReleases = releases;
        mActivity = activity;
        updateDataSet(releases);
    }

    @Override
    public int getItemCount() {
        return mCombinedData.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object o = mCombinedData.get(position);
        if (o instanceof Release) {
            return TYPE_RELEASE;
        } else {
            return TYPE_ASSET;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object o = mCombinedData.get(position);
        if (holder instanceof ViewHolderRelease) {
            Release r = (Release) o;
            ViewHolderRelease h = (ViewHolderRelease) holder;
            h.txtReleaseName.setText(r.getName());
            h.txtReleaseDate.setText(r.getPublished_at().replace("T", "\n").replace("Z", ""));
        } else {
            Asset a = (Asset) o;
            ViewHolderAsset h = (ViewHolderAsset) holder;
            h.txtAsset.setText(a.getName());
            h.viewAsset.setTag(R.id.backing_url, a.getUrl());
            h.viewAsset.setTag(R.id.asset_name, a.getName());
            h.viewAsset.setTag(R.id.release_name, a.getReleaseName());
            h.viewAsset.setTag(R.id.mime_type, a.getContent_type());
            h.viewAsset.setOnClickListener(this);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Change inflated layout based on 'header'.
        View v;
        if (viewType == TYPE_RELEASE) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_release, parent, false);
            return new ViewHolderRelease(v);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_asset, parent, false);
            return new ViewHolderAsset(v);
        }

    }

    public void updateDataSet(List<Release> releases) {
        mCombinedData.clear();

        for (Release release : releases) {
            mCombinedData.add(release);
            for (Asset asset : release.getAssets()) {
                mCombinedData.add(asset);
                asset.setReleaseName(release.getName());
            }
        }
        notifyDataSetChanged();
    }


    @Override
    public void onClick(View v) {
        downloadFile(v);
    }

    private void downloadFile(final View view) {
        Boolean downloading = (Boolean) view.getTag(R.id.downloading);
        if (downloading != null && downloading) {
            return;
        }
        view.setTag(R.id.downloading, true);

        final Context context = view.getContext().getApplicationContext();
        String assetName = (String) view.getTag(R.id.asset_name);
        String releaseName = (String) view.getTag(R.id.release_name);
        String downloadUrl = (String) view.getTag(R.id.backing_url);
        Log.d("Adapter", "Download URL " + downloadUrl);
        final String mimeType = (String) view.getTag(R.id.mime_type);
        File releaseDir = new File(downloadsDir, releaseName.replace("/", "_"));
        if (!releaseDir.exists()) {
            releaseDir.mkdir();
        }
        File downloadFile = new File(releaseDir, assetName);
        if (downloadFile.exists()) {
            downloadFile.delete();
        }
        final ProgressDialog progressBar = new ProgressDialog(view.getContext());
        progressBar.setIndeterminate(false);
        progressBar.setTitle("Downloading\n" + releaseName + " " + assetName);
        progressBar.setMessage("Message");
        progressBar.setMax(100);
        progressBar.setProgress(0);
        progressBar.setIndeterminate(false);
        progressBar.show();

        Ion.with(context)
                .load(downloadUrl)
                .followRedirect(true)
                .setHeader("Authorization", context.getString(R.string.github_oauth_token))
                .setHeader("Accept", "application/octet-stream")
                // have a ProgressBar get updated automatically with the percent
                .progressDialog(progressBar)
                .progress(new ProgressCallback() {
                    @Override
                    public void onProgress(final long downloaded, final long total) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                float d = downloaded;
                                float t = total;
                                progressBar.setMessage(downloaded + " / " + total + " - " + (int) ((d/t) * 100) + "%");
                            }
                        });
                    }
                })
                .write(downloadFile)
                .setCallback(new FutureCallback<File>() {
                    @Override
                    public void onCompleted(Exception e, File file) {
                        if (progressBar.isShowing()) {
                            progressBar.dismiss();
                        }
                        view.setTag(R.id.downloading, false);
                        if (e == null ) {
                            Intent install = new Intent(Intent.ACTION_VIEW);
                            install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                                install.setDataAndType(Uri.fromFile(file), mimeType);
                            } else {
                                Uri apkURI = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
                                install.setDataAndType(apkURI, mimeType);
                                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            }
                            context.startActivity(install);
                        } else {
                            Log.e("Adapter", e.getMessage(), e);
                            Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private void startDownload() {

    }

    public int getHeaderCount() {
        if (mReleases == null) {
            return 0;
        }
        return mReleases.size();
    }

    public void clear() {
        mCombinedData.clear();
        notifyDataSetChanged();
    }


    private class ViewHolderRelease extends RecyclerView.ViewHolder {
        private TextView txtReleaseName;
        private TextView txtReleaseDate;

        ViewHolderRelease(View view) {
            super(view);
            txtReleaseName = (TextView) view.findViewById(R.id.txtReleaseName);
            txtReleaseDate = (TextView) view.findViewById(R.id.txtReleaseDate);
        }
    }

    class ViewHolderAsset extends RecyclerView.ViewHolder {
        private TextView txtAsset;
        private View viewAsset;
        private ProgressBar progressBar;

        ViewHolderAsset(View view) {
            super(view);
            viewAsset = view.findViewById(R.id.viewAsset);
            txtAsset = (TextView) view.findViewById(R.id.txtAsset);
            progressBar = (ProgressBar) view.findViewById(R.id.progress);
        }
    }

}
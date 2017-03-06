package com.alimuzaffar.demo.githubreleasedownloader;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alimuzaffar.demo.githubreleasedownloader.model.Asset;
import com.alimuzaffar.demo.githubreleasedownloader.model.Release;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class ReleaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
    private static final int TYPE_RELEASE = 0;
    private static final int TYPE_ASSET = 1;

    private ArrayList<Object> mCombinedData = new ArrayList<>(100);
    private List<Release> mReleases = null;
    private MainActivity mActivity = null;

    public ReleaseAdapter(MainActivity activity, List<Release> releases) {
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
        Log.d("ReleaseAdapter", position+"");
        if (holder instanceof ViewHolderRelease) {
            Release r = (Release) o;
            ViewHolderRelease h = (ViewHolderRelease) holder;
            h.txtReleaseName.setText(r.getName());
            h.txtReleaseDate.setText(r.getCreated_at().replace("T", "\n").replace("Z", ""));
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

        String assetName = (String) view.getTag(R.id.asset_name);
        String releaseName = (String) view.getTag(R.id.release_name);
        String downloadUrl = (String) view.getTag(R.id.backing_url);
        Log.d("Adapter", "Download URL " + downloadUrl);
        final String mimeType = (String) view.getTag(R.id.mime_type);
        mActivity.downloadApk(downloadUrl,
                releaseName.replace("/", "_"),
                assetName,
                mimeType);
    }

    @SuppressWarnings("unused")
    public int getHeaderCount() {
        if (mReleases == null) {
            return 0;
        }
        return mReleases.size();
    }

    @SuppressWarnings("unused")
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
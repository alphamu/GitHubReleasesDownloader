package com.alimuzaffar.demo.githubreleasedownloader;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;

import static android.content.ContentValues.TAG;

@SuppressWarnings({"WeakerAccess", "FieldCanBeLocal"})
public abstract class DownloadFileAsyncTask extends AsyncTask<ResponseBody, Long, Boolean> {
    private File downloadsDir = new File(Environment.getExternalStorageDirectory(), "Download");
    protected File filename;
    protected String mimeType;
    final File releaseDir;
    final String releaseName;
    final String assetName;

    public DownloadFileAsyncTask(String releaseName, String assetName, String mimeType) {
        this.releaseName = releaseName;
        this.assetName = assetName;
        this.mimeType = mimeType;
        releaseDir = new File(downloadsDir, releaseName);
        filename = new File(releaseDir, assetName);
    }

    @Override
    protected Boolean doInBackground(ResponseBody... params) {
        if (!releaseDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            releaseDir.mkdirs();
        }

        if (filename.exists()) {
            return true;
        }

        InputStream inputStream = params[0].byteStream();
        OutputStream output = null;
        try {
            output = new FileOutputStream(filename);

            byte[] buffer = new byte[1024]; // or other buffer size
            int read;
            long total = params[0].contentLength();
            long downloaded = 0;

            Log.d(TAG, "Attempting to write to: " + filename.getAbsolutePath());
            while ((read = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
                Log.v(TAG, "Writing to buffer to output stream.");
                downloaded += buffer.length;
                publishProgress(total, downloaded);
            }
            Log.d(TAG, "Flushing output stream.");
            output.flush();
            Log.d(TAG, "Output flushed.");
            return true;
        } catch (IOException e) {
            Log.e(TAG, "IO Exception: " + e.getMessage(), e);
            onError(e.getMessage());
            return false;
        } finally {
            try {
                if (output != null) {
                    output.close();
                    Log.d(TAG, "Output stream closed sucessfully.");
                } else {
                    Log.d(TAG, "Output stream is null");
                }
            } catch (IOException e) {
                Log.e(TAG, "Couldn't close output stream: " + e.getMessage());
                e.printStackTrace();
                onError(e.getMessage());
            }
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        Log.d(TAG, "Download success: " + result);
        if (result)
            onFinished(filename);
    }

    abstract void onError(String message);

    abstract void onFinished(File filename);
}
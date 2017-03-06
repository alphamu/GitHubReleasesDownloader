package com.alimuzaffar.demo.githubreleasedownloader.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class StoragePermissionHelper extends Fragment {
    private static final int REQUEST_STORAGE_PERMISSIONS = 10;
    public static final String TAG = "StoragePerm";

    private StoragePermissionCallback mCallback;
    private static boolean sStoragePermissionDenied;

    public static StoragePermissionHelper newInstance() {
        return new StoragePermissionHelper();
    }

    public StoragePermissionHelper() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof StoragePermissionCallback && mCallback == null) {
            mCallback = (StoragePermissionCallback) context;
        } else if (mCallback == null){
            throw new IllegalArgumentException("activity must extend BaseActivity or StoragePermissionCallback");
        }
        checkStoragePermissions();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    public void setCallback(StoragePermissionCallback callback) {
        mCallback = callback;
    }

    public void checkStoragePermissions() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            mCallback.onStoragePermissionGranted();
        } else {
            // UNCOMMENT TO SUPPORT ANDROID M RUNTIME PERMISSIONS
            if (!sStoragePermissionDenied) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSIONS);
            }
        }
    }

    @SuppressWarnings("unused")
    public boolean hasStoragePermission() {
        return ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
    @SuppressWarnings("unused")
    public void setStoragePermissionDenied(boolean cameraMicPermissionDenied) {
        sStoragePermissionDenied = cameraMicPermissionDenied;
    }
    @SuppressWarnings("unused")
    public static boolean isStoragePermissionDenied() {
        return sStoragePermissionDenied;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_STORAGE_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mCallback.onStoragePermissionGranted();
            } else {
                Log.i("BaseActivity", "STORAGE group permission was NOT granted.");
                mCallback.onStoragePermissionDenied();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public static StoragePermissionHelper attach(FragmentActivity activity, StoragePermissionCallback callback) {
        StoragePermissionHelper headlessFrag = (StoragePermissionHelper) activity.getSupportFragmentManager()
                .findFragmentByTag(StoragePermissionHelper.TAG);

        if (headlessFrag == null) {
            headlessFrag = StoragePermissionHelper.newInstance();
            activity.getSupportFragmentManager().beginTransaction()
                    .add(headlessFrag, StoragePermissionHelper.TAG)
                    .commit();
            headlessFrag.setCallback(callback);
        } else {
            headlessFrag.setCallback(callback);
            headlessFrag.checkStoragePermissions();
        }

        return headlessFrag;
    }

    public static void detach(FragmentActivity activity) {
        StoragePermissionHelper headlessFrag = (StoragePermissionHelper) activity.getSupportFragmentManager()
                .findFragmentByTag(StoragePermissionHelper.TAG);

        if (headlessFrag != null) {
            headlessFrag = StoragePermissionHelper.newInstance();
            activity.getSupportFragmentManager().beginTransaction()
                    .remove(headlessFrag)
                    .commitAllowingStateLoss();
        }

    }

    public interface StoragePermissionCallback {
        void onStoragePermissionGranted();

        void onStoragePermissionDenied();
    }

}
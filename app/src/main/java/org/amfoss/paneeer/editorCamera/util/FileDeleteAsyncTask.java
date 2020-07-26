package org.amfoss.paneeer.editorCamera.util;

import android.os.AsyncTask;

import java.io.File;

public class FileDeleteAsyncTask extends AsyncTask<Void, Void, Object> {

    private File mDirectory;
    private OnAsyncFileDeleteListener mOnAsyncFileDeleteListener;

    public FileDeleteAsyncTask(File directory, OnAsyncFileDeleteListener onAsyncFileDeleteListener) {
        mDirectory = directory;
        mOnAsyncFileDeleteListener = onAsyncFileDeleteListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Void... voids) {
        if (mDirectory != null && mDirectory.exists() && mDirectory.listFiles() != null) {
            for (File childFile : mDirectory.listFiles()) {
                if (childFile != null) {
                    if (childFile.isDirectory()) {
                        deleteDirectory(childFile);
                    } else {
                        childFile.delete();
                    }
                }
            }
            mDirectory.delete();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object result) {
        if (mOnAsyncFileDeleteListener != null) {
            mOnAsyncFileDeleteListener.processFinish(result);
        }
    }

    private void deleteDirectory(File directory) {
        if (directory != null && directory.exists() && directory.listFiles() != null) {
            for (File childFile : directory.listFiles()) {
                if (childFile != null) {
                    if (childFile.isDirectory()) {
                        deleteDirectory(childFile);
                    }
                    else {
                        childFile.delete();
                    }
                }
            }
        }
    }

    public interface OnAsyncFileDeleteListener {
        void processFinish(Object result);
    }
}

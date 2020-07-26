package org.amfoss.paneeer.editorCamera.network;

import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadAsyncTask extends AsyncTask<Integer, Integer, Boolean> {

    private String targetPath;
    private String achiveUrl;

    private DownloadAsyncResponse responseListener;


    public DownloadAsyncTask(String targetPath, String url, DownloadAsyncResponse responseListener) {
        this.targetPath = targetPath;
        this.achiveUrl = url;
        this.responseListener = responseListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Integer... params) {
        boolean success = false;
        FileOutputStream fileOutput = null;
        InputStream inputStream = null;

        try {
            URL url = new URL(achiveUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.connect();

            File file = new File(targetPath);
            file.createNewFile();

            fileOutput = new FileOutputStream(file);

            inputStream = urlConnection.getInputStream();

            byte[] buffer = new byte[1024];
            int bufferLength;
            int read = 0;
            int total = urlConnection.getContentLength();
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
                read += bufferLength;
                if (total > 0) {
                    int progress = (int) (100 * (read / (float) total));
                    publishProgress(progress);
                }
            }

            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileOutput != null) {
                try {
                    fileOutput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return success;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {

    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (responseListener != null) {
            responseListener.processFinish(result);
        }
    }
}
package org.amfoss.paneeer.editorCamera.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.Nullable;

import org.amfoss.paneeer.R;


public class PlayerActivity extends Activity {

    public static String INTENT_URI = "player_uri";

    private VideoView mVideoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        String uriString = getIntent().getStringExtra(INTENT_URI);

        setContentView(R.layout.activity_player);

        mVideoView = (VideoView) findViewById(R.id.video_view);

        //Creating MediaController
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(mVideoView);

        //specify the location of media file
        Uri uri = Uri.parse(uriString);

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        //Setting MediaController and URI, then starting the videoView
        mVideoView.setMediaController(mediaController);
        mVideoView.setVideoURI(uri);
        mVideoView.requestFocus();
        mVideoView.start();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(mVideoView != null && mVideoView.isPlaying()) {
            mVideoView.pause();
        }
    }


    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoView != null && !mVideoView.isPlaying()) {
            mVideoView.resume();
            mVideoView.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

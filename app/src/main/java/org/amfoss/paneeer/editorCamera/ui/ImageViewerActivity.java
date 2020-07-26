package org.amfoss.paneeer.editorCamera.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import org.amfoss.paneeer.R;


public class ImageViewerActivity extends Activity {

    public static String INTENT_IMAGE_URI = "image_uri";

    private ImageView mImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        String uriString = getIntent().getStringExtra(INTENT_IMAGE_URI);

        setContentView(R.layout.activity_image);

        mImageView = (ImageView) findViewById(R.id.image_viewer);
        mImageView.setImageURI(Uri.parse(uriString));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

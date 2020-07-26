package org.amfoss.paneeer.base;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.amfoss.paneeer.ARMainActivity;
import org.amfoss.paneeer.R;
import org.amfoss.paneeer.editorCamera.ui.EditorCameraActivity;
import org.amfoss.paneeer.gallery.activities.GalleryMainActivity;
import org.amfoss.paneeer.gallery.util.PreferenceUtil;
import org.amfoss.paneeer.opencamera.Camera.CameraActivity;

public abstract class BaseActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    protected BottomNavigationView navigationView;
    BottomNavigationItemView nav_home;
    BottomNavigationItemView nav_cam;
    private PreferenceUtil SP;
    private boolean isSWNavBarChecked;

    private int[][] states =
            new int[][]{
                    new int[]{android.R.attr.state_checked}, // checked
                    new int[]{-android.R.attr.state_checked}, // unchecked
            };

    private int[] colors =
            new int[]{
                    Color.WHITE, // checked
                    0 // unchecked set default in onCreate
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());
        colors[1] = ContextCompat.getColor(this, R.color.bottom_navigation_tabs);
        ColorStateList myList = new ColorStateList(states, colors);
        navigationView = findViewById(R.id.bottombar);
        navigationView.setItemIconTintList(myList);
        navigationView.setItemTextColor(myList);
        navigationView.setOnNavigationItemSelectedListener(this);

        nav_home = findViewById(R.id.home);
        nav_cam = findViewById(R.id.camera);

        SP = PreferenceUtil.getInstance(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        isSWNavBarChecked = SP.getBoolean(getString(R.string.preference_colored_nav_bar), true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }

    // Remove inter-activity transition to avoid screen tossing on tapping bottom navigation items
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() != getNavigationMenuItemId()) {
            switch (item.getItemId()) {
                case R.id.camera:
                    Intent cameraIntent = new Intent(BaseActivity.this, CameraActivity.class);
                    startActivity(cameraIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    break;
                case R.id.edcamera:
                    Intent edCameraIntent = new Intent(BaseActivity.this, EditorCameraActivity.class);
                    startActivity(edCameraIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    break;
                case R.id.arcamera:
                    Intent arCameraIntent = new Intent(BaseActivity.this, ARMainActivity.class);
                    startActivity(arCameraIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    break;
                case R.id.home:
                    Intent homeIntent = new Intent(this, GalleryMainActivity.class);
                    startActivity(homeIntent);
                    break;
            }
            finish();
            overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
        }
        return true;
    }

    private void updateNavigationBarState() {
        int actionId = getNavigationMenuItemId();
        selectBottomNavigationBarItem(actionId);
    }

    void selectBottomNavigationBarItem(int itemId) {
        Menu menu = navigationView.getMenu();
        for (int i = 0, size = menu.size(); i < size; i++) {
            MenuItem item = menu.getItem(i);
            boolean shouldBeChecked = item.getItemId() == itemId;
            if (shouldBeChecked) {
                item.setChecked(true);
                break;
            }
        }
    }

    void setIconColor(int color) {
        if (Color.red(color) + Color.green(color) + Color.blue(color) < 300)
            colors[0] = Color.WHITE;
        else colors[0] = Color.BLACK;
    }

    public abstract int getContentViewId();

    public abstract int getNavigationMenuItemId();

    public void setNavigationBarColor(int color) {
        if (isSWNavBarChecked) {
            navigationView.setBackgroundColor(color);
            SP.putInt(getString(R.string.preference_BottomNavColor), color);
        } else {
            navigationView.setBackgroundColor(
                    SP.getInt(getString(R.string.preference_BottomNavColor), color));
        }
        setIconColor(color);
    }

    /**
     * Animate bottom navigation bar from GONE to VISIBLE
     */
    public void showNavigationBar() {
        navigationView
                .animate()
                .translationY(0)
                .alpha(1.0f)
                .setDuration(400)
                .setListener(
                        new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                navigationView.setVisibility(View.VISIBLE);
                            }
                        });
    }

    /**
     * Animate bottom navigation bar from VISIBLE to GONE
     */
    public void hideNavigationBar() {
        navigationView
                .animate()
                .alpha(0.0f)
                .translationYBy(navigationView.getHeight())
                .setDuration(400)
                .setListener(
                        new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                navigationView.setVisibility(View.GONE);
                            }
                        });
    }
}

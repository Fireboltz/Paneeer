package org.amfoss.paneeer.gallery.activities;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.ChangeBounds;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.print.PrintHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.yalantis.ucrop.UCrop;

import org.amfoss.paneeer.R;
import org.amfoss.paneeer.base.SharedMediaActivity;
import org.amfoss.paneeer.base.ThemedActivity;
import org.amfoss.paneeer.data.local.DatabaseHelper;
import org.amfoss.paneeer.data.local.FavouriteImagesModel;
import org.amfoss.paneeer.data.local.ImageDescModel;
import org.amfoss.paneeer.data.local.TrashBinRealmModel;
import org.amfoss.paneeer.editor.CompressImageActivity;
import org.amfoss.paneeer.editor.EditImageActivity;
import org.amfoss.paneeer.editor.FileUtils;
import org.amfoss.paneeer.editor.utils.BitmapUtils;
import org.amfoss.paneeer.gallery.SelectAlbumBottomSheet;
import org.amfoss.paneeer.gallery.adapters.ImageAdapter;
import org.amfoss.paneeer.gallery.data.Album;
import org.amfoss.paneeer.gallery.data.AlbumSettings;
import org.amfoss.paneeer.gallery.data.Media;
import org.amfoss.paneeer.gallery.util.AlertDialogsHelper;
import org.amfoss.paneeer.gallery.util.ColorPalette;
import org.amfoss.paneeer.gallery.util.ContentHelper;
import org.amfoss.paneeer.gallery.util.Measure;
import org.amfoss.paneeer.gallery.util.PreferenceUtil;
import org.amfoss.paneeer.gallery.util.StringUtils;
import org.amfoss.paneeer.gallery.util.ThemeHelper;
import org.amfoss.paneeer.gallery.views.PagerRecyclerView;
import org.amfoss.paneeer.trashbin.TrashBinActivity;
import org.amfoss.paneeer.utilities.ActivitySwitchHelper;
import org.amfoss.paneeer.utilities.BasicCallBack;
import org.amfoss.paneeer.utilities.SnackBarHandler;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import uk.co.senab.photoview.PhotoViewAttacher;

import static org.amfoss.paneeer.gallery.activities.GalleryMainActivity.listAll;
import static org.amfoss.paneeer.utilities.Utils.promptSpeechInput;

@SuppressWarnings("ResourceAsColor")
public class SingleMediaActivity extends SharedMediaActivity
    implements ImageAdapter.OnSingleTap, ImageAdapter.enterTransition {

  private static int SLIDE_SHOW_INTERVAL = 5000;
  static final String ACTION_OPEN_ALBUM = "android.intent.action.pagerAlbumMedia";
  private static final String ACTION_REVIEW = "com.android.camera.action.REVIEW";
  private int REQUEST_CODE_SD_CARD_PERMISSIONS = 42;
  private ImageAdapter adapter;
  private PreferenceUtil SP;
  private RelativeLayout ActivityBackground;
  private SelectAlbumBottomSheet bottomSheetDialogFragment;
  private boolean fullScreenMode, customUri = false;
  public static final int ACTION_REQUEST_EDITIMAGE = 9;
  private Bitmap mainBitmap;
  private int imageWidth, imageHeight;
  private SingleMediaActivity context;
  public static final String EXTRA_OUTPUT = "extra_output";
  public static String pathForDescription;
  public Boolean allPhotoMode;
  public Boolean favphotomode;
  public Boolean upoadhis;
  private Boolean trashdis;
  public int all_photo_pos;
  public int size_all;
  public int current_image_pos;
  private Uri uri;
  private Realm realm;
  private FavouriteImagesModel fav;
  private DatabaseHelper databaseHelper;
  private Handler handler;
  private Runnable runnable;
  boolean slideshow = false;
  private boolean details = false;
  private ArrayList<Media> favouriteslist;
  public static Media mediacompress = null;
  private Menu menu;

  private ArrayList<Media> uploadhistory;
  private ArrayList<Media> trashbinlistd;

  ImageDescModel temp;
  private final int REQ_CODE_SPEECH_INPUT = 100;
  String voiceInput;
  EditText editTextDescription;
  private RelativeLayout relativeLayout;

  ViewSwitcher viewSwitcher;
  View parentView;
  ActionMenuView bottomBar;
  ImageView imgView;
  PagerRecyclerView mViewPager;
  Toolbar toolbar;

  Runnable slideShowRunnable =
      new Runnable() {
        @Override
        public void run() {
          try {
            if (!allPhotoMode && !favphotomode) {
              mViewPager.scrollToPosition(
                  (getAlbum().getCurrentMediaIndex() + 1) % getAlbum().getMedia().size());
            } else if (allPhotoMode && !favphotomode) {
              mViewPager.scrollToPosition((current_image_pos + 1) % listAll.size());
            } else if (favphotomode && !allPhotoMode) {
              mViewPager.scrollToPosition((current_image_pos + 1) % favouriteslist.size());
            }
          } catch (Exception e) {
            e.printStackTrace();
          } finally {
            if (getAlbum().getCurrentMediaIndex() + 1 == getAlbum().getMedia().size() - 1) {
              handler.removeCallbacks(slideShowRunnable);
              slideshow = false;
              toggleSystemUI();
            } else {
              handler.postDelayed(this, SLIDE_SHOW_INTERVAL);
            }
          }
        }
      };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    supportPostponeEnterTransition();
    context = this;
    setContentView(R.layout.activity_pager);
    relativeLayout = findViewById(R.id.PhotoPager_Layout);
      viewSwitcher = findViewById(R.id.view_switcher_single_media);
      parentView = findViewById(R.id.PhotoPager_Layout);
      bottomBar = findViewById(R.id.toolbar_bottom);
      imgView = findViewById(R.id.img);
      toolbar = findViewById(R.id.toolbar);
      mViewPager = findViewById(R.id.photos_pager);
    DisplayMetrics metrics = getResources().getDisplayMetrics();
    imageWidth = metrics.widthPixels;
    imageHeight = metrics.heightPixels;
    handler = new Handler();
    runnable =
        new Runnable() {
          @Override
          public void run() {
            hideSystemUI();
          }
        };
    startHandler();
    overridePendingTransition(R.anim.media_zoom_in, 0);
    SP = PreferenceUtil.getInstance(getApplicationContext());
    favphotomode = getIntent().getBooleanExtra("fav_photos", false);
    upoadhis = getIntent().getBooleanExtra("uploadhistory", false);
    trashdis = getIntent().getBooleanExtra("trashbin", false);
    allPhotoMode = getIntent().getBooleanExtra(getString(R.string.all_photo_mode), false);
    all_photo_pos = getIntent().getIntExtra(getString(R.string.position), 0);
    size_all = getIntent().getIntExtra(getString(R.string.allMediaSize), getAlbum().getCount());
    if (getIntent().hasExtra("favouriteslist")) {
      favouriteslist = getIntent().getParcelableArrayListExtra("favouriteslist");
    }
    if (getIntent().hasExtra("datalist")) {
      uploadhistory = getIntent().getParcelableArrayListExtra("datalist");
    }
    if (getIntent().hasExtra("trashdatalist")) {
      trashbinlistd = getIntent().getParcelableArrayListExtra("trashdatalist");
    }

    String path2 = getIntent().getStringExtra("path");
    pathForDescription = path2;

    //            mViewPager.setLocked(savedInstanceState.getBoolean(ISLOCKED_ARG, false));
    try {
      Album album;
      if ((getIntent().getAction().equals(Intent.ACTION_VIEW)
              || getIntent().getAction().equals(ACTION_REVIEW))
          && getIntent().getData() != null) {

        String path = ContentHelper.getMediaPath(getApplicationContext(), getIntent().getData());
        pathForDescription = path;
        File file = null;
        if (path != null) file = new File(path);

        if (file != null && file.isFile()) {
          // the image is stored in the storage
          album = new Album(getApplicationContext(), file);
        } else {
          // try to show with Uri
          album = new Album(getApplicationContext(), getIntent().getData());
          customUri = true;
        }
        getAlbums().addAlbum(0, album);
      }
      setUpSwitcherAnimation();
      initUI();
      setupUI();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setUpSwitcherAnimation() {
    Animation in = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
    Animation out = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);

    viewSwitcher.setInAnimation(in);
    viewSwitcher.setOutAnimation(out);
  }

  private void initUI() {
    final Menu bottomMenu = bottomBar.getMenu();
    getMenuInflater().inflate(R.menu.menu_bottom_view_pager, bottomMenu);
    if (upoadhis) {
      bottomMenu.findItem(R.id.action_favourites).setVisible(false);
      bottomMenu.findItem(R.id.action_edit).setVisible(false);
      bottomMenu.findItem(R.id.action_compress).setVisible(false);
    }

    if (trashdis) {
      bottomMenu.findItem(R.id.action_favourites).setVisible(false);
      bottomMenu.findItem(R.id.action_edit).setVisible(false);
      bottomMenu.findItem(R.id.action_compress).setVisible(false);
      bottomMenu.findItem(R.id.action_share).setVisible(false);
      bottomMenu.findItem(R.id.restore_action).setVisible(true);
      // bottomMenu.findItem(R.id.action_delete).setVisible(false);
    }

    if (!allPhotoMode && favphotomode) {
      bottomBar.getMenu().getItem(5).setVisible(false);
    }

    for (int i = 0; i < bottomMenu.size(); i++) {
      bottomMenu
          .getItem(i)
          .setOnMenuItemClickListener(
              new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                  stopHandler();
                  return onOptionsItemSelected(item);
                }
              });
    }
    setSupportActionBar(toolbar);
    toolbar.bringToFront();
    toolbar.setNavigationIcon(getToolbarIcon(CommunityMaterial.Icon.cmd_arrow_left));
    toolbar.setNavigationOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            onBackPressed();
          }
        });
    setRecentApp(getString(R.string.app_name));
    setupSystemUI();
    final LinearLayoutManager linearLayoutManager =
        new LinearLayoutManager(
            ActivitySwitchHelper.getContext(), LinearLayoutManager.HORIZONTAL, false);
    mViewPager.setLayoutManager(linearLayoutManager);
    mViewPager.setHasFixedSize(true);
    mViewPager.setLongClickable(true);

    getWindow()
        .getDecorView()
        .setOnSystemUiVisibilityChangeListener(
            new View.OnSystemUiVisibilityChangeListener() {
              @Override
              public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) showSystemUI();
                else hideSystemUI();
              }
            });
    BasicCallBack basicCallBack =
        new BasicCallBack() {
          @Override
          public void callBack(int status, Object data) {
            toggleSystemUI();
          }
        };

    if (!allPhotoMode && !favphotomode && !upoadhis && !trashdis) {
      adapter = new ImageAdapter(getAlbum().getMedia(), basicCallBack, this, this);
      getSupportActionBar()
          .setTitle(
              (getAlbum().getCurrentMediaIndex() + 1)
                  + " "
                  + getString(R.string.of)
                  + " "
                  + getAlbum().getMedia().size());
      current_image_pos = all_photo_pos;
      //            toolbar.setTitle((mViewPager.getCurrentItem() + 1) + " " +
      // getString(R.string.of) + " " + getAlbum().getMedia().size());
      if (bottomMenu.findItem(R.id.action_favourites).getIcon().getColorFilter() == null) {
        if (!favsearch(getAlbum().getMedia(current_image_pos).getPath())) {
          bottomMenu.findItem(R.id.action_favourites).getIcon().clearColorFilter();
        } else {
          bottomMenu
              .findItem(R.id.action_favourites)
              .getIcon()
              .setColorFilter(getAccentColor(), PorterDuff.Mode.SRC_IN);
        }
      }
      mViewPager.setOnPageChangeListener(
          new PagerRecyclerView.OnPageChangeListener() {
            @Override
            public void onPageChanged(int oldPosition, int position) {

              ImageAdapter.ViewHolder imageViewHolder =
                  (ImageAdapter.ViewHolder) mViewPager.findViewHolderForAdapterPosition(position);
              imageViewHolder
                  .getImageView()
                  .setOnPhotoTapListener(
                      new PhotoViewAttacher.OnPhotoTapListener() {
                        @Override
                        public void onPhotoTap(View view, float x, float y) {
                          singleTap();
                        }

                        @Override
                        public void onOutsidePhotoTap() {
                          singleTap();
                        }
                      });
              getAlbum().setCurrentPhotoIndex(position);
              toolbar.setTitle(
                  (position + 1)
                      + " "
                      + getString(R.string.of)
                      + " "
                      + getAlbum().getMedia().size());
              invalidateOptionsMenu();
              if (!favsearch(getAlbum().getMedia(position).getPath())) {
                bottomMenu.findItem(R.id.action_favourites).getIcon().clearColorFilter();
              } else {
                bottomMenu
                    .findItem(R.id.action_favourites)
                    .getIcon()
                    .setColorFilter(getAccentColor(), PorterDuff.Mode.SRC_IN);
              }
              pathForDescription = getAlbum().getMedia().get(position).getPath();
            }
          });
      mViewPager.scrollToPosition(getAlbum().getCurrentMediaIndex());
    } else if (allPhotoMode && !favphotomode && !upoadhis && !trashdis) {
      adapter = new ImageAdapter(GalleryMainActivity.listAll, basicCallBack, this, this);
      getSupportActionBar()
          .setTitle(all_photo_pos + 1 + " " + getString(R.string.of) + " " + size_all);
      current_image_pos = all_photo_pos;
      if (bottomMenu.findItem(R.id.action_favourites).getIcon().getColorFilter() == null) {
        if (!favsearch(listAll.get(current_image_pos).getPath())) {
          bottomMenu.findItem(R.id.action_favourites).getIcon().clearColorFilter();
        } else {
          bottomMenu
              .findItem(R.id.action_favourites)
              .getIcon()
              .setColorFilter(getAccentColor(), PorterDuff.Mode.SRC_IN);
        }
      }
      mViewPager.setOnPageChangeListener(
          new PagerRecyclerView.OnPageChangeListener() {
            @Override
            public void onPageChanged(int oldPosition, int position) {

              ImageAdapter.ViewHolder imageViewHolder =
                  (ImageAdapter.ViewHolder) mViewPager.findViewHolderForAdapterPosition(position);
              imageViewHolder
                  .getImageView()
                  .setOnPhotoTapListener(
                      new PhotoViewAttacher.OnPhotoTapListener() {
                        @Override
                        public void onPhotoTap(View view, float x, float y) {

                          singleTap();
                        }

                        @Override
                        public void onOutsidePhotoTap() {

                          singleTap();
                        }
                      });
              current_image_pos = position;
              getAlbum().setCurrentPhotoIndex(getAlbum().getCurrentMediaIndex());
              toolbar.setTitle((position + 1) + " " + getString(R.string.of) + " " + size_all);
              invalidateOptionsMenu();
              if (!favsearch(listAll.get(current_image_pos).getPath())) {
                bottomMenu.findItem(R.id.action_favourites).getIcon().clearColorFilter();
              } else {
                bottomMenu
                    .findItem(R.id.action_favourites)
                    .getIcon()
                    .setColorFilter(getAccentColor(), PorterDuff.Mode.SRC_IN);
              }
              pathForDescription = listAll.get(position).getPath();
            }
          });
      mViewPager.scrollToPosition(all_photo_pos);
    } else if (!allPhotoMode && favphotomode && !upoadhis && !trashdis) {
      adapter = new ImageAdapter(favouriteslist, basicCallBack, this, this);
      getSupportActionBar()
          .setTitle(all_photo_pos + 1 + " " + getString(R.string.of) + " " + size_all);
      current_image_pos = all_photo_pos;
      mViewPager.setOnPageChangeListener(
          new PagerRecyclerView.OnPageChangeListener() {
            @Override
            public void onPageChanged(int oldPosition, int position) {

              ImageAdapter.ViewHolder imageViewHolder =
                  (ImageAdapter.ViewHolder) mViewPager.findViewHolderForAdapterPosition(position);
              imageViewHolder
                  .getImageView()
                  .setOnPhotoTapListener(
                      new PhotoViewAttacher.OnPhotoTapListener() {
                        @Override
                        public void onPhotoTap(View view, float x, float y) {
                          singleTap();
                        }

                        @Override
                        public void onOutsidePhotoTap() {
                          singleTap();
                        }
                      });
              current_image_pos = position;
              getAlbum().setCurrentPhotoIndex(getAlbum().getCurrentMediaIndex());
              toolbar.setTitle((position + 1) + " " + getString(R.string.of) + " " + size_all);
              invalidateOptionsMenu();
              pathForDescription = favouriteslist.get(position).getPath();
            }
          });
      mViewPager.scrollToPosition(all_photo_pos);
    } else if (!favphotomode && !allPhotoMode && upoadhis && !trashdis) {
      adapter = new ImageAdapter(uploadhistory, basicCallBack, this, this);
      getSupportActionBar()
          .setTitle(all_photo_pos + 1 + " " + getString(R.string.of) + " " + size_all);
      current_image_pos = all_photo_pos;
      mViewPager.setOnPageChangeListener(
          new PagerRecyclerView.OnPageChangeListener() {
            @Override
            public void onPageChanged(int oldPosition, int position) {
              ImageAdapter.ViewHolder imageViewHolder =
                  (ImageAdapter.ViewHolder) mViewPager.findViewHolderForAdapterPosition(position);
              imageViewHolder
                  .getImageView()
                  .setOnPhotoTapListener(
                      new PhotoViewAttacher.OnPhotoTapListener() {
                        @Override
                        public void onPhotoTap(View view, float x, float y) {

                          singleTap();
                        }

                        @Override
                        public void onOutsidePhotoTap() {

                          singleTap();
                        }
                      });

              current_image_pos = position;
              getAlbum().setCurrentPhotoIndex(getAlbum().getCurrentMediaIndex());
              toolbar.setTitle((position + 1) + " " + getString(R.string.of) + " " + size_all);
              invalidateOptionsMenu();
              pathForDescription = uploadhistory.get(position).getPath();
            }
          });
      mViewPager.scrollToPosition(all_photo_pos);
    } else if (trashdis && !upoadhis && !favphotomode && !allPhotoMode) {
      adapter = new ImageAdapter(trashbinlistd, basicCallBack, this, this);
      getSupportActionBar().setTitle(all_photo_pos + 1 + " " + "of" + " " + size_all);
      current_image_pos = all_photo_pos;
      mViewPager.setOnPageChangeListener(
          new PagerRecyclerView.OnPageChangeListener() {
            @Override
            public void onPageChanged(int oldPosition, int position) {
              ImageAdapter.ViewHolder imageViewHolder =
                  (ImageAdapter.ViewHolder) mViewPager.findViewHolderForAdapterPosition(position);
              imageViewHolder
                  .getImageView()
                  .setOnPhotoTapListener(
                      new PhotoViewAttacher.OnPhotoTapListener() {
                        @Override
                        public void onPhotoTap(View view, float x, float y) {
                          singleTap();
                        }

                        @Override
                        public void onOutsidePhotoTap() {
                          singleTap();
                        }
                      });

              current_image_pos = position;
              getAlbum().setCurrentPhotoIndex(getAlbum().getCurrentMediaIndex());
              toolbar.setTitle((position + 1) + " " + getString(R.string.of) + " " + size_all);
              invalidateOptionsMenu();
              pathForDescription = trashbinlistd.get(position).getPath();
            }
          });
      mViewPager.scrollToPosition(all_photo_pos);
    }

    Display aa = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
    mViewPager.setAdapter(adapter);

    if (aa.getRotation() == Surface.ROTATION_90) {
      Configuration configuration = new Configuration();
      configuration.orientation = Configuration.ORIENTATION_LANDSCAPE;
      onConfigurationChanged(configuration);
    }
  }

  private void setupUI() {

    /** ** Theme *** */
    toolbar = findViewById(R.id.toolbar);
    toolbar.setBackgroundColor(
        isApplyThemeOnImgAct()
            ? ColorPalette.getTransparentColor(getPrimaryColor(), getTransparency())
            : ColorPalette.getTransparentColor(getDefaultThemeToolbarColor3th(), 175));

    toolbar.setPopupTheme(getPopupToolbarStyle());

    ActivityBackground = findViewById(R.id.PhotoPager_Layout);
    ActivityBackground.setBackgroundColor(getBackgroundColor());

    setStatusBarColor();
    setNavBarColor();

    /** ** SETTINGS *** */
    if (SP.getBoolean("set_max_luminosity", false)) updateBrightness(1.0F);
    else
      try {
        float brightness =
            Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        brightness = brightness == 1.0F ? 255.0F : brightness;
        updateBrightness(brightness);
      } catch (Settings.SettingNotFoundException e) {
        e.printStackTrace();
      }

    if (SP.getBoolean("set_picture_orientation", false))
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
  }

  /**
   * startHandler and stopHandler are helper methods for onUserInteraction, that auto-hides the
   * nav-bars and switch the activity to full screen, thus giving more better UX.
   */
  private void startHandler() {
    handler.postDelayed(runnable, 5000);
  }

  private void stopHandler() {
    handler.removeCallbacks(runnable);
  }

  @Override
  public void onUserInteraction() {
    super.onUserInteraction();
    stopHandler();
    startHandler();
  }

  @Override
  public void onResume() {
    super.onResume();
    ActivitySwitchHelper.setContext(this);
    setupUI();
  }

  @Override
  protected void onStop() {
    super.onStop();
    stopHandler();
    SP.putBoolean("auto_update_media", true);
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    Glide.get(getApplicationContext()).clearMemory();
    Glide.get(getApplicationContext()).trimMemory(TRIM_MEMORY_COMPLETE);
    System.gc();
  }

  @Override
  public boolean onMenuOpened(int featureId, Menu menu) {
    if (featureId == AppCompatDelegate.FEATURE_SUPPORT_ACTION_BAR && menu != null) stopHandler();

    return super.onMenuOpened(featureId, menu);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_view_pager, menu);
    this.menu = menu;
    return true;
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    RelativeLayout.LayoutParams params =
        new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
      params.setMargins(0, 0, Measure.getNavigationBarSize(SingleMediaActivity.this).x, 0);
    else params.setMargins(0, 0, 0, 0);

    toolbar.setLayoutParams(params);

    setUpViewPager();
  }

  private boolean favsearch(String path) {
    boolean favis = false;
    realm = Realm.getDefaultInstance();
    RealmResults<FavouriteImagesModel> realmQuery =
        realm.where(FavouriteImagesModel.class).findAll();
    for (int i = 0; i < realmQuery.size(); i++) {
      if (realmQuery.get(i).getPath().equals(path)) {
        favis = true;
        break;
      }
    }
    return favis;
  }

  private void performrealmaction(final ImageDescModel descModel, String newpath) {
    realm = Realm.getDefaultInstance();
    int index = descModel.getId().lastIndexOf("/");
    String name = descModel.getId().substring(index + 1);
    String newpathy = newpath + "/" + name;
    realm.beginTransaction();
    ImageDescModel imageDescModel = realm.createObject(ImageDescModel.class, newpathy);
    imageDescModel.setTitle(descModel.getTitle());
    realm.commitTransaction();
    realm.executeTransaction(
        new Realm.Transaction() {
          @Override
          public void execute(Realm realm) {
            RealmResults<ImageDescModel> result =
                realm.where(ImageDescModel.class).equalTo("path", descModel.getId()).findAll();
            result.deleteAllFromRealm();
          }
        });
  }

  private void getdescriptionpaths(String patjs, String newpth) {
    realm = Realm.getDefaultInstance();
    RealmQuery<ImageDescModel> realmQuery = realm.where(ImageDescModel.class);
    for (int i = 0; i < realmQuery.count(); i++) {
      if (realmQuery.findAll().get(i).getId().equals(patjs)) {
        performrealmaction(realmQuery.findAll().get(i), newpth);
        break;
      }
    }
  }

  @Override
  public boolean onPrepareOptionsMenu(final Menu menu) {

    if (getAlbum().getCurrentMedia().getPath().equals(getAlbum().getCoverPath()))
      menu.findItem(R.id.action_cover).setTitle("Remove cover image");

    if (allPhotoMode || favphotomode) {
      menu.findItem(R.id.action_cover).setVisible(false);
    }
    if (!allPhotoMode && !favphotomode && !upoadhis && !trashdis) {
      menu.setGroupVisible(R.id.only_photos_options, true);
    } else if (!allPhotoMode && favphotomode && !upoadhis && !trashdis) {
      menu.findItem(R.id.action_copy).setVisible(false);
      menu.findItem(R.id.action_move).setVisible(false);
    } else if (!allPhotoMode && !favphotomode && (upoadhis || trashdis)) {
      menu.findItem(R.id.action_copy).setVisible(false);
      menu.findItem(R.id.action_move).setVisible(false);
      menu.findItem(R.id.slide_show).setVisible(false);
      menu.findItem(R.id.action_use_as).setVisible(false);
      menu.findItem(R.id.action_cover).setVisible(false);
    }
    if (customUri) {
      menu.setGroupVisible(R.id.on_internal_storage, false);
      menu.setGroupVisible(R.id.only_photos_options, false);
      menu.findItem(R.id.sort_action).setVisible(false);
    }
    if (getAlbum().getCount() == 1) {
      menu.findItem(R.id.action_cover).setVisible(false);
      menu.findItem(R.id.slide_show).setVisible(false);
    }
    return true;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == REQ_CODE_SPEECH_INPUT && data != null) {
      ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
      voiceInput = result.get(0);
      editTextDescription.setText(
          editTextDescription.getText().toString().trim() + " " + voiceInput);
      editTextDescription.setSelection(editTextDescription.length());
      return;
    }

    if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SD_CARD_PERMISSIONS) {
      Uri treeUri = data.getData();
      // Persist URI in shared preference so that you can use it later.
      ContentHelper.saveSdCardInfo(getApplicationContext(), treeUri);
      getContentResolver()
          .takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }

    if (data != null && resultCode == RESULT_OK) {
      switch (requestCode) {
        case UCrop.REQUEST_CROP:
          final Uri imageUri = UCrop.getOutput(data);
          if (imageUri != null && imageUri.getScheme().equals("file")) {
            try {
              // copyFileToDownloads(imageUri);
              // TODO: 21/08/16 handle this better
              handleEditorImage(data);
              if (ContentHelper.copyFile(
                  getApplicationContext(),
                  new File(imageUri.getPath()),
                  new File(getAlbum().getPath()))) {
                // ((ImageFragment)
                // adapter.getRegisteredFragment(getAlbum().getCurrentMediaIndex())).displayMedia(true);
                SnackBarHandler.showWithBottomMargin(
                    parentView,
                    getString(R.string.new_file_created),
                    parentView.getHeight() - bottomBar.getTop());
              }
              // adapter.notifyDataSetChanged();
            } catch (Exception e) {
              Log.e("ERROS - uCrop", imageUri.toString(), e);
            }
          } else
            SnackBarHandler.showWithBottomMargin(
                parentView, "errori random", parentView.getHeight() - bottomBar.getTop());
          break;
        default:
          break;
      }
    }
  }

  private void handleEditorImage(Intent data) {
    String newFilePath = data.getStringExtra(EditImageActivity.EXTRA_OUTPUT);
    boolean isImageEdit = data.getBooleanExtra(EditImageActivity.IMAGE_IS_EDIT, false);

    if (isImageEdit) {

    } else { // Or use the original unedited pictures
      newFilePath = data.getStringExtra(EditImageActivity.FILE_PATH);
    }
    // System.out.println("newFilePath---->" + newFilePath);
    // File file = new File(newFilePath);
    // System.out.println("newFilePath size ---->" + (file.length() / 1024)+"KB");
    Log.d("image is edit", isImageEdit + "");
    LoadImageTask loadTask = new LoadImageTask();
    loadTask.execute(newFilePath);
  }

  private void displayAlbums(boolean reload) {
    Intent i = new Intent(SingleMediaActivity.this, GalleryMainActivity.class);
    Bundle b = new Bundle();
    b.putInt(SplashScreen.CONTENT, SplashScreen.ALBUMS_PREFETCHED);
    if (!reload) i.putExtras(b);
    startActivity(i);
    finish();
  }

  private void deleteCurrentMedia() {
    boolean success = false;
    if (!allPhotoMode && !favphotomode && !upoadhis && !trashdis) {
      if (AlertDialogsHelper.check) {
        success = addToTrash();
        Snackbar snackbar =
            SnackBarHandler.showWithBottomMargin(
                parentView,
                getString(R.string.trashbin_move_onefile),
                parentView.getHeight() - bottomBar.getTop(),
                Snackbar.LENGTH_SHORT);
        snackbar.setAction(
            R.string.open,
            new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                startActivity(new Intent(SingleMediaActivity.this, TrashBinActivity.class));
              }
            });
        snackbar.show();
      } else {
        success = getAlbum().deleteCurrentMedia(getApplicationContext());
        Snackbar snackbar =
            SnackBarHandler.showWithBottomMargin(
                parentView,
                getApplicationContext().getString(R.string.photo_deleted_msg),
                parentView.getHeight() - bottomBar.getTop());
        snackbar.show();
      }
      if (!success) {
        final AlertDialog.Builder dialogBuilder =
            new AlertDialog.Builder(SingleMediaActivity.this, getDialogStyle());

        AlertDialogsHelper.getTextDialog(
            SingleMediaActivity.this,
            dialogBuilder,
            R.string.sd_card_write_permission_title,
            R.string.sd_card_permissions_message,
            null);

        dialogBuilder.setPositiveButton(
            getString(R.string.ok_action).toUpperCase(),
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialogInterface, int i) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                  startActivityForResult(
                      new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),
                      REQUEST_CODE_SD_CARD_PERMISSIONS);
              }
            });
        dialogBuilder.show();
      }
      if (getAlbum().getMedia().size() == 0) {
        if (customUri) finish();
        else {
          getAlbums().removeCurrentAlbum();
          displayAlbums(false);
        }
      }
      adapter.notifyDataSetChanged();
      getSupportActionBar()
          .setTitle(
              (getAlbum().getCurrentMediaIndex() + 1)
                  + " "
                  + getString(R.string.of)
                  + " "
                  + getAlbum().getMedia().size());
    } else if (allPhotoMode && !favphotomode && !upoadhis && !trashdis) {
      int c = current_image_pos;
      if (AlertDialogsHelper.check) {
        success = addToTrash();
      } else {
        deleteMedia(listAll.get(current_image_pos).getPath());
        success = true;
      }
      if (success) {
        GalleryMainActivity.listAll.remove(current_image_pos);
        size_all = GalleryMainActivity.listAll.size();
        adapter.notifyDataSetChanged();
        // SnackBarHandler.show(parentView,
        // getApplicationContext().getString(R.string.photo_deleted_msg));
      }
      if (current_image_pos != size_all)
        getSupportActionBar().setTitle((c + 1) + " " + getString(R.string.of) + " " + size_all);
      //            mViewPager.setCurrentItem(current_image_pos);
      //            toolbar.setTitle((mViewPager.getCurrentItem() + 1) + " " +
      // getString(R.string.of) + " " + size_all);
    } else if (favphotomode && !allPhotoMode && !upoadhis && !trashdis) {
      int c = current_image_pos;
      // deleteMedia(favouriteslist.get(current_image_pos).getPath());
      realm = Realm.getDefaultInstance();
      realm.executeTransaction(
          new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
              RealmResults<FavouriteImagesModel> favouriteImagesModels =
                  realm
                      .where(FavouriteImagesModel.class)
                      .equalTo("path", favouriteslist.get(current_image_pos).getPath())
                      .findAll();
              favouriteImagesModels.deleteAllFromRealm();
            }
          });
      deleteFromList(favouriteslist.get(current_image_pos).getPath());
      size_all = favouriteslist.size();
      if (size_all > 0) {
        adapter.notifyDataSetChanged();
        getSupportActionBar().setTitle((c + 1) + " " + getString(R.string.of) + " " + size_all);
        SnackBarHandler.create(
                parentView, getApplicationContext().getString(R.string.photo_deleted_from_fav_msg))
            .show();
      } else {
        onBackPressed();
      }
    } else if (!favphotomode && !allPhotoMode && upoadhis && !trashdis) {
      int c = current_image_pos;
      // deleteMedia(favouriteslist.get(current_image_pos).getPath());
      if (uploadhistory.get(current_image_pos).getPath().contains(".nomedia")) {
        File file = new File(uploadhistory.get(current_image_pos).getPath());
        if (file.exists()) {
          file.delete();
        }
      }
      realm = Realm.getDefaultInstance();
      realm.executeTransaction(
          new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
            }
          });
      deleteFromList(uploadhistory.get(current_image_pos).getPath());
      size_all = uploadhistory.size();
      if (size_all > 0) {
        adapter.notifyDataSetChanged();
        getSupportActionBar().setTitle((c + 1) + " " + getString(R.string.of) + " " + size_all);
        SnackBarHandler.create(
                parentView, getApplicationContext().getString(R.string.photo_deleted_from_fav_msg))
            .show();
      } else {
        onBackPressed();
      }
    } else if (trashdis && !favphotomode && !upoadhis && !allPhotoMode) {
      int c = current_image_pos;
      realm = Realm.getDefaultInstance();
      realm.executeTransaction(
          new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
              RealmResults<TrashBinRealmModel> trashBinRealmModels =
                  realm
                      .where(TrashBinRealmModel.class)
                      .equalTo("trashbinpath", trashbinlistd.get(current_image_pos).getPath())
                      .findAll();
              trashBinRealmModels.deleteAllFromRealm();
            }
          });
      deleteFromList(trashbinlistd.get(current_image_pos).getPath());
      size_all = trashbinlistd.size();
      if (size_all > 0) {
        adapter.notifyDataSetChanged();
        getSupportActionBar().setTitle((c + 1) + " " + getString(R.string.of) + " " + size_all);
        // SnackBarHandler.show(parentView,
        // getApplicationContext().getString(R.string.photo_deleted_from_fav_msg));
      } else {
        onBackPressed();
      }
    }
  }

  private void addTrashObjectsToRealm(String mediaPath) {
    String trashbinpath = Environment.getExternalStorageDirectory() + "/" + ".nomedia";
    realm = Realm.getDefaultInstance();
    realm.beginTransaction();
    String name = mediaPath.substring(mediaPath.lastIndexOf("/") + 1);
    String trashpath = trashbinpath + "/" + name;
    TrashBinRealmModel trashBinRealmModel = realm.createObject(TrashBinRealmModel.class, trashpath);
    trashBinRealmModel.setOldpath(mediaPath);
    trashBinRealmModel.setDatetime(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
    trashBinRealmModel.setTimeperiod("null");
    realm.commitTransaction();
  }

  private void deleteFromList(String path) {
    if (favphotomode) {
      for (int i = 0; i < favouriteslist.size(); i++) {
        if (favouriteslist.get(i).getPath().equals(path)) {
          favouriteslist.remove(i);
          break;
        }
      }
    } else if (upoadhis) {
      for (int i = 0; i < uploadhistory.size(); i++) {
        if (uploadhistory.get(i).getPath().equals(path)) {
          uploadhistory.remove(i);
          break;
        }
      }
    } else if (trashdis) {
      for (int i = 0; i < trashbinlistd.size(); i++) {
        if (trashbinlistd.get(i).getPath().equals(path)) {
          trashbinlistd.remove(i);
          break;
        }
      }
    }
  }

  private boolean addToTrash() {
    String pathOld = null;
    String oldpath = null;
    int no = 0;
    boolean succ = false;
    if (!allPhotoMode && !favphotomode && !upoadhis) {
      oldpath = getAlbum().getCurrentMedia().getPath();
    } else if (allPhotoMode && !favphotomode && !upoadhis) {
      oldpath = listAll.get(current_image_pos).getPath();
    }
    File file = new File(Environment.getExternalStorageDirectory() + "/" + ".nomedia");
    if (file.exists() && file.isDirectory()) {
      if (!allPhotoMode && !favphotomode) {
        pathOld = getAlbum().getCurrentMedia().getPath();
        succ = getAlbum().moveCurrentMedia(getApplicationContext(), file.getAbsolutePath());
      } else if (allPhotoMode && !favphotomode) {
        pathOld = listAll.get(current_image_pos).getPath();
        succ =
            getAlbum()
                .moveAnyMedia(
                    getApplicationContext(),
                    file.getAbsolutePath(),
                    listAll.get(current_image_pos).getPath());
      }
      if (succ) {
        Snackbar snackbar =
            SnackBarHandler.showWithBottomMargin2(
                parentView,
                getString(R.string.trashbin_move_onefile),
                navigationView.getHeight(),
                Snackbar.LENGTH_SHORT);
        final String finalOldpath = oldpath;
        snackbar.setAction(
            "UNDO",
            new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                getAlbum()
                    .moveAnyMedia(getApplicationContext(), getAlbum().getPath(), finalOldpath);
              }
            });
        snackbar.show();
      } else {
        SnackBarHandler.showWithBottomMargin(
            parentView,
                no + " " + getString(R.string.trashbin_move_error),
            navigationView.getHeight());
      }
    } else {
      if (file.mkdir()) {
        if (!allPhotoMode && !favphotomode) {
          pathOld = getAlbum().getCurrentMedia().getPath();
          succ = getAlbum().moveCurrentMedia(getApplicationContext(), file.getAbsolutePath());
        } else if (allPhotoMode && !favphotomode) {
          pathOld = getAlbum().getCurrentMedia().getPath();
          succ =
              getAlbum()
                  .moveAnyMedia(
                      getApplicationContext(),
                      file.getAbsolutePath(),
                      listAll.get(current_image_pos).getPath());
        }
        if (succ) {
          SnackBarHandler.showWithBottomMargin(
              parentView,
                  no + " " + getString(R.string.trashbin_move_onefile),
              navigationView.getHeight());
        } else {
          SnackBarHandler.showWithBottomMargin(
              parentView,
                  no + " " + getString(R.string.trashbin_move_error),
              navigationView.getHeight());
        }
      }
    }
    addTrashObjectsToRealm(pathOld);
    return succ;
  }

  private void deleteMedia(String path) {
    String[] projection = {MediaStore.Images.Media._ID};

    // Match on the file path
    String selection = MediaStore.Images.Media.DATA + " = ?";
    String[] selectionArgs = new String[] {path};

    // Query for the ID of the media matching the file path
    Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    ContentResolver contentResolver = getContentResolver();
    Cursor c = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
    if (c.moveToFirst()) {
      // We found the ID. Deleting the item via the content provider will also remove the file
      long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
      Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
      contentResolver.delete(deleteUri, null, null);
    }
    c.close();
  }

  private void deletefav(final String path) {
    realm = Realm.getDefaultInstance();
    realm.executeTransaction(
        new Realm.Transaction() {
          @Override
          public void execute(Realm realm) {
            RealmResults<FavouriteImagesModel> favouriteImagesModels =
                realm.where(FavouriteImagesModel.class).equalTo("path", path).findAll();
            favouriteImagesModels.deleteAllFromRealm();
          }
        });
  }

  private void deletefromfav(final MenuItem item) {
    String ButtonDelete = "";
    final AlertDialog.Builder deleteDialog =
        new AlertDialog.Builder(SingleMediaActivity.this, getDialogStyle());
    AlertDialogsHelper.getTextDialog(
        SingleMediaActivity.this,
        deleteDialog,
        R.string.remove_from_favourites,
        R.string.delete_from_favourites_message,
        null);
    ButtonDelete = this.getString(R.string.remove);
    deleteDialog.setNegativeButton(this.getString(R.string.cancel).toUpperCase(), null);
    deleteDialog.setPositiveButton(
        ButtonDelete.toUpperCase(),
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            item.getIcon().clearColorFilter();
            deletefav(getAlbum().getCurrentMedia().getPath());

            SnackBarHandler.showWithBottomMargin(
                parentView,
                getString(R.string.photo_deleted_from_fav_msg),
                parentView.getHeight() - bottomBar.getTop());
          }
        });
    AlertDialog alertDialog = deleteDialog.create();
    alertDialog.show();
    AlertDialogsHelper.setButtonTextColor(
        new int[] {DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE},
        getAccentColor(),
        alertDialog);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {

    switch (item.getItemId()) {
      case android.R.id.home:
        supportFinishAfterTransition();
        return true;
      case R.id.action_copy:
        handler.removeCallbacks(slideShowRunnable);
        bottomSheetDialogFragment = new SelectAlbumBottomSheet();
        bottomSheetDialogFragment.setTitle(getString(R.string.copy_to));
        bottomSheetDialogFragment.setSelectAlbumInterface(
            new SelectAlbumBottomSheet.SelectAlbumInterface() {
              @Override
              public void folderSelected(String path) {

                File file =
                    new File(
                        path
                            + "/"
                            + getAlbum().getCurrentMedia().getName()
                            + getAlbum()
                                .getCurrentMedia()
                                .getPath()
                                .substring(
                                    getAlbum().getCurrentMedia().getPath().lastIndexOf(".")));
                if (file.exists()) {

                  bottomSheetDialogFragment.dismiss();
                } else {
                  getAlbum()
                      .copyPhoto(
                          getApplicationContext(), getAlbum().getCurrentMedia().getPath(), path);
                  bottomSheetDialogFragment.dismiss();
                  SnackBarHandler.showWithBottomMargin(
                      relativeLayout,
                      getString(R.string.copied_successfully) + " to " + path,
                      parentView.getHeight() - bottomBar.getTop());
                }
              }
            });
        bottomSheetDialogFragment.show(
            getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
        break;

      case R.id.action_share:
          Uri uri1 = null;
          Intent share = new Intent(Intent.ACTION_SEND);
          if (!allPhotoMode) uri1 = Uri.fromFile(new File(getAlbum().getCurrentMedia().getPath()));
          else uri1 = Uri.fromFile(new File(listAll.get(current_image_pos).getPath()));
          share.putExtra(Intent.EXTRA_STREAM, uri1);
          share.setType("image/*");
          share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
          startActivity(Intent.createChooser(share, "Share Image"));
        /*handler.removeCallbacks(slideShowRunnable);
        Intent share = new Intent(SingleMediaActivity.this, SharingActivity.class);
        if (!allPhotoMode) share.putExtra(EXTRA_OUTPUT, getAlbum().getCurrentMedia().getPath());
        else share.putExtra(EXTRA_OUTPUT, listAll.get(current_image_pos).getPath());
        startActivity(share);*/
        return true;

      case R.id.action_edit:
        handler.removeCallbacks(slideShowRunnable);
        if (!allPhotoMode && !favphotomode) {
          uri = Uri.fromFile(new File(getAlbum().getCurrentMedia().getPath()));
        } else if (allPhotoMode && !favphotomode) {
          uri = Uri.fromFile(new File(listAll.get(current_image_pos).getPath()));
        } else if (!allPhotoMode && favphotomode) {
          uri = Uri.fromFile(new File(favouriteslist.get(current_image_pos).getPath()));
        }

        final String extension = uri.getPath();
        if (extension != null
            && !(extension.substring(extension.lastIndexOf(".")).equals(".gif"))) {
          Intent editIntent = new Intent(SingleMediaActivity.this, EditImageActivity.class);
          editIntent.putExtra("extra_input", uri.getPath());
          editIntent.putExtra(
              "extra_output",
              FileUtils.genEditFile(FileUtils.getExtension(extension)).getAbsolutePath());
          editIntent.putExtra("requestCode", ACTION_REQUEST_EDITIMAGE);
          startActivity(editIntent);
        } else {
          SnackBarHandler.create(parentView, getString(R.string.image_invalid)).show();
        }
        break;

      case R.id.action_use_as:
        handler.removeCallbacks(slideShowRunnable);
        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
        if (!allPhotoMode)
          intent.setDataAndType(
              getAlbum().getCurrentMedia().getUri(), getAlbum().getCurrentMedia().getMimeType());
        else
          intent.setDataAndType(
              Uri.fromFile(new File(listAll.get(current_image_pos).getPath())),
              StringUtils.getMimeType(listAll.get(current_image_pos).getPath()));
        startActivity(Intent.createChooser(intent, getString(R.string.use_as)));
        return true;

      case R.id.print:
        PrintHelper photoPrinter = new PrintHelper(this);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        Bitmap bitmap =
            BitmapFactory.decodeFile(
                getAlbum().getCurrentMedia().getPath(), new BitmapFactory.Options());
        photoPrinter.printBitmap(getString(R.string.print), bitmap);
        return true;

      case R.id.pdf:
        PrintHelper photopdf = new PrintHelper(this);
        photopdf.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        Bitmap bitmappdf =
            BitmapFactory.decodeFile(
                getAlbum().getCurrentMedia().getPath(), new BitmapFactory.Options());
        photopdf.printBitmap(getString(R.string.pdf), bitmappdf);
        return true;

      case R.id.action_favourites:
        realm = Realm.getDefaultInstance();
        String realpath = getAlbum().getCurrentMedia().getPath();
        RealmQuery<FavouriteImagesModel> query =
            realm.where(FavouriteImagesModel.class).equalTo("path", realpath);
        if (query.count() == 0) {
          realm.beginTransaction();
          fav = realm.createObject(FavouriteImagesModel.class, realpath);
          ImageDescModel q =
              realm.where(ImageDescModel.class).equalTo("path", realpath).findFirst();
          if (q != null) {
            fav.setDescription(q.getTitle());
          } else {
            fav.setDescription(" ");
          }
          item.getIcon().setColorFilter(getAccentColor(), PorterDuff.Mode.SRC_IN);
          realm.commitTransaction();
          final Snackbar snackbar =
              SnackBarHandler.showWithBottomMargin(
                  parentView,
                  getResources().getString(R.string.add_favourite),
                  parentView.getHeight() - bottomBar.getTop());
          snackbar.setAction(
              R.string.openfav,
              new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                  Intent intent = new Intent(SingleMediaActivity.this, GalleryMainActivity.class);
                  intent.putExtra("openFav", true);
                  startActivity(intent);
                }
              });
          snackbar.show();
        } else {
          deletefromfav(item);
        }
        break;

      case R.id.action_compress:
        handler.removeCallbacks(slideShowRunnable);
        if (!allPhotoMode) uri = Uri.fromFile(new File(getAlbum().getCurrentMedia().getPath()));
        else uri = Uri.fromFile(new File(listAll.get(current_image_pos).getPath()));
        String extension1 = uri.getPath();
        if (extension1 != null
            && !(extension1.substring(extension1.lastIndexOf(".")).equals(".gif"))) {
          Intent compressIntent = new Intent(SingleMediaActivity.this, CompressImageActivity.class);
          if (!allPhotoMode)
            compressIntent.putExtra(EXTRA_OUTPUT, getAlbum().getCurrentMedia().getPath());
          else compressIntent.putExtra(EXTRA_OUTPUT, listAll.get(current_image_pos).getPath());
          startActivity(compressIntent);

          // to send the resolution of image
          handler.removeCallbacks(slideShowRunnable);
          if (!allPhotoMode && !favphotomode) {
            mediacompress = getAlbum().getCurrentMedia();
          } else if (allPhotoMode && !favphotomode) {
            mediacompress = new Media(new File(listAll.get(current_image_pos).getPath()));
          } else if (!allPhotoMode && favphotomode) {
            mediacompress = new Media(new File(favouriteslist.get(current_image_pos).getPath()));
          }
        } else SnackBarHandler.create(parentView, R.string.image_invalid).show();
        break;

      case R.id.action_delete:
        String ButtonDelete = "";
        handler.removeCallbacks(slideShowRunnable);
        deleteaction(ButtonDelete);
        return true;

      case R.id.slide_show:
        handler.removeCallbacks(slideShowRunnable);
        setSlideShowDialog();
        return true;

      case R.id.action_move:
        final String pathcurrent = getAlbum().getCurrentMedia().getPath();
        handler.removeCallbacks(slideShowRunnable);
        bottomSheetDialogFragment = new SelectAlbumBottomSheet();
        bottomSheetDialogFragment.setTitle(getString(R.string.move_to));
        bottomSheetDialogFragment.setSelectAlbumInterface(
            new SelectAlbumBottomSheet.SelectAlbumInterface() {
              @Override
              public void folderSelected(String path) {
                getAlbum().moveCurrentMedia(getApplicationContext(), path);
                getSupportActionBar()
                    .setTitle(
                        (getAlbum().getCurrentMediaIndex() + 1)
                            + " "
                            + getString(R.string.of)
                            + " "
                            + getAlbum().getMedia().size());

                if (getAlbum().getMedia().size() == 0) {
                  if (customUri) finish();
                  else {
                    getAlbums().removeCurrentAlbum();
                    displayAlbums(false);
                  }
                }
                adapter.notifyDataSetChanged();
                getdescriptionpaths(pathcurrent, path);
                //                        toolbar.setTitle((mViewPager.getCurrentItem() + 1) + " " +
                // getString(R.string.of) + " " + getAlbum().getCount());
                bottomSheetDialogFragment.dismiss();
                SnackBarHandler.showWithBottomMargin(
                    relativeLayout,
                    getString(R.string.photo_moved_successfully) + " to " + path,
                    parentView.getHeight() - bottomBar.getTop());
              }
            });
        bottomSheetDialogFragment.show(
            getSupportFragmentManager(), bottomSheetDialogFragment.getTag());

        return true;

      case R.id.action_cover:
        if (getAlbum().getCurrentMedia().getPath().equals(getAlbum().getCoverPath())) {
          AlbumSettings albumSettings =
              AlbumSettings.getSettings(getApplicationContext(), getAlbum());
          albumSettings.changeCoverPath(getApplicationContext(), getAlbum().getMedia(0).getPath());
          SnackBarHandler.showWithBottomMargin(
              parentView,
              getString(R.string.cover_removed),
              parentView.getHeight() - bottomBar.getTop());
        } else {
          AlbumSettings albumSettings =
              AlbumSettings.getSettings(getApplicationContext(), getAlbum());
          albumSettings.changeCoverPath(
              getApplicationContext(), getAlbum().getCurrentMedia().getPath());
          SnackBarHandler.showWithBottomMargin(
              parentView,
              getString(R.string.change_cover),
              parentView.getHeight() - bottomBar.getTop());
          MenuItem cover = menu.findItem(R.id.action_cover);
          cover.setTitle("Remove cover image");
        }

        return true;

      case R.id.action_settings:
        handler.removeCallbacks(slideShowRunnable);
        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        break;

      case R.id.restore_action:
        String button = "";
        final AlertDialog.Builder deleteDialog =
            new AlertDialog.Builder(SingleMediaActivity.this, getDialogStyle());
        AlertDialogsHelper.getTextDialog(
            SingleMediaActivity.this, deleteDialog, R.string.restore, R.string.restore_image, null);
        button = this.getString(R.string.restore);
        deleteDialog.setNegativeButton(this.getString(R.string.cancel).toUpperCase(), null);
        deleteDialog.setPositiveButton(
            button.toUpperCase(),
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialogInterface, int i) {
                restoreImage(trashbinlistd.get(current_image_pos).getPath());
              }
            });

        AlertDialog alertDialog = deleteDialog.create();
        alertDialog.show();
        AlertDialogsHelper.setButtonTextColor(
            new int[] {DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE},
            getAccentColor(),
            alertDialog);
        return true;

      default:
        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        // return super.onOptionsItemSelected(item);
    }
    return super.onOptionsItemSelected(item);
  }

  private void restoreImage(String path) {
    realm = Realm.getDefaultInstance();
    RealmResults<TrashBinRealmModel> trashBinRealmModels =
        realm.where(TrashBinRealmModel.class).equalTo("trashbinpath", path).findAll();
    String oldpath = trashBinRealmModels.get(0).getOldpath();
    String oldFolder = oldpath.substring(0, oldpath.lastIndexOf("/"));
    if (restoreMove(context, trashBinRealmModels.get(0).getTrashbinpath(), oldFolder)) {
      scanFile(
          context,
          new String[] {
            trashBinRealmModels.get(0).getTrashbinpath(),
            StringUtils.getPhotoPathMoved(trashBinRealmModels.get(0).getTrashbinpath(), oldFolder)
          });
      if (removeFromRealm(trashBinRealmModels.get(0).getTrashbinpath())) {
        deleteFromList(trashbinlistd.get(current_image_pos).getPath());
        size_all = trashbinlistd.size();
        if (size_all > 0) {
          adapter.notifyDataSetChanged();
          getSupportActionBar()
              .setTitle((current_image_pos + 1) + " " + getString(R.string.of) + " " + size_all);
          // SnackBarHandler.show(parentView,
          // getApplicationContext().getString(R.string.photo_deleted_from_fav_msg));
        } else {
          onBackPressed();
        }
      }
    }
  }

  public void scanFile(Context context, String[] path) {
    MediaScannerConnection.scanFile(context, path, null, null);
  }

  private boolean restoreMove(Context context, String source, String targetDir) {
    File from = new File(source);
    File to = new File(targetDir);
    return ContentHelper.moveFile(context, from, to);
  }

  private boolean removeFromRealm(final String path) {
    final boolean[] delete = {false};
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(
        new Realm.Transaction() {
          @Override
          public void execute(Realm realm) {
            RealmResults<TrashBinRealmModel> result =
                realm.where(TrashBinRealmModel.class).equalTo("trashbinpath", path).findAll();
            delete[0] = result.deleteAllFromRealm();
          }
        });
    return delete[0];
  }

  private void updateBrightness(float level) {
    WindowManager.LayoutParams lp = getWindow().getAttributes();
    lp.screenBrightness = level;
    getWindow().setAttributes(lp);
  }

  @Override
  public void setNavBarColor() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      if (isApplyThemeOnImgAct())
        if (isNavigationBarColored())
          getWindow()
              .setNavigationBarColor(
                  ColorPalette.getTransparentColor(
                      ColorPalette.getObscuredColor(getPrimaryColor()), getTransparency()));
        else
          getWindow()
              .setNavigationBarColor(
                  ColorPalette.getTransparentColor(
                      ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000),
                      getTransparency()));
      else
        getWindow()
            .setNavigationBarColor(
                ColorPalette.getTransparentColor(
                    ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000), 175));
    }
  }

  @Override
  protected void setStatusBarColor() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      if (isApplyThemeOnImgAct())
        if (isTranslucentStatusBar() && isTransparencyZero()) {
          getWindow()
              .setStatusBarColor(
                  ColorPalette.getTransparentColor(getPrimaryColor(), getTransparency()));
        } else {
          getWindow().setStatusBarColor(ColorPalette.getObscuredColor(getPrimaryColor()));
        }
      else
        getWindow()
            .setStatusBarColor(
                ColorPalette.getTransparentColor(
                    ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000), 175));
    }
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    /*  if (mViewPager != null) {
        outState.putBoolean(ISLOCKED_ARG, mViewPager.isLocked());
    }*/
    super.onSaveInstanceState(outState);
  }

  public void toggleSystemUI() {
    if (fullScreenMode) showSystemUI();
    else hideSystemUI();
  }

  private void hideSystemUI() {
    runOnUiThread(
        new Runnable() {
          public void run() {
            toolbar
                .animate()
                .translationY(-toolbar.getHeight())
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(200)
                .start();
            bottomBar
                .animate()
                .translationY(+bottomBar.getHeight())
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(200)
                .start();
            getWindow()
                .getDecorView()
                .setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
            fullScreenMode = true;
            changeBackGroundColor();
            stopHandler(); // removing any runnable from the message queue
          }
        });
  }

  private void setupSystemUI() {
    toolbar
        .animate()
        .translationY(Measure.getStatusBarHeight(getResources()))
        .setInterpolator(new DecelerateInterpolator())
        .setDuration(0)
        .start();
    getWindow()
        .getDecorView()
        .setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
  }

  private void showSystemUI() {
    runOnUiThread(
        new Runnable() {
          public void run() {
            toolbar
                .animate()
                .translationY(Measure.getStatusBarHeight(getResources()))
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(240)
                .start();
            bottomBar
                .animate()
                .translationY(0)
                .setInterpolator(new DecelerateInterpolator())
                .start();

            getWindow()
                .getDecorView()
                .setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            fullScreenMode = false;
            changeBackGroundColor();
          }
        });
  }

  private void deleteaction(String ButtonDelete) {
    final AlertDialog.Builder deleteDialog =
        new AlertDialog.Builder(SingleMediaActivity.this, getDialogStyle());
    if (favphotomode) {
      AlertDialogsHelper.getTextDialog(
          SingleMediaActivity.this,
          deleteDialog,
          R.string.remove_from_favourites,
          R.string.delete_from_favourites_message,
          null);
      ButtonDelete = this.getString(R.string.remove);
    } else if (!favphotomode && !upoadhis && !trashdis) {
      AlertDialogsHelper.getTextCheckboxDialog(
          SingleMediaActivity.this,
          deleteDialog,
          R.string.delete,
          R.string.delete_photo_message,
          null,
          "Move to TrashBin",
          getAccentColor());
      ButtonDelete = this.getString(R.string.delete);
    } else if (upoadhis && !favphotomode && !trashdis) {
      AlertDialogsHelper.getTextDialog(
          SingleMediaActivity.this,
          deleteDialog,
          R.string.delete,
          R.string.delete_photo_message,
          null);
      ButtonDelete = this.getString(R.string.delete);
    } else if (trashdis && !upoadhis && !favphotomode) {
      AlertDialogsHelper.getTextDialog(
          SingleMediaActivity.this, deleteDialog, R.string.delete, R.string.delete_image_bin, null);
      ButtonDelete = this.getString(R.string.delete);
    }
    deleteDialog.setNegativeButton(this.getString(R.string.cancel).toUpperCase(), null);
    deleteDialog.setPositiveButton(
        ButtonDelete.toUpperCase(),
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            deleteCurrentMedia();
          }
        });
    AlertDialog alertDialog = deleteDialog.create();
    alertDialog.show();
    AlertDialogsHelper.setButtonTextColor(
        new int[] {DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE},
        getAccentColor(),
        alertDialog);
  }

  private void changeBackGroundColor() {
    int colorTo;
    int colorFrom;
    if (fullScreenMode) {
      colorFrom = getBackgroundColor();
      colorTo = (ContextCompat.getColor(SingleMediaActivity.this, R.color.md_black_1000));
    } else {
      colorFrom = (ContextCompat.getColor(SingleMediaActivity.this, R.color.md_black_1000));
      colorTo = getBackgroundColor();
    }
    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
    colorAnimation.setDuration(240);
    colorAnimation.addUpdateListener(
        new ValueAnimator.AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimator animator) {
            ActivityBackground.setBackgroundColor((Integer) animator.getAnimatedValue());
          }
        });
    colorAnimation.start();
  }

  @Override
  public void onBackPressed() {
    if (details) {
      viewSwitcher.showPrevious();
      toggleSystemUI();
      details = false;
    } else super.onBackPressed();
  }

  @Override
  public void onPause() {
    super.onPause();
    if (isFinishing()) {
      overridePendingTransition(0, R.anim.media_zoom_out);
    }
  }

  @Override
  public void singleTap() {
    toggleSystemUI();
    if (slideshow) {
      handler.removeCallbacks(slideShowRunnable);
      slideshow = false;
    }
  }

  @Override
  public void startPostponedTransition() {
    getWindow().setSharedElementEnterTransition(new ChangeBounds().setDuration(300));
    startPostponedEnterTransition();
  }

  private void setSlideShowDialog() {

    final AlertDialog.Builder slideshowDialog =
        new AlertDialog.Builder(SingleMediaActivity.this, getDialogStyle());
    final View SlideshowDialogLayout = getLayoutInflater().inflate(R.layout.dialog_slideshow, null);
    final TextView slideshowDialogTitle =
        SlideshowDialogLayout.findViewById(R.id.slideshow_dialog_title);
    final CardView slideshowDialogCard =
        SlideshowDialogLayout.findViewById(R.id.slideshow_dialog_card);
    final TextInputLayout til = SlideshowDialogLayout.findViewById(R.id.time_text_input_layout);
    final EditText editTextTimeInterval =
        SlideshowDialogLayout.findViewById(R.id.slideshow_edittext);

    slideshowDialogTitle.setBackgroundColor(getPrimaryColor());
    slideshowDialogCard.setBackgroundColor(getCardBackgroundColor());
    editTextTimeInterval
        .getBackground()
        .mutate()
        .setColorFilter(getTextColor(), PorterDuff.Mode.SRC_ATOP);
    editTextTimeInterval.setTextColor(getTextColor());
    editTextTimeInterval.setHintTextColor(getSubTextColor());
    setCursorDrawableColor(editTextTimeInterval, getTextColor());
    slideshowDialog.setView(SlideshowDialogLayout);

    final AlertDialog dialog = slideshowDialog.create();
    dialog.setCancelable(false);
    dialog.setButton(
        DialogInterface.BUTTON_NEGATIVE,
        getString(R.string.cancel).toUpperCase(),
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            dialog.dismiss();
          }
        });

    dialog.setButton(
        DialogInterface.BUTTON_POSITIVE,
        getString(R.string.ok_action).toUpperCase(),
        (DialogInterface.OnClickListener) null);
    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

    editTextTimeInterval.setOnEditorActionListener(
        new TextView.OnEditorActionListener() {
          @Override
          public boolean onEditorAction(TextView textView, int keyCode, KeyEvent keyEvent) {
            if (keyCode == EditorInfo.IME_ACTION_DONE || keyCode == EditorInfo.IME_ACTION_NEXT) {
              String value = editTextTimeInterval.getText().toString();
              if (!"".equals(value)) {
                slideshow = true;
                int intValue = Integer.parseInt(value);
                SLIDE_SHOW_INTERVAL = intValue * 1000;
                if (SLIDE_SHOW_INTERVAL > 1000 && SLIDE_SHOW_INTERVAL <= 10000) {
                  dialog.dismiss();
                  hideSystemUI();
                  SnackBarHandler.showWithBottomMargin(
                      parentView, getString(R.string.slide_start), 0);
                  handler.postDelayed(slideShowRunnable, SLIDE_SHOW_INTERVAL);
                }
              }
              return true;
            }
            return false;
          }
        });
    dialog.setOnShowListener(
        new DialogInterface.OnShowListener() {
          @Override
          public void onShow(DialogInterface dialogInterface) {
            Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(
                new View.OnClickListener() {
                  @Override
                  public void onClick(View view) {
                    String value = editTextTimeInterval.getText().toString();
                    if (!"".equals(value)) {
                      slideshow = true;
                      int intValue = Integer.parseInt(value);
                      SLIDE_SHOW_INTERVAL = intValue * 1000;
                      if (SLIDE_SHOW_INTERVAL > 1000 && SLIDE_SHOW_INTERVAL <= 10000) {
                        dialog.dismiss();
                        hideSystemUI();
                        SnackBarHandler.showWithBottomMargin(
                            parentView, getString(R.string.slide_start), 0);
                        handler.postDelayed(slideShowRunnable, SLIDE_SHOW_INTERVAL);
                      }
                    }
                  }
                });
          }
        });
    editTextTimeInterval.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // empty method
          }

          @Override
          public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            til.setError(null);
          }

          @Override
          public void afterTextChanged(Editable editable) {
            if (editTextTimeInterval.getText().toString().equals("")) {
              dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
              dialog
                  .getButton(DialogInterface.BUTTON_POSITIVE)
                  .setTextColor(getResources().getColor(R.color.accent_grey));
            } else if (Integer.parseInt(editTextTimeInterval.getText().toString()) > 10
                || Integer.parseInt(editTextTimeInterval.getText().toString()) < 2) {
              editTextTimeInterval.requestFocus();
              dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
              til.setError(getString(R.string.time_limit));
              dialog
                  .getButton(DialogInterface.BUTTON_POSITIVE)
                  .setTextColor(getResources().getColor(R.color.accent_grey));
            } else {
              dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
              dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getAccentColor());
            }
          }
        });
    dialog.show();
    AlertDialogsHelper.setButtonTextColor(
        new int[] {DialogInterface.BUTTON_NEGATIVE}, getAccentColor(), dialog);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    handler.removeCallbacks(slideShowRunnable);
  }

  private final class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
    @Override
    protected Bitmap doInBackground(String... params) {
      return BitmapUtils.getSampledBitmap(params[0], imageWidth / 4, imageHeight / 4);
    }

    @Override
    protected void onCancelled() {
      super.onCancelled();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCancelled(Bitmap result) {
      super.onCancelled(result);
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Bitmap result) {
      super.onPostExecute(result);
      if (mainBitmap != null) {
        mainBitmap.recycle();
        mainBitmap = null;
        System.gc();
      }
      mainBitmap = result;
      imgView.setImageBitmap(mainBitmap);
    }
  }

  private void setUpViewPager() {

    BasicCallBack basicCallBack =
        new BasicCallBack() {
          @Override
          public void callBack(int status, Object data) {
            toggleSystemUI();
          }
        };
    if (!allPhotoMode && !favphotomode) {
      adapter = new ImageAdapter(getAlbum().getMedia(), basicCallBack, this, this);
      getSupportActionBar()
          .setTitle(
              (getAlbum().getCurrentMediaIndex() + 1)
                  + " "
                  + getString(R.string.of)
                  + " "
                  + getAlbum().getMedia().size());
      mViewPager.setOnPageChangeListener(
          new PagerRecyclerView.OnPageChangeListener() {
            @Override
            public void onPageChanged(int oldPosition, int position) {
              getAlbum().setCurrentPhotoIndex(position);
              toolbar.setTitle(
                  (position + 1)
                      + " "
                      + getString(R.string.of)
                      + " "
                      + getAlbum().getMedia().size());
              invalidateOptionsMenu();
              pathForDescription = getAlbum().getMedia().get(position).getPath();
            }
          });
      mViewPager.scrollToPosition(getAlbum().getCurrentMediaIndex());
    } else if (allPhotoMode && !favphotomode) {
      adapter = new ImageAdapter(GalleryMainActivity.listAll, basicCallBack, this, this);
      getSupportActionBar()
          .setTitle(current_image_pos + 1 + " " + getString(R.string.of) + " " + size_all);
      mViewPager.setOnPageChangeListener(
          new PagerRecyclerView.OnPageChangeListener() {
            @Override
            public void onPageChanged(int oldPosition, int position) {
              current_image_pos = position;
              getAlbum().setCurrentPhotoIndex(getAlbum().getCurrentMediaIndex());
              toolbar.setTitle((position + 1) + " " + getString(R.string.of) + " " + size_all);
              invalidateOptionsMenu();
              pathForDescription = listAll.get(position).getPath();
            }
          });
      mViewPager.scrollToPosition(current_image_pos);
    } else if (!allPhotoMode && favphotomode) {
      adapter = new ImageAdapter(favouriteslist, basicCallBack, this, this);
      getSupportActionBar()
          .setTitle(current_image_pos + 1 + " " + getString(R.string.of) + " " + size_all);
      mViewPager.setOnPageChangeListener(
          new PagerRecyclerView.OnPageChangeListener() {
            @Override
            public void onPageChanged(int oldPosition, int position) {
              current_image_pos = position;
              getAlbum().setCurrentPhotoIndex(getAlbum().getCurrentMediaIndex());
              toolbar.setTitle((position + 1) + " " + getString(R.string.of) + " " + size_all);
              invalidateOptionsMenu();
              pathForDescription = favouriteslist.get(position).getPath();
            }
          });
      mViewPager.scrollToPosition(current_image_pos);
    }
    mViewPager.setAdapter(adapter);
  }
}

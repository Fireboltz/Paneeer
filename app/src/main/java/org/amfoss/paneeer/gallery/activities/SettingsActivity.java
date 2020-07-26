package org.amfoss.paneeer.gallery.activities;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import org.amfoss.paneeer.R;
import org.amfoss.paneeer.base.ThemedActivity;
import org.amfoss.paneeer.gallery.util.AlertDialogsHelper;
import org.amfoss.paneeer.gallery.util.PreferenceUtil;
import org.amfoss.paneeer.gallery.util.StaticMapProvider;
import org.amfoss.paneeer.opencamera.Camera.MyPreferenceFragment;
import org.amfoss.paneeer.opencamera.Camera.PreferenceKeys;
import org.amfoss.paneeer.utilities.ActivitySwitchHelper;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

@SuppressWarnings("ResourceAsColor")
public class SettingsActivity extends ThemedActivity {

  private PreferenceUtil SP;

  Toolbar toolbar;
  ScrollView scr;
  TextView txtGT;
  TextView txtPT;
  TextView txtAT;
  View parent;

  private SwitchCompat swMaxLuminosity;
  private SwitchCompat swPictureOrientation;
  private SwitchCompat swDelayFullImage;
  private SwitchCompat swAutoUpdate;
  private SwitchCompat swSwipeDirection;

  private boolean saf_dialog_from_preferences;

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);
      toolbar = findViewById(R.id.toolbar);
      scr = findViewById(R.id.settingAct_scrollView);
      txtGT = findViewById(R.id.general_setting_title);
      txtPT = findViewById(R.id.picture_setting_title);
      txtAT = findViewById(R.id.advanced_setting_title);
      parent = findViewById(R.id.setting_background);
    toolbar = findViewById(R.id.toolbar);
    SP = PreferenceUtil.getInstance(getApplicationContext());

    scr = findViewById(R.id.settingAct_scrollView);

    /** * CAMERA ** */
    findViewById(R.id.ll_camera)
        .setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                openCameraSetting();
              }
            });


    /** * EXCLUDED ALBUMS INTENT ** */
    findViewById(R.id.ll_excluded_album)
        .setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, ExcludedAlbumsActivity.class));
              }
            });

    /** * MAP PROVIDER DIALOG ** */
    findViewById(R.id.ll_map_provider)
        .setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                mapProviderDialog();
              }
            });

    /** * RESET SETTINGS ** */
    findViewById(R.id.ll_reset_settings)
        .setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                resetSettingsDialog();
              }
            });

    /** * SW SWIPE DIRECTION ** */
    swSwipeDirection = findViewById(R.id.Set_media_viewer_swipe_direction);
    swSwipeDirection.setChecked(
        SP.getBoolean(getString(R.string.preference_swipe_direction_inverted), false));
    swSwipeDirection.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SP.putBoolean(getString(R.string.preference_swipe_direction_inverted), isChecked);
            updateSwitchColor(swSwipeDirection, getAccentColor());
          }
        });

    /** * SW AUTO UPDATE MEDIA ** */
    swAutoUpdate = findViewById(R.id.SetAutoUpdateMedia);
    LinearLayout autoUpdateMedia = findViewById(R.id.ll_auto_update_media);
    autoUpdateMedia.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            boolean on = swAutoUpdate.isChecked();
            if (on) {
              swAutoUpdate.setChecked(false);
            } else {
              swAutoUpdate.setChecked(true);
            }
          }
        });
    swAutoUpdate.setChecked(SP.getBoolean(getString(R.string.preference_auto_update_media), false));
    swAutoUpdate.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SP.putBoolean(getString(R.string.preference_auto_update_media), isChecked);
            updateSwitchColor(swAutoUpdate, getAccentColor());
          }
        });

    /** * SW DELAY FULL-SIZE IMAGE ** */
    swDelayFullImage = findViewById(R.id.set_full_resolution);
    swDelayFullImage.setChecked(
        SP.getBoolean(getString(R.string.preference_delay_full_image), true));
    swDelayFullImage.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SP.putBoolean(getString(R.string.preference_delay_full_image), isChecked);
            updateSwitchColor(swDelayFullImage, getAccentColor());
          }
        });

    /** * SW PICTURE ORIENTATION ** */
    swPictureOrientation = findViewById(R.id.set_picture_orientation);
    LinearLayout pictureOrientation = findViewById(R.id.ll_switch_picture_orientation);
    pictureOrientation.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            boolean no = swPictureOrientation.isChecked();
            if (no) {
              swPictureOrientation.setChecked(false);
            } else {
              swPictureOrientation.setChecked(true);
            }
          }
        });
    swPictureOrientation.setChecked(
        SP.getBoolean(getString(R.string.preference_auto_rotate), false));
    swPictureOrientation.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SP.putBoolean(getString(R.string.preference_auto_rotate), isChecked);
            updateSwitchColor(swPictureOrientation, getAccentColor());
          }
        });

    /** * SW MAX LUMINOSITY ** */
    swMaxLuminosity = findViewById(R.id.set_max_luminosity);
    LinearLayout maxLuminosity = findViewById(R.id.ll_switch_max_luminosity);
    maxLuminosity.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            boolean no = swMaxLuminosity.isChecked();
            if (no) {
              swMaxLuminosity.setChecked(false);
            } else {
              swMaxLuminosity.setChecked(true);
            }
          }
        });
    swMaxLuminosity.setChecked(SP.getBoolean(getString(R.string.preference_max_brightness), false));
    swMaxLuminosity.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SP.putBoolean(getString(R.string.preference_max_brightness), isChecked);
            updateSwitchColor(swMaxLuminosity, getAccentColor());
          }
        });
  }

  private void mapProviderDialog() {

    final AlertDialog.Builder dialogBuilder =
        new AlertDialog.Builder(SettingsActivity.this, getDialogStyle());
    View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_map_provider, null);
    TextView dialogTitle = dialogLayout.findViewById(R.id.title);
    ((CardView) dialogLayout.findViewById(R.id.dialog_chose_provider_title))
        .setCardBackgroundColor(getCardBackgroundColor());
    dialogTitle.setBackgroundColor(getPrimaryColor());

    final RadioGroup mapProvider = dialogLayout.findViewById(R.id.radio_group_maps_provider);
    RadioButton radioGoogleMaps = dialogLayout.findViewById(R.id.radio_google_maps);
    RadioButton radioMapBoxStreets = dialogLayout.findViewById(R.id.radio_mapb_streets);
    RadioButton radioMapBoxDark = dialogLayout.findViewById(R.id.radio_mapb_dark);
    RadioButton radioMapBoxLight = dialogLayout.findViewById(R.id.radio_mapb_light);
    RadioButton radioTyler = dialogLayout.findViewById(R.id.radio_osm_tyler);
    setRadioTextButtonColor(radioGoogleMaps, getSubTextColor());
    setRadioTextButtonColor(radioMapBoxStreets, getSubTextColor());
    setRadioTextButtonColor(radioMapBoxDark, getSubTextColor());
    setRadioTextButtonColor(radioMapBoxLight, getSubTextColor());
    setRadioTextButtonColor(radioTyler, getSubTextColor());

    ((TextView) dialogLayout.findViewById(R.id.header_proprietary_maps))
        .setTextColor(getTextColor());
    ((TextView) dialogLayout.findViewById(R.id.header_free_maps)).setTextColor(getTextColor());
    switch (StaticMapProvider.fromValue(
        SP.getInt(
            getString(R.string.preference_map_provider),
            StaticMapProvider.GOOGLE_MAPS.getValue()))) {
      case GOOGLE_MAPS:
      default:
        radioGoogleMaps.setChecked(true);
        break;
      case MAP_BOX:
        radioMapBoxStreets.setChecked(true);
        break;
      case MAP_BOX_DARK:
        radioMapBoxDark.setChecked(true);
        break;
      case MAP_BOX_LIGHT:
        radioMapBoxLight.setChecked(true);
        break;
      case TYLER:
        radioTyler.setChecked(true);
        break;
    }

    dialogBuilder.setNegativeButton(getString(R.string.cancel).toUpperCase(), null);
    dialogBuilder.setPositiveButton(
        getString(R.string.ok_action).toUpperCase(),
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            switch (mapProvider.getCheckedRadioButtonId()) {
              case R.id.radio_google_maps:
              default:
                SP.putInt(
                    getString(R.string.preference_map_provider),
                    StaticMapProvider.GOOGLE_MAPS.getValue());
                break;
              case R.id.radio_mapb_streets:
                SP.putInt(
                    getString(R.string.preference_map_provider),
                    StaticMapProvider.MAP_BOX.getValue());
                break;
              case R.id.radio_osm_tyler:
                SP.putInt(
                    getString(R.string.preference_map_provider),
                    StaticMapProvider.TYLER.getValue());
                break;
              case R.id.radio_mapb_dark:
                SP.putInt(
                    getString(R.string.preference_map_provider),
                    StaticMapProvider.MAP_BOX_DARK.getValue());
                break;
              case R.id.radio_mapb_light:
                SP.putInt(
                    getString(R.string.preference_map_provider),
                    StaticMapProvider.MAP_BOX_LIGHT.getValue());
                break;
            }
          }
        });
    dialogBuilder.setView(dialogLayout);
    AlertDialog alertDialog = dialogBuilder.create();
    alertDialog.show();
    AlertDialogsHelper.setButtonTextColor(
        new int[] {DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE},
        getAccentColor(),
        alertDialog);
  }

  private void updateViewswithAccentColor(int color) {
    txtGT.setTextColor(color);
    txtPT.setTextColor(color);
    txtAT.setTextColor(color);

    updateSwitchColor(swDelayFullImage, color);
    updateSwitchColor(swMaxLuminosity, color);
    updateSwitchColor(swPictureOrientation, color);
    updateSwitchColor(swAutoUpdate, color);
    updateSwitchColor(swSwipeDirection, color);
  }

  @Override
  public void onPostResume() {
    super.onPostResume();
    ActivitySwitchHelper.setContext(this);
    setTheme();
  }

  private void setTheme() {

    /** BackGround * */
    findViewById(R.id.setting_background).setBackgroundColor(getBackgroundColor());

    /** Cards * */
    int color = getCardBackgroundColor();
    ((CardView) findViewById(R.id.general_setting_card)).setCardBackgroundColor(color);
    ((CardView) findViewById(R.id.preview_picture_setting_card)).setCardBackgroundColor(color);
    ((CardView) findViewById(R.id.advanced_setting_card)).setCardBackgroundColor(color);

    toolbar.setBackgroundColor(getPrimaryColor());
    setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(
        new IconicsDrawable(this)
            .icon(CommunityMaterial.Icon.cmd_arrow_left)
            .color(Color.WHITE)
            .sizeDp(19));
    toolbar.setNavigationOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            onBackPressed();
          }
        });

    setStatusBarColor();
    setNavBarColor();
    setRecentApp(getString(R.string.settings));
    setScrollViewColor(scr);
    updateViewswithAccentColor(getAccentColor());

    /** Icons * */
    color = getIconColor();
    ((IconicsImageView) findViewById(R.id.ll_switch_picture_orientation_icon)).setColor(color);
    ((IconicsImageView) findViewById(R.id.ll_switch_max_luminosity_icon)).setColor(color);
    ((IconicsImageView) findViewById(R.id.ll_switch_full_resolution_icon)).setColor(color);
    ((IconicsImageView) findViewById(R.id.excluded_album_icon)).setColor(color);
    ((IconicsImageView) findViewById(R.id.auto_update_media_Icon)).setColor(color);
    ((IconicsImageView) findViewById(R.id.camera_icon)).setColor(color);
    ((IconicsImageView) findViewById(R.id.map_provider_icon)).setColor(color);
    ((IconicsImageView) findViewById(R.id.media_viewer_swipe_direction_Icon)).setColor(color);
    ((IconicsImageView) findViewById(R.id.reset_settings_Icon)).setColor(color);

    /** TextViews * */
    color = getTextColor();
    ((TextView) findViewById(R.id.max_luminosity_Item)).setTextColor(color);
    ((TextView) findViewById(R.id.full_resolution_Item)).setTextColor(color);
    ((TextView) findViewById(R.id.picture_orientation_Item)).setTextColor(color);
    ((TextView) findViewById(R.id.Excluded_Album_Item_Title)).setTextColor(color);
    ((TextView) findViewById(R.id.auto_update_media_Item)).setTextColor(color);
    ((TextView) findViewById(R.id.camera_item_title)).setTextColor(color);
    ((TextView) findViewById(R.id.map_provider_item_title)).setTextColor(color);
    ((TextView) findViewById(R.id.media_viewer_swipe_direction_Item)).setTextColor(color);
    ((TextView) findViewById(R.id.reset_settings_Item)).setTextColor(color);

    /** Sub Text Views* */
    color = getSubTextColor();
    ((TextView) findViewById(R.id.max_luminosity_Item_Sub)).setTextColor(color);
    ((TextView) findViewById(R.id.full_resolution_Item_Sub)).setTextColor(color);
    ((TextView) findViewById(R.id.picture_orientation_Item_Sub)).setTextColor(color);
    ((TextView) findViewById(R.id.Excluded_Album_Item_Title_Sub)).setTextColor(color);
    ((TextView) findViewById(R.id.auto_update_media_Item_sub)).setTextColor(color);
    ((TextView) findViewById(R.id.map_provider_item_sub)).setTextColor(color);
    ((TextView) findViewById(R.id.media_viewer_swipe_direction_sub)).setTextColor(color);
    ((TextView) findViewById(R.id.camera_item_sub)).setTextColor(color);
    ((TextView) findViewById(R.id.reset_settings_Item_sub)).setTextColor(color);
  }

  @Override
  public void onBackPressed() {
    FragmentManager fm = getFragmentManager();
    if (fm.getBackStackEntryCount() > 0) {
      fm.popBackStack();
      if (fm.getBackStackEntryCount() == 0) findViewById(R.id.ll_camera).setVisibility(View.GONE);
      findViewById(R.id.settingAct_scrollView).setVisibility(View.VISIBLE);
      setToolbarCamera(false);
    } else {
        Intent homeIntent = new Intent(this, GalleryMainActivity.class);
        startActivity(homeIntent);
        finish();
    }
  }

  private void setToolbarCamera(Boolean isCamera) {
    getSupportActionBar();
    if (isCamera) toolbar.setTitle(getString(R.string.camera_setting_title));
    else toolbar.setTitle(getString(R.string.settings));
  }

  private void resetSettingsDialog() {

    final AlertDialog.Builder resetDialog =
        new AlertDialog.Builder(SettingsActivity.this, getDialogStyle());

    AlertDialogsHelper.getTextDialog(
        SettingsActivity.this, resetDialog, R.string.reset, R.string.reset_settings, null);

    resetDialog.setNegativeButton(this.getString(R.string.no_action).toUpperCase(), null);
    resetDialog.setPositiveButton(
        this.getString(R.string.yes_action).toUpperCase(),
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            SP.clearPreferences();
            recreate();
            Toast.makeText(getApplicationContext(), R.string.settings_reset, Toast.LENGTH_SHORT)
                .show();
          }
        });
    AlertDialog alertDialog = resetDialog.create();
    alertDialog.show();
    AlertDialogsHelper.setButtonTextColor(
        new int[] {DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE},
        getAccentColor(),
        alertDialog);
  }

  private void openCameraSetting() {
    setToolbarCamera(true);
    MyPreferenceFragment fragment = new MyPreferenceFragment();
    getFragmentManager()
        .beginTransaction()
        .add(R.id.pref_container, fragment, "PREFERENCE_FRAGMENT")
        .addToBackStack(null)
        .commitAllowingStateLoss();
    findViewById(R.id.settingAct_scrollView).setVisibility(View.GONE);
  }

  public boolean isUsingSAF() {
    // check Android version just to be safe
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      SharedPreferences sharedPreferences = getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getBoolean(PreferenceKeys.getUsingSAFPreferenceKey(), false);
    }
    return false;
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public void openFolderChooserDialogSAF(boolean from_preferences) {
    this.saf_dialog_from_preferences = from_preferences;
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
    // Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    // intent.addCategory(Intent.CATEGORY_OPENABLE);
    startActivityForResult(intent, 42);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
    if (requestCode == 42) {
      if (resultCode == RESULT_OK && resultData != null) {
        Uri treeUri = resultData.getData();
        // from
        // https://developer.android.com/guide/topics/providers/document-provider.html#permissions :
        final int takeFlags =
            resultData.getFlags()
                & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        // Check for the freshest data.
        getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PreferenceKeys.getSaveLocationSAFPreferenceKey(), treeUri.toString());
        editor.apply();

      } else {
        // cancelled - if the user had yet to set a save location, make sure we switch SAF back off
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(this);
        String uri =
            sharedPreferences.getString(PreferenceKeys.getSaveLocationSAFPreferenceKey(), "");
        if (uri.length() == 0) {
          SharedPreferences.Editor editor = sharedPreferences.edit();
          editor.putBoolean(PreferenceKeys.getUsingSAFPreferenceKey(), false);
          editor.apply();
        }
      }
    }
  }
}

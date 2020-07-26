package org.amfoss.paneeer;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import androidx.multidex.MultiDex;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import org.amfoss.paneeer.gallery.data.Album;
import org.amfoss.paneeer.gallery.data.HandlingAlbums;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class Paneeer extends Application {

  private HandlingAlbums albums = null;
  public static Context applicationContext;
  private RefWatcher refWatcher;

  public Album getAlbum() {
    return albums.dispAlbums.size() > 0 ? albums.getCurrentAlbum() : Album.getEmptyAlbum();
  }

  @Override
  public void onCreate() {

    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
    StrictMode.setVmPolicy(builder.build());

    if (LeakCanary.isInAnalyzerProcess(this)) {
      // This process is dedicated to LeakCanary for heap analysis.
      // You should not init your app in this process.
      return;
    }
    refWatcher = LeakCanary.install(this);
    albums = new HandlingAlbums(getApplicationContext());
    applicationContext = getApplicationContext();

    MultiDex.install(this);

    /** Realm initialization */
    Realm.init(this);
    RealmConfiguration realmConfiguration =
        new RealmConfiguration.Builder()
            .name("paneeer.realm")
            .schemaVersion(1)
            .deleteRealmIfMigrationNeeded()
            .build();
    Realm.getInstance(realmConfiguration);
    Realm.setDefaultConfiguration(realmConfiguration);
    super.onCreate();
  }

  public static RefWatcher getRefWatcher(Context context) {
    Paneeer paneeer = (Paneeer) context.getApplicationContext();
    return paneeer.refWatcher;
  }

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
  }

  public HandlingAlbums getAlbums() {
    return albums;
  }
}

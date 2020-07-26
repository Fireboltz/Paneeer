package org.amfoss.paneeer.base;

import android.os.Bundle;

import org.amfoss.paneeer.Paneeer;
import org.amfoss.paneeer.gallery.data.Album;
import org.amfoss.paneeer.gallery.data.HandlingAlbums;

public class SharedMediaActivity extends ThemedActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  public static HandlingAlbums getAlbums() {
    return ((Paneeer) Paneeer.applicationContext).getAlbums();
  }

  public Album getAlbum() {
    return ((Paneeer) getApplicationContext()).getAlbum();
  }
}

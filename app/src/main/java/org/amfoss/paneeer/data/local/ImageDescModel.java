package org.amfoss.paneeer.data.local;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ImageDescModel extends RealmObject {
  @PrimaryKey private String path;

  private String desc;

  public ImageDescModel() {}

  public ImageDescModel(String path, String title) {
    this.path = path;
    this.desc = title;
  }

  public String getId() {
    return path;
  }

  public void setId(String path) {
    this.path = path;
  }

  public String getTitle() {
    return desc;
  }

  public void setTitle(String description) {
    this.desc = description;
  }
}

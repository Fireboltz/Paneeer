package org.amfoss.paneeer.data.local;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class FavouriteImagesModel extends RealmObject {

  @PrimaryKey private String path;
  private String description;

  public FavouriteImagesModel() {}

  public void setPath(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}

package org.amfoss.paneeer.data.local;

import io.realm.Realm;

public class DatabaseHelper {

  private Realm realm;

  public DatabaseHelper(Realm realm) {
    this.realm = realm;
  }

  /**
   * Description is getting through the match of path of the image
   *
   * @param path Path passes as a parameter
   * @return model object
   */
  public ImageDescModel getImageDesc(String path) {
    ImageDescModel result = realm.where(ImageDescModel.class).equalTo("path", path).findFirst();
    return result;
  }

  public void update(ImageDescModel item) {
    realm.beginTransaction();
    realm.copyToRealmOrUpdate(item);
    realm.commitTransaction();
  }

  public void delete(ImageDescModel item) {
    realm.beginTransaction();
    item.deleteFromRealm();
    realm.commitTransaction();
  }
}

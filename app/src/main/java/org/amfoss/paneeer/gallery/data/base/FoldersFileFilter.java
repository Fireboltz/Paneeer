package org.amfoss.paneeer.gallery.data.base;

import java.io.File;
import java.io.FileFilter;

public class FoldersFileFilter implements FileFilter {
  @Override
  public boolean accept(File pathname) {
    return pathname.isDirectory();
  }
}

package org.amfoss.paneeer.utilities;

import android.content.Context;

public class ActivitySwitchHelper {
  public static Context context;

  public static Context getContext() {
    return context;
  }

  public static void setContext(Context context) {
    ActivitySwitchHelper.context = context;
  }
}

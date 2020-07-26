package org.amfoss.paneeer.utilities;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.view.View;

import org.amfoss.paneeer.R;

import java.util.Locale;

public class Utils {
  public static void promptSpeechInput(
      Activity activity, int requestCode, View parentView, String promtMsg) {
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(
        RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, promtMsg);
    try {
      activity.startActivityForResult(intent, requestCode);
    } catch (ActivityNotFoundException a) {
      SnackBarHandler.create(parentView, activity.getString(R.string.speech_not_supported)).show();
    }
  }
}

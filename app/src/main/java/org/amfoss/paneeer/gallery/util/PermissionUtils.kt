package org.amfoss.paneeer.gallery.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionUtils {
    @JvmStatic
    fun checkPermission(
        context: Context?,
        permission: String?
    ): Boolean {
        return (ContextCompat.checkSelfPermission(context!!, permission!!)
                == PackageManager.PERMISSION_GRANTED)
    }

    @JvmStatic
    fun isDeviceInfoGranted(context: Context?): Boolean {
        return checkPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
}
package org.amfoss.paneeer.editor.utils

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.text.TextUtils
import java.io.File

object FileUtil {
    fun checkFileExist(path: String?): Boolean {
        if (TextUtils.isEmpty(path)) return false
        val file = File(path)
        return file.exists()
    }

    // Get file extension
    fun getExtensionName(filename: String?): String {
        if (filename != null && filename.length > 0) {
            val dot = filename.lastIndexOf('.')
            if (dot > -1 && dot < filename.length - 1) {
                return filename.substring(dot + 1)
            }
        }
        return ""
    }

    /**
     * The image file is added to an album
     *
     * @param context
     * @param dstPath
     */
    @JvmStatic
    fun albumUpdate(context: Context?, dstPath: String?) {
        if (TextUtils.isEmpty(dstPath) || context == null) return
        val values = ContentValues(2)
        val extensionName =
            getExtensionName(dstPath)
        values.put(
            MediaStore.Images.Media.MIME_TYPE,
            "image/" + if (TextUtils.isEmpty(extensionName)) "jpeg" else extensionName
        )
        values.put(MediaStore.Images.Media.DATA, dstPath)
        context.contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }
}
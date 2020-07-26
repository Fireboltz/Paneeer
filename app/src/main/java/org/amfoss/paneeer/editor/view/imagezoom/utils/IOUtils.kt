package org.amfoss.paneeer.editor.view.imagezoom.utils

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import java.io.Closeable

object IOUtils {
    /**
     * Close a [Closeable] stream without throwing any exception
     *
     * @param c
     */
    @JvmStatic
    fun closeSilently(c: Closeable?) {
        if (c == null) return
        try {
            c.close()
        } catch (t: Throwable) {
        }
    }

    fun closeSilently(c: ParcelFileDescriptor?) {
        if (c == null) return
        try {
            c.close()
        } catch (t: Throwable) {
        }
    }

    fun closeSilently(cursor: Cursor?) {
        if (cursor == null) return
        try {
            cursor?.close()
        } catch (t: Throwable) {
        }
    }

    /**
     * Try to return the absolute file path from the given Uri
     *
     * @param context
     * @param uri
     * @return the file path or null
     */
    fun getRealFilePath(context: Context, uri: Uri?): String? {
        if (null == uri) return null
        val scheme = uri.scheme
        var data: String? = null
        if (scheme == null) data =
            uri.path else if (ContentResolver.SCHEME_FILE == scheme) {
            data = uri.path
        } else if (ContentResolver.SCHEME_CONTENT == scheme) {
            val cursor = context
                .contentResolver
                .query(
                    uri,
                    arrayOf(MediaStore.Images.ImageColumns.DATA),
                    null,
                    null,
                    null
                )
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    val index =
                        cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    if (index > -1) {
                        data = cursor.getString(index)
                    }
                }
                cursor.close()
            }
        }
        return data
    }
}
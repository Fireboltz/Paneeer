package org.amfoss.paneeer.gallery.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class PreferenceUtil private constructor(mContext: Context) {
    private val SP: SharedPreferences
    val editor: SharedPreferences.Editor
        get() = SP.edit()

    fun putString(key: String?, value: String?) {
        editor.putString(key, value).commit()
    }

    fun getString(key: String?, defValue: String?): String? {
        return SP.getString(key, defValue)
    }

    fun putInt(key: String?, value: Int) {
        editor.putInt(key, value).commit()
    }

    fun getInt(key: String?, defValue: Int): Int {
        return SP.getInt(key, defValue)
    }

    fun putBoolean(key: String?, value: Boolean) {
        editor.putBoolean(key, value).commit()
    }

    fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return SP.getBoolean(key, defValue)
    }

    fun remove(key: String?) {
        editor.remove(key).commit()
    }

    fun clearPreferences() {
        editor.clear().commit()
    }

    companion object {
        private var instance: PreferenceUtil? = null
        @JvmStatic
        fun getInstance(context: Context): PreferenceUtil? {
            if (instance == null) {
                synchronized(
                    PreferenceUtil::class.java
                ) {
                    if (instance == null) instance =
                        PreferenceUtil(context)
                }
            }
            return instance
        }
    }

    init {
        SP = PreferenceManager.getDefaultSharedPreferences(mContext)
    }
}
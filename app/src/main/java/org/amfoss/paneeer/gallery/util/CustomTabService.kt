package org.amfoss.paneeer.gallery.util

import android.app.Activity
import android.content.ComponentName
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import org.amfoss.paneeer.R

class CustomTabService(private val activity: Activity, private val color: Int) {
    private var mCustomTabsClient: CustomTabsClient? = null
    private var mCustomTabsSession: CustomTabsSession? = null
    private var mCustomTabsServiceConnection: CustomTabsServiceConnection? = null
    private var mCustomTabsIntent: CustomTabsIntent? = null
    private fun init() {
        mCustomTabsServiceConnection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(
                componentName: ComponentName, customTabsClient: CustomTabsClient
            ) {
                mCustomTabsClient = customTabsClient
                mCustomTabsClient!!.warmup(0L)
                mCustomTabsSession = mCustomTabsClient!!.newSession(null)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                mCustomTabsClient = null
            }
        }
        CustomTabsClient.bindCustomTabsService(
            activity, activity.packageName, mCustomTabsServiceConnection!!
        )
        mCustomTabsIntent = CustomTabsIntent.Builder(mCustomTabsSession)
            .setShowTitle(true)
            .setToolbarColor(color)
            .build()
    }

    fun launchUrl(Url: String?) {
        try {
            mCustomTabsIntent!!.launchUrl(activity, Uri.parse(Url))
        } catch (e: Exception) {
            Toast.makeText(
                activity.application,
                R.string.error_title.toString() + e.toString(),
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    init {
        init()
    }
}
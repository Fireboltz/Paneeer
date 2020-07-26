package org.amfoss.paneeer.gallery.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element
import org.amfoss.paneeer.BuildConfig
import org.amfoss.paneeer.R
import org.amfoss.paneeer.gallery.util.CustomTabService

class AboutActivity : AppCompatActivity() {
    var toolbar: Toolbar? = null
    var llAbout: LinearLayout? = null
    var customTabService: CustomTabService? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        toolbar = findViewById(R.id.toolbar)
        llAbout = findViewById(R.id.llAbout)
        customTabService = CustomTabService(
            this,
            resources.getColor(R.color.about_background_color)
        )
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle(R.string.about)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        simulateDayNight()
        val aboutPage = AboutPage(this)
            .isRTL(false)
            .setImage(R.mipmap.ic_launcher_round)
            .setDescription(getString(R.string.app_light_description))
            .addItem(
                Element(
                    BuildConfig.VERSION_NAME + "",
                    R.drawable.ic_release
                )
            )
            .create()
        llAbout?.addView(aboutPage)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    fun simulateDayNight() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
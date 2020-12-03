package com.schugarkub.dataguard.view.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.schugarkub.dataguard.DataGuardActivity
import com.schugarkub.dataguard.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        findViewById<MaterialToolbar>(R.id.settings_toolbar).apply {
            navigationIcon = ContextCompat.getDrawable(this@SettingsActivity, R.drawable.ic_back)
            setNavigationOnClickListener {
                navigateUpTo(Intent(this@SettingsActivity, DataGuardActivity::class.java))
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateUpTo(Intent(this@SettingsActivity, DataGuardActivity::class.java))
    }
}
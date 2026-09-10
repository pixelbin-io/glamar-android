package io.pixelbin.glamar.sample

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class StartActivity : AppCompatActivity() {
    private lateinit var experienceToggle: MaterialButtonToggleGroup
    private lateinit var launchButton: MaterialButton

    companion object {
        private const val STATE_EXPERIENCE = "selected_experience"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, maxOf(systemBars.bottom, keyboard.bottom))
            insets
        }
        experienceToggle = findViewById(R.id.experience_toggle)
        launchButton = findViewById(R.id.buttonNext)

        experienceToggle.addOnButtonCheckedListener { _, _, isChecked ->
            if (isChecked) updateExperience()
        }
        updateExperience()
        launchButton.setOnClickListener { launchExperience() }
        for (id in listOf(R.id.vto_access_key, R.id.skin_analysis_app_id)) {
            findViewById<TextInputEditText>(id).setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    launchExperience()
                    true
                } else {
                    false
                }
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        experienceToggle.check(savedInstanceState.getInt(STATE_EXPERIENCE, R.id.mode_vto))
        updateExperience()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_EXPERIENCE, experienceToggle.checkedButtonId)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        launchButton.isEnabled = true
    }

    private fun updateExperience() {
        val isSkinAnalysis = experienceToggle.checkedButtonId == R.id.mode_skin_analysis
        findViewById<View>(R.id.vto_fields).visibility = if (isSkinAnalysis) View.GONE else View.VISIBLE
        findViewById<View>(R.id.skin_analysis_fields).visibility = if (isSkinAnalysis) View.VISIBLE else View.GONE
        findViewById<TextView>(R.id.experience_description).setText(
            if (isSkinAnalysis) R.string.skin_analysis_description else R.string.vto_description
        )
        launchButton.setText(if (isSkinAnalysis) R.string.launch_skin_analysis else R.string.launch_vto)
    }

    private fun launchExperience() {
        if (!launchButton.isEnabled) return
        val (accessKey, appId) = readCredentials() ?: return
        launchButton.isEnabled = false
        startActivity(MainActivity.createIntent(this, accessKey, appId))
    }

    private fun readCredentials(): Pair<String, String?>? {
        val isSkinAnalysis = experienceToggle.checkedButtonId == R.id.mode_skin_analysis
        val accessKeyLayout = findViewById<TextInputLayout>(
            if (isSkinAnalysis) R.id.skin_analysis_access_key_layout else R.id.vto_access_key_layout
        )
        val accessKey = accessKeyLayout.editText?.text?.toString()?.trim().orEmpty()
        val appIdLayout = findViewById<TextInputLayout>(R.id.skin_analysis_app_id_layout)
        val appId = if (isSkinAnalysis) appIdLayout.editText?.text?.toString()?.trim().orEmpty() else null

        accessKeyLayout.error = if (accessKey.isEmpty()) getString(R.string.access_key_required) else null
        appIdLayout.error = if (isSkinAnalysis && appId.isNullOrEmpty()) getString(R.string.app_id_required) else null

        if (accessKey.isEmpty()) {
            accessKeyLayout.editText?.requestFocus()
            return null
        }
        if (isSkinAnalysis && appId.isNullOrEmpty()) {
            appIdLayout.editText?.requestFocus()
            return null
        }

        return accessKey to appId
    }

}

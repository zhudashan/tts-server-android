package com.github.jing332.tts_server_android.ui.preference

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.github.jing332.tts_server_android.AppLocale
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.constant.CodeEditorTheme
import com.github.jing332.tts_server_android.help.config.AppConfig
import com.github.jing332.tts_server_android.help.config.ScriptEditorConfig
import com.github.jing332.tts_server_android.help.config.SysTtsConfig
import com.github.jing332.tts_server_android.ui.preference.backup_restore.BackupRestoreActivity
import com.github.jing332.tts_server_android.ui.systts.direct_upload.DirectUploadSettingsActivity
import com.github.jing332.tts_server_android.ui.view.widget.ListChooseView
import com.github.jing332.tts_server_android.utils.dp
import com.github.jing332.tts_server_android.utils.longToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class SettingsFragment : PreferenceFragmentCompat() {
    companion object {
        private const val TAG = "SettingsFragment"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = AppConfig.kotprefName
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findPreference<Preference>(R.string.key_direct_link_settings)?.apply {
            setOnPreferenceClickListener {
                startActivity(Intent(requireContext(), DirectUploadSettingsActivity::class.java))
                true
            }
        }

        findPreference<Preference>(R.string.key_backup_and_restore)!!.apply {
            setOnPreferenceClickListener {
                startActivity(Intent(requireContext(), BackupRestoreActivity::class.java))
                true
            }
        }

        findPreference<ListPreference>(R.string.key_spinner_max_drop_down_count)!!.apply {
            entryValues = (0..50).map { "$it" }.toTypedArray()
            entries = entryValues

            setValue(AppConfig.spinnerMaxDropDownCount.toString(), "20")
            summary = entry
            setOnPreferenceChangeListener { _, newValue ->
                AppConfig.spinnerMaxDropDownCount = newValue.toString().toInt()
                true
            }
        }

        findPreference<ListPreference>(R.string.key_file_picker_mode)!!.apply {
            entryValues = (0..2).map { "$it" }.toTypedArray()
            entries = arrayOf(
                getString(R.string.file_picker_mode_prompt),
                getString(R.string.file_picker_mode_system),
                getString(R.string.file_picker_mode_builtin)
            )
            setValue(AppConfig.filePickerMode.toString(), "0")
            summary = entry
            setOnPreferenceChangeListener { _, newValue ->
                AppConfig.filePickerMode = newValue.toString().toInt()
                true
            }
        }

        initSwitchPreference(R.string.key_is_skip_silent_text, SysTtsConfig.isSkipSilentText) {
            SysTtsConfig.isSkipSilentText = it
        }

        initSwitchPreference(R.string.key_is_exo_decoder, SysTtsConfig.isExoDecoderEnabled) {
            SysTtsConfig.isExoDecoderEnabled = it
        }

        initSwitchPreference(
            R.string.key_is_stream_play_mode,
            SysTtsConfig.isStreamPlayModeEnabled
        ) {
            SysTtsConfig.isStreamPlayModeEnabled = it
        }

        findPreference<ListPreference>(R.string.key_max_empty_audio_retry_count)!!.apply {
            entries = buildList {
                add(getString(R.string.no_retries))
                addAll((1..10).map { "$it" })
            }.toTypedArray()

            entryValues = (0..10).map { "$it" }.toTypedArray()
            setValue(SysTtsConfig.maxEmptyAudioRetryCount.toString(), "1")
            summary = entry
            setOnPreferenceChangeListener { _, newValue ->
                SysTtsConfig.maxEmptyAudioRetryCount = (newValue as String).toInt()
                true
            }
        }

        findPreference<ListPreference>(R.string.key_max_retry_count)!!.apply {
            entries = buildList {
                add(getString(R.string.no_retries))
                addAll((1..50).map { "$it" })
            }.toTypedArray()

            entryValues = (0..50).map { "$it" }.toTypedArray()
            setValue(SysTtsConfig.maxRetryCount.toString(), "1")
            summary = entry
            setOnPreferenceChangeListener { _, newValue ->
                SysTtsConfig.maxRetryCount = (newValue as String).toInt()
                true
            }
        }

        // 备用配置使用条件
        findPreference<ListPreference>(R.string.key_standby_triggered_retry_index)!!.apply {
            entries = (1..10).map { "$it" }.toTypedArray()
            entryValues = entries
            setValue(SysTtsConfig.standbyTriggeredRetryIndex.toString(), "1")
            summary = entry
            setOnPreferenceChangeListener { _, newValue ->
                SysTtsConfig.standbyTriggeredRetryIndex = (newValue as String).toInt()
                true
            }
        }

        // 请求超时
        findPreference<ListPreference>(R.string.key_request_timeout)!!.apply {
            entries = (1..30).map { "${it}s" }.toTypedArray()
            entryValues = (1..30).map { "$it" }.toTypedArray()
            setValue((SysTtsConfig.requestTimeout / 1000).toString(), "5")
            summary = entry
            setOnPreferenceChangeListener { _, newValue ->
                SysTtsConfig.requestTimeout = (newValue as String).toInt() * 1000
                true
            }
        }

        initSwitchPreference(R.string.key_is_voice_multiple, SysTtsConfig.isVoiceMultipleEnabled) {
            SysTtsConfig.isVoiceMultipleEnabled = it
        }

        initSwitchPreference(R.string.key_is_group_multiple, SysTtsConfig.isGroupMultipleEnabled) {
            SysTtsConfig.isGroupMultipleEnabled = it
        }

        initSwitchPreference(R.string.key_is_wake_lock, SysTtsConfig.isWakeLockEnabled) {
            SysTtsConfig.isWakeLockEnabled = it
        }

        initSwitchPreference(
            R.string.key_is_foreground_service, SysTtsConfig.isForegroundServiceEnabled
        ) { SysTtsConfig.isForegroundServiceEnabled = it }

        // 代码编辑器主题
        val editorTheme: ListPreference = findPreference(R.string.key_code_editor_theme)!!
        val themes = linkedMapOf(
            CodeEditorTheme.AUTO to requireContext().getString(R.string.follow_system),
            CodeEditorTheme.QUIET_LIGHT to "Quiet-Light",
            CodeEditorTheme.SOLARIZED_DRAK to "Solarized-Dark",
            CodeEditorTheme.DARCULA to "Dracula",
            CodeEditorTheme.ABYSS to "Abyss"
        )
        editorTheme.entries = themes.values.toTypedArray()
        editorTheme.entryValues = themes.keys.map { it.toString() }.toTypedArray()
        editorTheme.setValueIndexNoException(ScriptEditorConfig.codeEditorTheme)
        editorTheme.summary = themes[ScriptEditorConfig.codeEditorTheme]
        editorTheme.setOnPreferenceChangeListener { _, newValue ->
            ScriptEditorConfig.codeEditorTheme = newValue.toString().toInt()
            editorTheme.setValueIndexNoException(ScriptEditorConfig.codeEditorTheme)
            true
        }

        val wordWrapSwitch =
            findPreference<SwitchPreferenceCompat>(R.string.key_is_code_editor_word_wrap)!!
        wordWrapSwitch.isChecked = ScriptEditorConfig.isCodeEditorWordWrapEnabled
        wordWrapSwitch.setOnPreferenceChangeListener { _, newValue ->
            ScriptEditorConfig.isCodeEditorWordWrapEnabled = newValue.toString().toBoolean()
            true
        }

        // 语言
        val langPre: ListPreference = findPreference(R.string.key_language)!!
        langPre.apply {
            entries =
                AppLocale.localeMap.map { "${it.value.displayName} - ${it.value.getDisplayName(it.value)}" }
                    .toMutableList()
                    .apply { add(0, getString(R.string.follow_system)) }.toTypedArray()
            entryValues =
                mutableListOf("").apply { addAll(AppLocale.localeMap.keys) }.toTypedArray()
            setValue(AppLocale.getLocaleCodeFromFile(requireContext()), "")
            summary = entry

            setOnPreferenceChangeListener { _, newValue ->
                AppLocale.saveLocaleCodeToFile(requireContext(), newValue as String)
                AppLocale.setLocale(app)
                longToast(R.string.app_language_update_warn)
                true
            }

        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is ListPreference) {
            val tvMsg =
                if (preference.dialogMessage.isNullOrEmpty()) null
                else TextView(requireContext()).apply {
                    setTypeface(null, Typeface.BOLD)
                    text = preference.dialogMessage
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    setPadding(/* left = */ 8.dp, /* top = */16.dp,
                        /* right = */8.dp, /* bottom = */8.dp
                    )
                }

            val chooseView = ListChooseView(preference.context).apply {
                isSearchFilterEnabled = false
                setItems(
                    preference.entries.map { it.toString() },
                    preference.findIndexOfValue(preference.value)
                )
            }

            val layout =
                LinearLayout(preference.context).apply {
                    orientation = LinearLayout.VERTICAL
                    tvMsg?.let { addView(it) }
                    addView(chooseView)
                }

            val dlg = MaterialAlertDialogBuilder(requireContext())
                .setTitle(preference.dialogTitle)
                .apply { preference.icon?.let { setIcon(it) } }
                .setView(layout)
                .setPositiveButton(android.R.string.cancel) { _, _ -> }
                .show()

            chooseView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                val value = preference.entryValues[position].toString()
                if (preference.callChangeListener(value)) {
                    preference.setValueIndexNoException(preference.findIndexOfValue(value))
                    preference.summary = preference.entries[position]
                }

                dlg.dismiss()
            }
        } else
            super.onDisplayPreferenceDialog(preference)
    }

    // 不抛异常
    private fun ListPreference.setValueIndexNoException(index: Int): Boolean {
        kotlin.runCatching {
            this.setValueIndex(index)
            return true
        }

        return false
    }

    private fun ListPreference.setValue(value: String, default: String) {
        kotlin.runCatching {
            this.value = value
        }.onFailure {
            this.value = default
        }
    }

    private fun initSwitchPreference(
        @StringRes key: Int,
        isChecked: Boolean = false,
        valueChange: (Boolean) -> Unit
    ) {
        initSwitchPreference(getString(key), isChecked, valueChange)
    }

    private fun initSwitchPreference(
        key: String,
        isChecked: Boolean = false,
        valueChange: (Boolean) -> Unit
    ) {
        findPreference<SwitchPreferenceCompat>(key)!!.init(isChecked) { valueChange.invoke(it) }
    }

    private fun SwitchPreferenceCompat.init(
        isChecked: Boolean = false,
        valueChange: (Boolean) -> Unit
    ) {
        this.isChecked = isChecked
        setOnPreferenceChangeListener { _, newValue ->
            valueChange.invoke(newValue.toString().toBoolean())
            true
        }
    }


    fun <T : Preference?> findPreference(@StringRes key: Int): T? {
        return findPreference(getString(key))
    }
}
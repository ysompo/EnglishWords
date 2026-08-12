package com.ysompo.englishwords.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ysompo.englishwords.databinding.ActivitySettingsBinding
import com.ysompo.englishwords.notification.ReminderScheduler
import com.ysompo.englishwords.settings.DifficultySettings
import com.ysompo.englishwords.settings.ProficiencyLevel
import com.ysompo.englishwords.settings.ReminderSettings

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var reminderSettings: ReminderSettings
    private lateinit var difficultySettings: DifficultySettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        reminderSettings = ReminderSettings(this)
        binding.reminderTimePicker.hour = reminderSettings.getReminderHour()
        binding.reminderTimePicker.minute = reminderSettings.getReminderMinute()

        difficultySettings = DifficultySettings(this)
        val checkedButtonId = when (difficultySettings.getLevel()) {
            ProficiencyLevel.BEGINNER -> binding.difficultyBeginner.id
            ProficiencyLevel.INTERMEDIATE -> binding.difficultyIntermediate.id
            ProficiencyLevel.ADVANCED -> binding.difficultyAdvanced.id
        }
        binding.difficultyRadioGroup.check(checkedButtonId)

        binding.saveButton.setOnClickListener {
            val hour = binding.reminderTimePicker.hour
            val minute = binding.reminderTimePicker.minute
            reminderSettings.setReminderTime(hour, minute)
            ReminderScheduler.schedule(this, hour, minute)

            val selectedLevel = when (binding.difficultyRadioGroup.checkedRadioButtonId) {
                binding.difficultyIntermediate.id -> ProficiencyLevel.INTERMEDIATE
                binding.difficultyAdvanced.id -> ProficiencyLevel.ADVANCED
                else -> ProficiencyLevel.BEGINNER
            }
            difficultySettings.setLevel(selectedLevel)

            finish()
        }
    }
}

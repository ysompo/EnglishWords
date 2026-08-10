package com.ysompo.englishwords.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ysompo.englishwords.databinding.ActivitySettingsBinding
import com.ysompo.englishwords.notification.ReminderScheduler
import com.ysompo.englishwords.settings.ReminderSettings

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var settings: ReminderSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settings = ReminderSettings(this)
        binding.reminderTimePicker.hour = settings.getReminderHour()
        binding.reminderTimePicker.minute = settings.getReminderMinute()

        binding.saveButton.setOnClickListener {
            val hour = binding.reminderTimePicker.hour
            val minute = binding.reminderTimePicker.minute
            settings.setReminderTime(hour, minute)
            ReminderScheduler.schedule(this, hour, minute)
            finish()
        }
    }
}

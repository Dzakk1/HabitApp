package com.dicoding.habitapp.ui.countdown

import android.net.Uri.Builder
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat.getParcelableExtra
import androidx.lifecycle.ViewModelProvider
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.dicoding.habitapp.R
import com.dicoding.habitapp.data.Habit
import com.dicoding.habitapp.notification.NotificationWorker
import com.dicoding.habitapp.utils.HABIT
import com.dicoding.habitapp.utils.HABIT_ID
import com.dicoding.habitapp.utils.HABIT_TITLE
import java.util.concurrent.TimeUnit

class CountDownActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_count_down)
        supportActionBar?.title = "Count Down"

        val habit = getParcelableExtra(intent, HABIT, Habit::class.java)

        if (habit != null){
            findViewById<TextView>(R.id.tv_count_down_title).text = habit.title

            val viewModel = ViewModelProvider(this).get(CountDownViewModel::class.java)

            //TODO 10 : Set initial time and observe current time. Update button state when countdown is finished

            viewModel.setInitialTime(habit.minutesFocus)

            viewModel.currentTimeString.observe(this) {time ->
                findViewById<TextView>(R.id.tv_count_down).text = time
            }

            //TODO 13 : Start and cancel One Time Request WorkManager to notify when time is up.

            viewModel.eventCountDownFinish.observe(this@CountDownActivity) {countDown ->
//                updateButtonState(it)
                if (countDown == true) {
                    val workManager = WorkManager.getInstance(this)
                    val builder = Data.Builder()
                        .putString(HABIT_TITLE,habit.title)
                        .putInt(HABIT_ID,habit.id)
                        .build()

                    val notificationWorker = OneTimeWorkRequest
                        .Builder(NotificationWorker::class.java)
                        .setInputData(builder)
                        .build()

                    workManager.enqueue(notificationWorker)
                }
            }

            findViewById<Button>(R.id.btn_start).setOnClickListener {
                viewModel.startTimer()
                updateButtonState(true)

//                val setTime = viewModel.getInitialTime()
//                if (setTime != null) {
//                    val data = workDataOf(HABIT_ID to habit.id, HABIT_TITLE to habit.title)
//
//                    val workRequest : WorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
//                        .setInitialDelay(setTime, TimeUnit.SECONDS)
//                        .setInputData(data)
//                        .build()
//                }

            }


            findViewById<Button>(R.id.btn_stop).setOnClickListener {
                viewModel.resetTimer()
                updateButtonState(false)
            }
        }

    }

    private fun updateButtonState(isRunning: Boolean) {
        findViewById<Button>(R.id.btn_start).isEnabled = !isRunning
        findViewById<Button>(R.id.btn_stop).isEnabled = isRunning
    }
}
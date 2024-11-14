package edu.temple.myapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var timerTextView: TextView

    private var timerService: TimerService.TimerBinder? = null
    private var isBound = false
    private var currentCount = 0

    private val timerHandler = Handler { msg ->
        currentCount = msg.what
        timerTextView.text = currentCount.toString()
        true
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            timerService = service as TimerService.TimerBinder
            isBound = true
            timerService?.setHandler(timerHandler)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            timerService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        timerTextView = findViewById(R.id.textView)

        Intent(this, TimerService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        startButton.setOnClickListener {
            if (isBound) {
                timerService?.let { binder ->
                    if (!binder.isRunning || binder.paused) {
                        binder.start(currentCount)
                        startButton.text = "Pause"
                    } else {
                        binder.pause()
                        startButton.text = "Resume"
                    }
                }
            }
        }

        stopButton.setOnClickListener {
            if (isBound) {
                timerService?.stop()
                currentCount = 0
                timerTextView.text = currentCount.toString()
                startButton.text = "Start"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }
}

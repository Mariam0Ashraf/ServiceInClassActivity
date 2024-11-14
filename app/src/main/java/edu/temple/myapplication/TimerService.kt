package edu.temple.myapplication

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log

class TimerService : Service() {

    private var isRunnin = false
    private var timerHandle: Handler? = null
    lateinit var t: TimerThread
    private var pause = false

    inner class TimerBinder : Binder() {
        val isRunning: Boolean get() = this@TimerService.isRunnin
        val paused: Boolean get() = this@TimerService.pause

        fun start(startValue: Int) {
            if (!paused) {
                if (!isRunning) {
                    if (::t.isInitialized) t.interrupt()
                    this@TimerService.start(startValue)
                }
            } else {
                pause()
            }
        }

        fun setHandler(handler: Handler) {
            timerHandle = handler
        }

        fun stop() {
            if (::t.isInitialized || isRunning) {
                t.interrupt()
            }
            pause = false
            isRunnin = false
        }

        fun pause() {
            this@TimerService.pause()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("TimerService status", "Created")
    }

    override fun onBind(intent: Intent): IBinder {
        return TimerBinder()
    }

    fun start(startValue: Int) {
        t = TimerThread(startValue)
        t.start()
    }

    fun pause() {
        if (::t.isInitialized) {
            pause = !pause
            isRunnin = !pause
        }
    }

    inner class TimerThread(private var currentValue: Int) : Thread() {
        override fun run() {
            isRunnin = true
            try {
                for (i in currentValue..100) {
                    if (!isRunnin) break
                    currentValue = i
                    timerHandle?.sendEmptyMessage(i)

                    while (pause) sleep(100)

                    sleep(100)
                }
                isRunnin = false
            } catch (e: InterruptedException) {
                Log.d("Timer interrupted", e.toString())
                isRunnin = false
                pause = false
            }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (::t.isInitialized) {
            t.interrupt()
        }
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("TimerService status", "Destroyed")
    }
}

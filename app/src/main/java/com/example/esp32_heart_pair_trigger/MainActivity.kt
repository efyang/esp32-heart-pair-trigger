package com.example.esp32_heart_pair_trigger

import android.icu.text.DateTimePatternGenerator
import android.os.Bundle
import android.provider.ContactsContract
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_main.view.*
import android.view.KeyEvent.KEYCODE_VOLUME_UP
import android.view.KeyEvent.KEYCODE_VOLUME_DOWN
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.doAsync

import java.lang.Exception
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress

class MainActivity : AppCompatActivity() {
    var triggerState: Boolean = false;
    var sendState: Boolean = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        triggerStateTextView.setText("Trigger State: " + triggerState);
        val socket = DatagramSocket();

        GlobalScope.launch {
            while (true) {
                delay(100);
                if (sendState == true) {
                    val bytes = toMessage(triggerState).toByteArray();
                    try {
                        socket.connect(InetSocketAddress(ip.text.toString(), port.text.toString().toInt()))
                        socket.send(DatagramPacket(bytes, bytes.size));
                    } catch (e: Exception) {
                        System.out.println(e);
                    }
                }
            }
        }

        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            sendState = isChecked;
        }

    }

    private fun toMessage(trigger: Boolean): String {
        var s = " " + System.currentTimeMillis().toString();
        if (trigger) {
            s = "TRIGGER" + s;
        } else {
            s = "STOP" + s;
        }
        return s;
    }

    var double_press_time_ms: Long = 500;
    var previous_vdown_time: Long = 0;
    var vdown_press_i: Int = 0;
    var previous_vup_time: Long = 0;
    var vup_press_i: Int = 0;
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            // ... your code
            vdown_press_i++;
            if (vdown_press_i == 1) {
                previous_vdown_time = System.currentTimeMillis();
            } else if (vdown_press_i == 2) {
                if ((System.currentTimeMillis() - previous_vdown_time) < double_press_time_ms) {
                    triggerState = true;
                    triggerStateTextView.setText("Trigger State: " + triggerState);
                }
                vdown_press_i = 0;
            }
            true
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            // ... your code
            vup_press_i++;
            if (vup_press_i == 1) {
                previous_vup_time = System.currentTimeMillis();
            } else if (vup_press_i == 2) {
                if ((System.currentTimeMillis() - previous_vup_time) < double_press_time_ms) {
                    triggerState = false;
                    triggerStateTextView.setText("Trigger State: " + triggerState);
                }
                vup_press_i = 0;
            }
            true
        } else
            super.onKeyDown(keyCode, event)
    }
}

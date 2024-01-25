package com.example.aimstar.inappmessage.android

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.aimstar.inappmessage.android.databinding.ActivityMainBinding
import jp.co.aimstar.messaging.android.AimstarInAppMessaging
import jp.co.aimstar.messaging.android.AimstarInAppMessagingListener
import jp.co.aimstar.messaging.android.data.http.AimstarException
import jp.co.aimstar.messaging.android.data.model.InAppMessage

class MainActivity : AppCompatActivity() {
    companion object {
        private const val API_KEY = ""
    }

    private val pref: SharedPreferences by lazy {
        getSharedPreferences(
            "my_pref",
            MODE_PRIVATE,
        )
    }

    private var tenantId: String?
        set(value) {
            pref.edit {
                putString("tenantId", value)
            }
        }
        get() = pref.getString("tenantId", null)

    private var customerId: String?
        set(value) {
            pref.edit {
                putString("customerId", value)
            }
        }
        get() = pref.getString("customerId", null)
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        AimstarInAppMessaging.listener = object : AimstarInAppMessagingListener {
            override fun messageDismissed(message: InAppMessage) {
                Log.d("MainActivity", "messageDismissed")
            }

            override fun messageClicked(message: InAppMessage) {
                Log.d("MainActivity", "messageClicked")
            }

            override fun messageDetectedForDisplay(message: InAppMessage) {
                Log.d("MainActivity", "messageDetectedForDisplay")
            }

            override fun messageError(message: InAppMessage?, error: AimstarException) {
                Log.d("MainActivity", "messageError")
            }
        }
        binding.button.apply {
            setOnClickListener {
                val tenantId = binding.tenantIdTextView.text.toString()
                AimstarInAppMessaging.setup(
                    context = applicationContext,
                    apiKey = API_KEY,
                    tenantId = tenantId
                )
                val customerId = binding.customerIdTextView.text.toString()
                AimstarInAppMessaging.customerId = customerId
                AimstarInAppMessaging.isStrictLogin = binding.strictLoginCheckbox.isChecked
                val screenName = binding.screenNameTextView.text.toString()
                AimstarInAppMessaging.fetch(activity = this@MainActivity, screenName = screenName)
            }
        }
    }
}
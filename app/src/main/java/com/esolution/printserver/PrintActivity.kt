package com.esolution.printserver

import PreferenceHelper.customPreference
import PreferenceHelper.enableAutoCut
import PreferenceHelper.enableBE
import PreferenceHelper.enableCashDrawer
import PreferenceHelper.enableTCPIP
import PreferenceHelper.enableUSB
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.URL
import java.util.concurrent.Executors


class PrintActivity : AppCompatActivity() {

    private val CUSTOM_PREF_NAME = "PRINT_SERVER"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        deepLinkHandler()
    }

    private fun deepLinkHandler() {
        val data = intent.data

        try {

            if(data?.getQueryParameter("src") != null) {

                Toast.makeText(this, "Send job to Printer", Toast.LENGTH_LONG).show()

                val executor = Executors.newSingleThreadExecutor()

                // use the executor on method
                executor.execute {
                    // network call
                    val apiResponse = URL(data.getQueryParameter("src")).readText()

                    runOnUiThread {
                        // update UI
                        if (apiResponse != "") {
                            val prefs = customPreference(this, CUSTOM_PREF_NAME)

                            val printController =
                                PrintController(this, this, prefs.enableAutoCut, prefs.enableCashDrawer)

                            if (prefs.enableBE) {
                                if (printController.checkBluetoothPermission()) {
                                    printController.print("bluetooth", apiResponse)
                                }
                            } else if (prefs.enableUSB) {
                                printController.print("usb", apiResponse)
                            } else if (prefs.enableTCPIP) {
                                printController.print("tcp", apiResponse)
                            }
                        }
                    }
                }

                finish()
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        } catch (ex: Exception) {
            finish()
        }

    }
}
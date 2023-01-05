package com.esolution.printserver

import PreferenceHelper.customPreference
import PreferenceHelper.printerDpi
import PreferenceHelper.printerIpAddress
import PreferenceHelper.printerNbrCharactersPerLine
import PreferenceHelper.printerPort
import PreferenceHelper.printerTimeout
import PreferenceHelper.printerWidthMM
import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Parcelable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.connection.tcp.TcpConnection
import com.dantsu.escposprinter.connection.usb.UsbConnection
import com.dantsu.escposprinter.connection.usb.UsbPrintersConnections

class PrintController(private val activity: Activity, private val context: Context, private val autoCut: Boolean, private val cashDrawer: Boolean) {

    private val PERMISSION_BLUETOOTH = 1
    private val PERMISSION_BLUETOOTH_CONNECT = 2
    private val PERMISSION_BLUETOOTH_ADMIN = 3
    private val PERMISSION_BLUETOOTH_SCAN = 4

    private val CUSTOM_PREF_NAME = "PRINT_SERVER"

    private val dotsFeedPaper = 20
    private val ACTION_USB_PERMISSION = "com.esolution.printserver.USB_PERMISSION"

    private var data = ""

    fun print(method: String, data: String) {

        when (method) {
            "bluetooth" -> {
                bluetooth(data)
            }
            "tcp" -> {
                tcp(data)
            }
            "usb" -> {
                usb(data)
            }
        }
    }

    private fun bluetooth(data: String) {
        val prefs = customPreference(context, CUSTOM_PREF_NAME)
        val printer = EscPosPrinter(BluetoothPrintersConnections.selectFirstPaired(),
            prefs.printerDpi, prefs.printerWidthMM.toFloat(), prefs.printerNbrCharactersPerLine)

        if (cashDrawer) {
            printer.printFormattedTextAndOpenCashBox(data, dotsFeedPaper)
        }  else if (autoCut) {
            printer.printFormattedTextAndCut(data, dotsFeedPaper)
        } else {
            printer.printFormattedText(data)
        }
    }

    fun checkBluetoothPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.BLUETOOTH), PERMISSION_BLUETOOTH)

            return false
        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.BLUETOOTH_ADMIN), PERMISSION_BLUETOOTH_ADMIN)

            return false
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), PERMISSION_BLUETOOTH_CONNECT)

            return false
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.BLUETOOTH_SCAN), PERMISSION_BLUETOOTH_SCAN)

            return false
        }

        return true
    }

    private fun tcp(data: String) {
        Thread {
            run {
                try {
                    val prefs = customPreference(context, CUSTOM_PREF_NAME)
                    val printer = EscPosPrinter(
                        TcpConnection(prefs.printerIpAddress, prefs.printerPort, prefs.printerTimeout),
                        prefs.printerDpi, prefs.printerWidthMM.toFloat(), prefs.printerNbrCharactersPerLine
                    )

                    if (cashDrawer) {
                        printer.printFormattedTextAndOpenCashBox(data, dotsFeedPaper)
                    }  else if (autoCut) {
                        printer.printFormattedTextAndCut(data, dotsFeedPaper)
                    } else {
                        printer.printFormattedText(data)
                    }
                } catch (_: Exception) {

                }
            }
        }
    }

    private val usbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    val usbManager = activity.getSystemService(Context.USB_SERVICE) as UsbManager?
                    val usbDevice =
                        intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (usbManager != null && usbDevice != null) {

                            val prefs = customPreference(context, CUSTOM_PREF_NAME)

                            val printer = EscPosPrinter(UsbConnection(usbManager, usbDevice),
                                prefs.printerDpi, prefs.printerWidthMM.toFloat(), prefs.printerNbrCharactersPerLine)

                            if (autoCut) {
                                printer.printFormattedTextAndCut(data, dotsFeedPaper)
                            } else if (cashDrawer) {
                                printer.printFormattedTextAndOpenCashBox(data, dotsFeedPaper)
                            } else {
                                printer.printFormattedText(data)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun usb(data: String) {

        this.data = data
        val usbConnection = UsbPrintersConnections.selectFirstConnected(context)
        val usbManager = activity.getSystemService(Context.USB_SERVICE) as UsbManager?
        if (usbConnection != null && usbManager != null) {
            val permissionIntent = PendingIntent.getBroadcast(
                context,
                0,
                Intent(ACTION_USB_PERMISSION),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
            )
            val filter = IntentFilter(ACTION_USB_PERMISSION)
            activity.registerReceiver(usbReceiver, filter)
            usbManager.requestPermission(usbConnection.device, permissionIntent)
        }
    }

}
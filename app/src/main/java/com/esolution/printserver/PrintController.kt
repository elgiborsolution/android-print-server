package com.esolution.printserver

import PreferenceHelper.customPreference
import PreferenceHelper.printerDpi
import PreferenceHelper.printerIpAddress
import PreferenceHelper.printerNbrCharactersPerLine
import PreferenceHelper.printerPort
import PreferenceHelper.printerTimeout
import PreferenceHelper.printerWidthMM
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.connection.tcp.TcpConnection

class PrintController(private val context: Context, private val autoCut: Boolean, private val cashDrawer: Boolean) {

    val CUSTOM_PREF_NAME = "PRINT_SERVER"
    private val dotsFeedPaper = 20
    private val ACTION_USB_PERMISSION = "com.esolution.printserver.USB_PERMISSION"

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
                } catch (e: Exception) {

                }
            }
        }
    }

    private val usbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
//            val action = intent.action
//            if (ACTION_USB_PERMISSION.equals(action)) {
//                synchronized(this) {
//                    val usbManager = getSystemService(context.USB_SERVICE) as UsbManager?
//                    val usbDevice =
//                        intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
//                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
//                        if (usbManager != null && usbDevice != null) {
//                            val printer = EscPosPrinter(UsbConnection(usbManager, usbDevice), 203, 48f, 32)
//            if (autoCut) {
//                printer.printFormattedTextAndCut(data, dotsFeedPaper)
//            } else if (cashDrawer) {
//                printer.printFormattedTextAndOpenCashBox(data, dotsFeedPaper)
//            } else {
//                printer.printFormattedText(data)
//            }
//                        }
//                    }
//                }
//            }
        }
    }

    private fun usb(data: String) {
//        val usbConnection = UsbPrintersConnections.selectFirstConnected(this)
//        val usbManager = getActivity().getSystemService(Context.USB_SERVICE) as UsbManager?
//        if (usbConnection != null && usbManager != null) {
//            val permissionIntent = PendingIntent.getBroadcast(
//                this,
//                0,
//                Intent(ACTION_USB_PERMISSION),
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
//            )
//            val filter = IntentFilter(ACTION_USB_PERMISSION)
//            registerReceiver(usbReceiver, filter)
//            usbManager.requestPermission(usbConnection.device, permissionIntent)
//        }
    }

}
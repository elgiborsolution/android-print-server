package com.esolution.printserver

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import PreferenceHelper.customPreference
import PreferenceHelper.enableAutoCut
import PreferenceHelper.enableBE
import PreferenceHelper.enableCashDrawer
import PreferenceHelper.enableTCPIP
import PreferenceHelper.enableUSB
import PreferenceHelper.numberOfCopiesToPrint
import PreferenceHelper.printerDpi
import PreferenceHelper.printerIpAddress
import PreferenceHelper.printerNbrCharactersPerLine
import PreferenceHelper.printerPort
import PreferenceHelper.printerTimeout
import PreferenceHelper.printerWidthMM
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.URL
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private var mHttpServer: HttpServer? = null
    private var serverUp = false
    private var port = 5000

    private val CUSTOM_PREF_NAME = "PRINT_SERVER"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val prefs = customPreference(this, CUSTOM_PREF_NAME)

        val btnServerStart = findViewById<Button>(R.id.btnServerStart)
        val btnTestPrint = findViewById<Button>(R.id.btnTestPrint)

        val enableBE = findViewById<Switch>(R.id.enableBE)
        val enableUSB = findViewById<Switch>(R.id.enableUSB)
        val enableTCPIP = findViewById<Switch>(R.id.enableTCPIP)

        val printerIpAddress = findViewById<EditText>(R.id.printerIpAddress)
        val printerPort = findViewById<EditText>(R.id.printerPort)
        val printerTimeout = findViewById<EditText>(R.id.printerTimeout)

        val printerDpi = findViewById<EditText>(R.id.printerDpi)
        val printerWidthMM = findViewById<EditText>(R.id.printerWidthMM)
        val printerNbrCharactersPerLine = findViewById<EditText>(R.id.printerNbrCharactersPerLine)

        val enableAutoCut = findViewById<Switch>(R.id.enableAutoCut)
        val enableCashDrawer = findViewById<Switch>(R.id.enableCashDrawer)

        val numberOfCopiesToPrint = findViewById<EditText>(R.id.numberOfCopiesToPrint)

        enableBE.isChecked = prefs.enableBE
        enableUSB.isChecked = prefs.enableUSB
        enableTCPIP.isChecked = prefs.enableTCPIP

        enableAutoCut.isChecked = prefs.enableAutoCut
        enableCashDrawer.isChecked = prefs.enableCashDrawer

        printerIpAddress.setText(prefs.printerIpAddress.toString())
        printerTimeout.setText(prefs.printerTimeout.toString())
        printerPort.setText(prefs.printerPort.toString())

        printerDpi.setText(prefs.printerDpi.toString())
        printerWidthMM.setText(prefs.printerWidthMM.toString())
        printerNbrCharactersPerLine.setText(prefs.printerNbrCharactersPerLine.toString())
        numberOfCopiesToPrint.setText(prefs.numberOfCopiesToPrint.toString())

        btnServerStart.setOnClickListener {
            serverUp = if(!serverUp){
                startServer(port)
                btnServerStart.setText(R.string.btn_server_stop)
                findViewById<TextView>(R.id.printServerDesc).text = "Listening on port 5000..."
                true
            } else{
                stopServer()
                btnServerStart.setText(R.string.btn_server_start)
                findViewById<TextView>(R.id.printServerDesc).text = "Click Start Server to open print API at port 5000"
                false
            }
        }

        btnTestPrint.setOnClickListener {

            try {

                val executor = Executors.newSingleThreadExecutor()

                // use the executor on method
                executor.execute {
                    // network call
                    val apiResponse = URL("https://elgibor-solution.com/test-print.php").readText()

                    runOnUiThread {
                        // update UI
                        doPrint(apiResponse)

                        Toast.makeText(this, "Send job to Printer", Toast.LENGTH_LONG).show()

                    }
                }

            } catch (ex: Exception) {
                finish()
            }
        }

        enableBE.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                prefs.enableBE = true
                prefs.enableTCPIP = false
                prefs.enableUSB = false

                enableUSB.isChecked = false
                enableTCPIP.isChecked = false
            } else {
                prefs.enableBE = false
            }
        }

        enableUSB.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                prefs.enableBE = false
                prefs.enableTCPIP = false
                prefs.enableUSB = true

                enableBE.isChecked = false
                enableTCPIP.isChecked = false
            } else {
                prefs.enableUSB = false
            }
        }

        enableTCPIP.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                prefs.enableBE = false
                prefs.enableTCPIP = true
                prefs.enableUSB = false

                enableBE.isChecked = false
                enableUSB.isChecked = false
            } else {
                prefs.enableTCPIP = false
            }
        }

        enableAutoCut.setOnCheckedChangeListener { _, isChecked ->
            prefs.enableAutoCut = isChecked
        }

        enableCashDrawer.setOnCheckedChangeListener { _, isChecked ->
            prefs.enableCashDrawer = isChecked
        }

        printerIpAddress.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                prefs.printerIpAddress = s.toString()
            }
        })

        printerPort.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                prefs.printerPort = Integer.parseInt(s.toString())
            }
        })

        printerTimeout.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                prefs.printerTimeout = Integer.parseInt(s.toString())
            }
        })

        printerDpi.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                prefs.printerDpi = Integer.parseInt(s.toString())
            }
        })

        printerWidthMM.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                prefs.printerWidthMM = Integer.parseInt(s.toString())
            }
        })

        printerNbrCharactersPerLine.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                prefs.printerNbrCharactersPerLine = Integer.parseInt(s.toString())
            }
        })

        numberOfCopiesToPrint.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                prefs.numberOfCopiesToPrint = Integer.parseInt(s.toString())
            }
        })
    }

    private fun streamToString(inputStream: InputStream): String {
        val s = Scanner(inputStream).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }

    private fun sendResponse(httpExchange: HttpExchange, responseText: String){
        httpExchange.sendResponseHeaders(200, responseText.length.toLong())
        val os = httpExchange.responseBody
        os.write(responseText.toByteArray())
        os.close()
    }

    private fun startServer(port: Int) {
        try {
            mHttpServer = HttpServer.create(InetSocketAddress(port), 0)
            mHttpServer!!.executor = Executors.newCachedThreadPool()

            mHttpServer!!.createContext("/", rootHandler)
            mHttpServer!!.createContext("/index", rootHandler)
            // Handle /messages endpoint
            mHttpServer!!.createContext("/print", printHandler)
            mHttpServer!!.start()//startServer server;


        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun stopServer() {
        if (mHttpServer != null){
            mHttpServer!!.stop(0)
        }
    }

    // Handler for root endpoint
    private val rootHandler = HttpHandler { exchange ->
        run {
            // Get request method
            when (exchange!!.requestMethod) {
                "GET" -> {
                    sendResponse(exchange, "Android Print Server")
                }
            }
        }
    }

    private val printHandler = HttpHandler { httpExchange ->
        run {
            when (httpExchange!!.requestMethod) {
                "GET" -> {
                    // Get all messages
                    sendResponse(httpExchange, "Would be all messages stringifies json")
                }
                "POST" -> {
                    val inputStream = httpExchange.requestBody

                    val requestBody = streamToString(inputStream)
                    val jsonBody = JSONObject(requestBody)
                    // save message to database

                    doPrint(requestBody)

                    //for testing
                    sendResponse(httpExchange, "Send Job to printer")
                }
            }
        }
    }

    private fun doPrint(requestBody: String) {
        val prefs = customPreference(this, CUSTOM_PREF_NAME)

        val printController = PrintController(this, prefs.enableAutoCut, prefs.enableCashDrawer)

        if(prefs.enableBE) {
            printController.print("bluetooth", requestBody)
        } else if (prefs.enableUSB) {
            printController.print("usb", requestBody)
        } else if (prefs.enableTCPIP) {
            printController.print("tcp", requestBody)
        }
    }
}



import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object PreferenceHelper {

    fun defaultPreference(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun customPreference(context: Context, name: String): SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    inline fun SharedPreferences.editMe(operation: (SharedPreferences.Editor) -> Unit) {
        val editMe = edit()
        operation(editMe)
        editMe.apply()
    }

    var SharedPreferences.enableBE
        get() = getBoolean("enableBE", false)
        set(value) {
            editMe {
                it.putBoolean("enableBE", value)
            }
        }

    var SharedPreferences.enableUSB
        get() = getBoolean("enableUSB", false)
        set(value) {
            editMe {
                it.putBoolean("enableUSB", value)
            }
        }

    var SharedPreferences.enableTCPIP
        get() = getBoolean("enableTCPIP", false)
        set(value) {
            editMe {
                it.putBoolean("enableTCPIP", value)
            }
        }

    var SharedPreferences.enableAutoCut
        get() = getBoolean("enableAutoCut", false)
        set(value) {
            editMe {
                it.putBoolean("enableAutoCut", value)
            }
        }

    var SharedPreferences.enableCashDrawer
        get() = getBoolean("enableCashDrawer", false)
        set(value) {
            editMe {
                it.putBoolean("enableCashDrawer", value)
            }
        }

    var SharedPreferences.printerIpAddress
        get() = getString("printerIpAddress", "test")
        set(value) {
            editMe {
                it.putString("printerIpAddress", value)
            }
        }

    var SharedPreferences.printerPort
        get() = getInt("printerPort", 9300)
        set(value) {
            editMe {
                it.putInt("printerPort", value)
            }
        }

    var SharedPreferences.printerTimeout
        get() = getInt("printerTimeout", 15)
        set(value) {
            editMe {
                it.putInt("printerTimeout", value)
            }
        }

    var SharedPreferences.printerDpi
        get() = getInt("printerDpi", 203)
        set(value) {
            editMe {
                it.putInt("printerDpi", value)
            }
        }

    var SharedPreferences.printerWidthMM
        get() = getInt("printerWidthMM", 48)
        set(value) {
            editMe {
                it.putInt("printerWidthMM", value)
            }
        }

    var SharedPreferences.printerNbrCharactersPerLine
        get() = getInt("printerNbrCharactersPerLine", 32)
        set(value) {
            editMe {
                it.putInt("printerNbrCharactersPerLine", value)
            }
        }

    var SharedPreferences.numberOfCopiesToPrint
        get() = getInt("numberOfCopiesToPrint", 1)
        set(value) {
            editMe {
                it.putInt("numberOfCopiesToPrint", value)
            }
        }

    var SharedPreferences.clearValues
        get() = { }
        set(value) {
            editMe {
                it.clear()
            }
        }
}
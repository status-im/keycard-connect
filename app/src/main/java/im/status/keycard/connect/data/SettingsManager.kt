package im.status.keycard.connect.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SettingsManager(context: Context) {
    private val sharedPreferences: SharedPreferences

    init {
        /** encrypted settings used for privacy **/
        val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        sharedPreferences = EncryptedSharedPreferences.create(context,"settings", masterKey, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    }

    val rpcEndpoint
        get() = String.format(RPC_ENDPOINT_TEMPLATE, CHAIN_ID_TO_SHORTNAME.getValue(sharedPreferences.getLong(SETTINGS_CHAIN_ID, DEFAULT_CHAIN_ID)))

    var chainID
        get() = sharedPreferences.getLong(SETTINGS_CHAIN_ID, DEFAULT_CHAIN_ID)
        set(chainID) {
            sharedPreferences.edit().apply {
                putLong(SETTINGS_CHAIN_ID, chainID)
                apply()
            }
        }

    var bip32Path
        get() = sharedPreferences.getString(SETTINGS_BIP32_PATH, DEFAULT_BIP32_PATH)!!
        set(bip32Path) {
            sharedPreferences.edit().apply {
                putString(SETTINGS_BIP32_PATH, bip32Path)
                apply()
            }
        }
}
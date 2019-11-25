package im.status.keycard.connect.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class SettingsManager(context: Context) {
    private val sharedPreferences: SharedPreferences

    init {
        /** encrypted settings used for privacy **/
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        sharedPreferences = EncryptedSharedPreferences.create("settings", masterKeyAlias, context, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
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
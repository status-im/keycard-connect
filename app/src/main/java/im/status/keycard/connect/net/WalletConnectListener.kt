package im.status.keycard.connect.net

interface WalletConnectListener {
    fun onConnected()
    fun onDisconnected()
    fun onAccountChanged(account: String?)
}
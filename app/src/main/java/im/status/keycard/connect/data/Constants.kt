package im.status.keycard.connect.data

const val PAIRING_ACTIVITY_PASSWORD = "pairingPassword"

const val PIN_ACTIVITY_ATTEMPTS = "remainingAttempts"
const val PIN_ACTIVITY_CARD_UID = "cardUID"

const val PUK_ACTIVITY_ATTEMPTS = PIN_ACTIVITY_ATTEMPTS

const val INIT_ACTIVITY_PIN = "initPIN"
const val INIT_ACTIVITY_PUK = "initPUK"
const val INIT_ACTIVITY_PAIRING = "initPairing"

const val SIGN_TEXT_MESSAGE = "signMessage"

const val SIGN_TX_AMOUNT = "signTxAmount"
const val SIGN_TX_CURRENCY = "signTxCurrency"
const val SIGN_TX_DATA = "signTxData"
const val SIGN_TX_TO = "signTxTo"

const val LOAD_TYPE = "loadKeyType"
const val LOAD_NONE = -1
const val LOAD_IMPORT_BIP39 = 0
const val LOAD_GENERATE_BIP39 = 1
const val LOAD_GENERATE = 2
const val LOAD_MNEMONIC = "loadKeyMnemonic"

const val MNEMONIC_PHRASE = "mnemonicPhrase"

const val REQ_INTERACTIVE_SCRIPT = 0x01
const val REQ_WALLETCONNECT = 0x02
const val REQ_LOADKEY = 0x03
const val REQ_APPLET_FILE = 0x04

const val CACHE_VALIDITY = 15 * 60 * 1000

const val SETTINGS_CHAIN_ID = "chainID"
const val SETTINGS_BIP32_PATH = "bip32Path"

const val INFURA_API_KEY = "27efcb33f94e4bd0866d1aadf8e1a12d"
const val RPC_ENDPOINT_TEMPLATE = "https://%s.infura.io/v3/${INFURA_API_KEY}"

const val CHAIN_ID_MAINNET = 1L
const val CHAIN_ID_ROPSTEN = 3L
const val CHAIN_ID_RINKEBY = 4L
const val CHAIN_ID_GOERLI = 5L
const val CHAIN_ID_KOVAN = 42L

val CHAIN_ID_TO_SHORTNAME = mapOf(CHAIN_ID_MAINNET to "mainnet", CHAIN_ID_ROPSTEN to "ropsten", CHAIN_ID_RINKEBY to "rinkeby", CHAIN_ID_GOERLI to "goerli", CHAIN_ID_KOVAN to "kovan")
val CHAIN_IDS = listOf(CHAIN_ID_MAINNET, CHAIN_ID_ROPSTEN, CHAIN_ID_RINKEBY, CHAIN_ID_GOERLI, CHAIN_ID_KOVAN)

const val DEFAULT_CHAIN_ID = CHAIN_ID_MAINNET
const val DEFAULT_BIP32_PATH = "m/44'/60'/0'/0/0"
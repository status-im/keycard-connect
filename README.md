# Keycard Connect

**NOTE**: _This app is a prototype. While it seems to work reasonably well and stable, the UI has not received any thought yet. 
It is fine to use but keep in mind that there isn't much error reporting, no progress indicators and some screens are crammed with info_

Keycard Connect's main function is to enable using the Keycard with dApps outside of a web3 browser using WalletConnect. The dApp must of course support WalletConnect for this to work. Additionally, this app is a little toolbox for Keycard, allowing things like changing PIN, initializing cards, changing seed etc.

## What works

### WalletConnect integration
At the moment all dApps tested worked fine. The implemented functions are:

* eth_sendTransaction
* eth_sendRawTransaction
* eth_signTransaction
* eth_sign
* eth_signTypedData
* personal_sign

### Seed management
* import a BIP39 mnemonic seed to a card
* create a random BIP39 mnemonic seed and import it on a card
* generate a random key on a card with no master key
* change seed at any moment
* remove seed

### Keycard management
* initialize an out-of-factory card with random PIN/PUK/pairing
* pairing (to an already intialized car)
* Unblock PIN with PUK
* Change PIN
* change PUK
* change pairing password
* unpair keycard tapped
* unpair keycard tapped from other clients/devices
* reinstall applet (destroys all data on card)
* allow switching account (BIP32 path) on card and used network (for WalletConnect)

## What needs to be done for 1.0
* design and implement a user-friendly UI

## Future plans (after 1.0)
* show account balance
* assign names to paired card

## Installation
Download the latest APK from the release pages and install on your phone.

## Usage
Select the button for the function you need and follow the instructions. An important thing to know is that whenever you need to tap your card you will get a prompt. When you tap the card, this prompt will remain until you can remove the card. Don't remove the card until this screen disappears by itself.

**WARNING**: Reinstalling the applet over NFC is slower and more dangerous than doing it over USB. Do it only if you have no choice. Especially on JCOP3 cards, make extra sure you do not remove the card until the "Tap your card" screen disappears, because losing connection during certain operations can kill the card.
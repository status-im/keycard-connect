# Keycard Connect

Keycard Connect's main function is to enable using the Keycard with dApps outside of a web3 browser using WalletConnect. The dApp must of course support WalletConnect for this to work. Additionally, this app is a little toolbox for Keycard, allowing things like changing PIN, initializing cards, changing seed etc.

## Installation
Download the latest APK from the release pages and install on your phone.

## Screenshots
<img align="center" width="30%" hspace="1%" src="https://user-images.githubusercontent.com/12899729/96104664-6891a680-0ed9-11eb-8f29-f2910dd4c5ca.jpg">
<img align="center" width="30%" hspace="1%" src="https://user-images.githubusercontent.com/12899729/96104738-7a734980-0ed9-11eb-8de8-b88836e71e7e.png">
<img align="center" width="30%" hspace="1%" src="https://user-images.githubusercontent.com/12899729/96104797-8c54ec80-0ed9-11eb-89d9-bf97675d7934.png">

## Features

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
* pairing (to an already intialized card)
* Unblock PIN with PUK
* Change PIN
* change PUK
* change pairing password
* unpair keycard tapped
* unpair keycard tapped from other clients/devices
* reinstall applet (destroys all data on card)
* allow switching account (BIP32 path) on card and used network (for WalletConnect)

## What needs to be done for 1.0
* redesign the load/change key screen (more input validation)
* implement mnemonic confirmation (now it is too easy to skip)
* improve progress indication (especially relevant for long operations)
* improve transaction screen to show ERC20 icon (when available)
* improve sign message screen to display EIP-712 data nicely

## Future plans (after 1.0)
* show account balance
* assign names to paired card

## Usage
Select the button for the function you need and follow the instructions. An important thing to know is that whenever you need to tap your card you will get a prompt. When you tap the card, this prompt will remain until you can remove the card. Don't remove the card until this screen disappears by itself.

**WARNING**: Reinstalling the applet over NFC is slower and more dangerous than doing it over USB. Do it only if you have no choice. Especially on JCOP3 cards, make extra sure you do not remove the card until the "Tap your card" screen disappears, because losing connection during certain operations can kill the card.

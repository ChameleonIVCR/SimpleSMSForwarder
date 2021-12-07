# SimpleSMSForwarder

SSF is an Android app to forward messages from a Socket.IO server, to SMS using the phone radio. It's compatible with Android API 17, all the way to API 30.

## Getting started

Install the application on the Android device (see https://github.com/ChameleonIVCR/SimpleSMSForwarder/releases), and run it. If it's your first time running the
application, you will be asked for permissions, and will then be prompted for a "Testing phone number". This phone number will be used to check if the GSM is up,
therefore sending constant network status messages to that number. Note that if you did not introduce it when prompted to do so, you will have to do it later
at the settings screen.

The login screen will prompt you to introduce the socket server IP, port, token, and to provide YOUR device phone number. Note that this could be different to
the testing phone number.

After logging in, the status screen has two main icons, Socket and GSM. The socket icon represents the socket client connection to the server. The GSM icon 
represents the phone radio, and SMS sending capabilities. Green means online, red means offline. The app will only operate if both Socket and GSM are online.

## Features

Timeouts:

- A brief timeout (5 seconds) will be triggered if an SMS failed to send.
- If an SMS fails 3 times, and goes through 3 timeouts, a network check will be triggered.
- A long timeout (2 minutes) will be triggered if a network check failed to complete.
- If a network check fails to complete after a long timeout, it will schedule another, and repeat until network is online.
- Execution will be resumed after the timeout ends.

 SMS:
 - SMS have a 160 character hard limit, if the message is longer, thee 161th character and onwards will be lost.
 - The simcard used will be the default set for SMS in Android settings, or the one available.
 - Network check SMS sent to the test phone number include a Unix timestamp to bring uniqueness to the message, and avoid triggering network restrictions.

## Features

- Socket.IO client
- SMS radio controller
- Operational status display
- Persistent configuration
- SMS rate limit
- SMS status messages

## TODO

- Notifications fragment
- WebSocket client variation
- Keep-alive Android notification
- Message rescheduling
- Add configurable country codes (+1 and +57 are currently hardcoded)

## Disclaimer

Please note that SMS rates will be charged by your mobile network provider, and the app will NOT limit the
amount of messages sent, but will rate limit them instead. We enforce fair usage, but ultimately, your mobile 
network provider may decide to block your phone line if suspicious messages are sent, or if the usage of applications 
of this type (SMS Automation) is against their Terms of Service.

## Troubleshooting 

If GSM never goes up after the app is correctly initialized (login has taken place), verify the following:

- If your phone has dual sims, disable the unused simcard, or set a default SIM for SMS in Android settings.
- Check your current data and SMS plan with your carrier.
- In some countries, you mnay need to call your carrier to request SMS activation for your phone line.

If GSM goes down after sending multiple messages, verify the following:

- Try to send a SMS manually. If it goes through, restart the app. If it does not, verify your line's credit, or data plan.
- The phone line may have been blocked temporarily by the carrier, as it was marked as spam, or violated their ToS. This usually lasts 24 hours.
- The rate limit was too low (default is 6).

If Socket goes down too often:

- Check the stability of your network connection. Jitter may cause unexpected reconnetions, and may produce timeouts.

## Build Dependencies

- Java SDK 8 or newer (Prefer Adoptium (https://adoptium.net/))
- Android Build Tools 31.0.0
- Android SDK API 30
- socket.io-client 2.0.1 (https://github.com/socketio/socket.io-client-java)

## Development

Intellij IDEA Ultimate Edition was used for development, and the corresponding .idea environment is included. To import the project in any other IDE,
erase the .idea folder locally.

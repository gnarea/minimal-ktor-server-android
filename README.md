# Example Netty-based Ktor server for Android 5+

This repo implements an Android 5+ app that runs a Netty-powered KTor server when the app is in the foreground.

The server runs on `0.0.0.0:13276` without TLS.

[`MainActivity`](app/src/main/java/com/example/ktorwsissue/MainActivity.kt) contains the KTor server code.

## Endpoints

This app implements two endpoints:

### `GET /`

It returns a `200 OK` with a text body (`text/plain`) that contains the model name of the device the server runs on.

### WebSocket `/ws`

This is a WebSocket endpoint that implements the following protocol:

1. Client connects to server.
1. Server sends text message to client.
1. Server waits for client to send message back (any message).
1. Server closes connection as soon as message from client is received.

## Automated test

[`ServerTest`](app/src/test/java/com/example/ktorwsissue/ServerTest.kt) contains a test that checks the endpoints above.

**Make sure to set the right IP to your Android device on your WiFi network.**

## Licence

This repo is in the public domain.

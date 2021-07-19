package com.example.ktorwsissue

import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets as ClientWebSockets
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.websocket.webSocket
import io.ktor.features.CallLogging
import io.ktor.http.ContentType
import io.ktor.http.cio.websocket.FrameType
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readBytes
import io.ktor.http.cio.websocket.send
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets as ServerWebSockets
import io.ktor.websocket.webSocket
import java.net.NetworkInterface
import java.nio.charset.Charset
import java.util.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class MainActivity : AppCompatActivity() {

    private val logger = Logger.getLogger("KtorServer")

    private val server by lazy {
        embeddedServer(Netty, 13276, watchPaths = emptyList()) {
            install(ServerWebSockets)
            install(CallLogging)

            routing {
                get("/") {
                    call.respondText("All good here in ${Build.MODEL}", ContentType.Text.Plain)
                }

                webSocket("/ws") {
                    logger.info("Sending message to client...")
                    send("foo")

                    val receivedMessage = incoming.receive()
                    val messageFormatted = if (receivedMessage.frameType == FrameType.TEXT) {
                        receivedMessage.readBytes().toString(Charset.defaultCharset())
                    } else {
                        "<non-text frame>"
                    }
                    logger.info("Got message from client: $messageFormatted")

                    logger.info("Closing connection...")
                    close()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CoroutineScope(Dispatchers.IO).launch {
            logger.info("Starting server...")
            server.start(wait = true)
        }

        CoroutineScope(Dispatchers.Default).launch {
            logger.info("Now get client to connect...")
            delay(3_000)
            val ktorEngine: HttpClientEngine = OkHttp.create {
                preconfigured = OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .build()
            }
            val ktorClient = HttpClient(ktorEngine) {
                install(ClientWebSockets)
            }
            ktorClient.webSocket("ws://127.0.0.1:13276/ws") {
                logger.info("Connection established")
                val incomingMessage = incoming.receive()
                logger.info("Got message $incomingMessage")
                send("bye")
            }

        }

        findViewById<TextView>(R.id.serverStatusText).text = getString(R.string.serverStartedMessage)

        val localIpAddress = getIpAddressInLocalNetwork()
        if (localIpAddress != null) {
            findViewById<TextView>(R.id.ipAddressText).text =
                getString(R.string.localIpAddressMessage, localIpAddress)
        }
    }

    override fun onDestroy() {
        logger.info("Stopping server")
        server.stop(1_000, 2_000)

        super.onDestroy()
    }

    private fun getIpAddressInLocalNetwork(): String? {
        val networkInterfaces = NetworkInterface.getNetworkInterfaces().iterator().asSequence()
        val localAddresses = networkInterfaces.flatMap {
            it.inetAddresses.asSequence()
                .filter { inetAddress ->
                    inetAddress.isSiteLocalAddress && !inetAddress.hostAddress.contains(":") &&
                        inetAddress.hostAddress != "127.0.0.1"
                }
                .map { inetAddress -> inetAddress.hostAddress }
        }
        return localAddresses.firstOrNull()
    }
}
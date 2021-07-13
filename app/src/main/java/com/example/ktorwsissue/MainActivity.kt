package com.example.ktorwsissue

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import io.ktor.application.call
import io.ktor.application.install
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
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import java.nio.charset.Charset
import java.util.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val coroutineContext = Dispatchers.IO

    private val logger = Logger.getLogger("KtorServer")

    private val server by lazy {
        embeddedServer(Netty, 13276, watchPaths = emptyList()) {
            install(WebSockets)
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

        CoroutineScope(coroutineContext).launch {
            logger.info("Starting server...")
            server.start(wait = true)
        }

        findViewById<TextView>(R.id.serverStatusText).text = getString(R.string.serverStartedMessage)
    }

    override fun onDestroy() {
        logger.info("Stopping server")
        server.stop(1_000, 2_000)

        super.onDestroy()
    }
}
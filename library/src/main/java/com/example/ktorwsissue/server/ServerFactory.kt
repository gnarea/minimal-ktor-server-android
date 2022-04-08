package com.example.ktorwsissue.server

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import java.nio.charset.Charset
import java.util.logging.Logger

object ServerFactory {
    private val logger = Logger.getLogger("KtorServer")
    fun getServer(port: Int): ApplicationEngine {
        return embeddedServer(Netty, port, watchPaths = emptyList()) {
            install(WebSockets)
            install(CallLogging)
            routing {
                get("/") {
                    call.respondText("All good here in MODEL", ContentType.Text.Plain)
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
}
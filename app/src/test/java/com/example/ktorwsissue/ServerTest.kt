package com.example.ktorwsissue

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.http.cio.websocket.send
import java.util.logging.Logger
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Test

class ServerTest {
    private val ktorEngine: HttpClientEngine = OkHttp.create {
        preconfigured = OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .build()
    }

    private val serverIPAddress = "192.168.1.106"

    private val logger: Logger = Logger.getLogger("Client")

    @Test
    fun testConnection() = runBlocking {
        val ktorClient = HttpClient(ktorEngine) {
            install(WebSockets)
        }

        val httpResponse = ktorClient.get<String>("http://${serverIPAddress}:13276/")
        logger.info("Got HTTP response: $httpResponse")

        ktorClient.webSocket("ws://${serverIPAddress}:13276/ws") {
            logger.info("Connection established")
            val incomingMessage = incoming.receive()
            logger.info("Got message $incomingMessage")
            send("bye")
        }
    }
}
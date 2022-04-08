package com.example.ktorwsissue.server

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.path
import io.ktor.request.receive
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.Netty
import io.ktor.websocket.*
import org.slf4j.event.Level
import java.nio.charset.Charset
import java.util.logging.Logger

data class Author(val name: String, val website: String)
data class ToDo(var id: Int, val name: String, val description: String, val completed: Boolean)

object ServerFactory {
    private val logger = Logger.getLogger("KtorServer")
    //Ref : https://github.com/Baeldung/kotlin-tutorials/blob/master/kotlin-libraries/src/main/kotlin/com/baeldung/ktor/APIServer.kt
    val toDoList = ArrayList<ToDo>()
    val jsonResponse = """{
            "id": 1,
            "task": "Pay waterbill",
            "description": "Pay water bill today",
        }"""
    fun getServer(port: Int): ApplicationEngine {
        return embeddedServer(Netty, port, watchPaths = emptyList()) {
            install(DefaultHeaders) {
                header("X-Developer", "Baeldung")
            }
            install(WebSockets)
            install(CallLogging) {
                level = Level.DEBUG
                filter { call -> call.request.path().startsWith("/todo") }
                filter { call -> call.request.path().startsWith("/author") }
            }
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                }
            }
            routing {
                get("/") {
                    call.respondText("All good here in MODEL", ContentType.Text.Plain)
                }
                route("/todo") {
                    post {
                        var toDo = call.receive<ToDo>()
                        toDo.id = toDoList.size
                        toDoList.add(toDo)
                        call.respond("Added")
                    }
                    delete("/{id}") {
                        call.respond(toDoList.removeAt(call.parameters["id"]!!.toInt()))
                    }
                    get("/{id}") {
                        call.respond(toDoList[call.parameters["id"]!!.toInt()])
                    }
                    get("/list") {
                        call.respond(toDoList)
                    }
                }
                get("/author"){
                    call.respond(Author("Baeldung","baeldung.com"))

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
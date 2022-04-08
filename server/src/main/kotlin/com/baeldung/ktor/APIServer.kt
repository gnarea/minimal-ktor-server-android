@file:JvmName("APIServer")
import com.example.ktorwsissue.server.ServerFactory
import java.util.logging.Logger

fun main(args: Array<String>) {
    val logger = Logger.getLogger("main")
    val server = ServerFactory.getServer(8080)
    logger.info("Starting server...")
    server.start(wait = true)
}
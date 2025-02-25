package ktor

import io.ktor.server.application.*
import io.ktor.server.routing.*
import ktor.routes.authRoutes

fun Application.configureRouting() {
    routing {
        authRoutes()
    }
}

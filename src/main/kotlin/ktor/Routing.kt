package ktor

import io.ktor.server.application.*
import io.ktor.server.routing.*
import ktor.routes.authRoutes
import ktor.routes.cardRoutes

fun Application.configureRouting() {
    routing {
        authRoutes()
        cardRoutes()
    }
}

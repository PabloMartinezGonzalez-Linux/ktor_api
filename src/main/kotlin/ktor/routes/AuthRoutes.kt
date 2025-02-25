package ktor.routes

import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import data.models.LoginRequest
import domain.models.Users

fun Route.authRoutes() {
    post("/auth/login") {

        // Almacenas la request (loginRequest) lo convierte a .json
        val loginRequest = call.receive<LoginRequest>()

        // Consulta en la base de datos
        // Users.select { ... }:
        //      Realiza un SELECT en la tabla Users buscando por el username proporcionado en el LoginRequest.
        // singleOrNull():
        //      Devuelve el primer registro encontrado o null si no hay coincidencias.

        val user = transaction {
            Users.select { Users.username eq loginRequest.username }.singleOrNull()
        }

        // Devuelve 404 not found del usuario
        if (user == null) {
            call.respond(HttpStatusCode.NotFound, "Usuario no encontrado")
            return@post
        }

        // Devulelve un 401 de contraseña incorrecta
        val dbPassword = user[Users.password]
        if (loginRequest.password != dbPassword) {
            call.respond(HttpStatusCode.Unauthorized, "Contraseña incorrecta")
            return@post
        }

        call.respondText("Login exitoso")
    }
}

package ktor.routes

import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import data.models.LoginRequest
import data.models.RegisterRequest
import domain.models.Users
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

fun Route.authRoutes() {
    post("/auth/login") {
        val loginRequest = call.receive<LoginRequest>()
        println("Datos recibidos: $loginRequest")

        val user = transaction {
            Users.select { Users.username eq loginRequest.username }.singleOrNull()
        }

        if (user == null) {
            call.respond(HttpStatusCode.NotFound, "Usuario no encontrado")
            return@post
        }

        val dbPassword = user[Users.password]
        if (loginRequest.password != dbPassword) {
            call.respond(HttpStatusCode.Unauthorized, "Contraseña incorrecta")
            return@post
        }

        val userId = user[Users.id]

        val token = JWT.create()
            .withIssuer("ktor.io")
            .withAudience("ktor_audience")
            .withClaim("username", loginRequest.username)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + 600000))
            .sign(Algorithm.HMAC256("secret"))

        println("Token generado: $token")

        call.respond(mapOf("token" to token))
    }

    post("/auth/register") {
        println("Endpoint /auth/register invocado")
        val registerRequest = call.receive<RegisterRequest>()

        val existingUser = transaction {
            Users.select { Users.username eq registerRequest.username }.singleOrNull()
        }

        if (existingUser != null) {
            call.respond(HttpStatusCode.Conflict, "El usuario ya existe")
            return@post
        }

        transaction {
            Users.insert {
                it[username] = registerRequest.username
                it[email] = registerRequest.email
                it[password] = registerRequest.password
            }
        }

        call.respond(HttpStatusCode.Created, mapOf("message" to "Usuario registrado exitosamente"))
    }
}

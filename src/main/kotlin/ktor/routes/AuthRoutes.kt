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
        // Recibe la solicitud de login y la deserializa en un objeto LoginRequest
        val loginRequest = call.receive<LoginRequest>()

        // Busca el usuario en la base de datos
        val user = transaction {
            Users.select { Users.username eq loginRequest.username }.singleOrNull()
        }

        if (user == null) {
            call.respond(HttpStatusCode.NotFound, "Usuario no encontrado")
            return@post
        }

        // Compara la contraseña proporcionada con la almacenada
        val dbPassword = user[Users.password]
        if (loginRequest.password != dbPassword) {
            call.respond(HttpStatusCode.Unauthorized, "Contraseña incorrecta")
            return@post
        }

        // Obtiene el userId del usuario (suponiendo que Users tiene una columna "id")
        val userId = user[Users.id]

        // Genera el token JWT incluyendo el claim "userId"
        val token = JWT.create()
            .withClaim("username", loginRequest.username)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + 600000)) // Token válido por 10 minutos
            .sign(Algorithm.HMAC256("secret"))

        // Devuelve el token en formato JSON
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

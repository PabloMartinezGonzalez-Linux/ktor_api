package ktor

import data.repository.DatabaseFactory
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "ktor sample app"
            verifier(
                JWT
                    .require(Algorithm.HMAC256("secret"))
                    .withAudience("ktor_audience")
                    .withIssuer("ktor.io")
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asInt()
                if (userId != null) {
                    JWTPrincipal(credential.payload)
                } else null
            }

        }
    }

    DatabaseFactory.initDB()
    configureRouting()
}

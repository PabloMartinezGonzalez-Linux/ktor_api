package ktor.routes

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import data.models.CardRequest
import data.models.CardResponse
import domain.models.Cards
import io.ktor.server.routing.*
import data.utils.ImageUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

fun Route.cardRoutes() {

    // Helper para obtener el userId del token JWT autenticado.
    fun getAuthenticatedUserId(call: ApplicationCall): Int? {
        // Asegúrate de que el token incluya el claim "userId" (de tipo Int)
        val principal = call.principal<JWTPrincipal>()
        return principal?.payload?.getClaim("userId")?.asInt()
    }

    route("/cards") {

        // Crear una nueva card (Create)
        post {
            val tokenUserId = getAuthenticatedUserId(call)
            if (tokenUserId == null) {
                call.respond(HttpStatusCode.Unauthorized, "Token inválido")
                return@post
            }

            // Recibe la solicitud y la convierte a JSON (CardRequest)
            val cardRequest = call.receive<CardRequest>()

            // Procesa la imagen: decodifica y guarda la imagen en el directorio del usuario,
            // obteniendo el nombre del archivo resultante.
            val savedFileName = ImageUtils.saveBase64Image(
                call.application,
                cardRequest.photo,
                tokenUserId.toString()
            )
            if (savedFileName == null) {
                call.respond(HttpStatusCode.BadRequest, "Error al procesar la imagen")
                return@post
            }

            // Inserta la nueva card usando el nombre del archivo en lugar del string Base64 completo
            val id = transaction {
                Cards.insert {
                    it[userId] = tokenUserId
                    it[photo] = savedFileName  // Se almacena el nombre del archivo
                    it[name] = cardRequest.name
                    it[description] = cardRequest.description
                    it[averageRating] = cardRequest.averageRating
                    it[hasImprovements] = cardRequest.hasImprovements
                } get Cards.id
            }
            call.respond(HttpStatusCode.Created, "Card creada con id $id")
        }


        // Obtener todas las cards del usuario autenticado (Read - list)
        get {
            val tokenUserId = getAuthenticatedUserId(call)
            if (tokenUserId == null) {
                call.respond(HttpStatusCode.Unauthorized, "Token inválido")
                return@get
            }
            val cards = transaction {
                Cards.select { Cards.userId eq tokenUserId }.map { row ->
                    CardResponse(
                        id = row[Cards.id],
                        userId = row[Cards.userId],
                        photo = row[Cards.photo],
                        name = row[Cards.name],
                        description = row[Cards.description],
                        averageRating = row[Cards.averageRating],
                        hasImprovements = row[Cards.hasImprovements]
                    )
                }
            }
            call.respond(cards)
        }

        // Obtener una card por id, pero solo si pertenece al usuario autenticado (Read - one)
        get("{id}") {
            val tokenUserId = getAuthenticatedUserId(call)
            if (tokenUserId == null) {
                call.respond(HttpStatusCode.Unauthorized, "Token inválido")
                return@get
            }
            val idParam = call.parameters["id"]?.toIntOrNull()
            if (idParam == null) {
                call.respond(HttpStatusCode.BadRequest, "Id inválido")
                return@get
            }
            val card = transaction {
                Cards.select { (Cards.id eq idParam) and (Cards.userId eq tokenUserId) }
                    .singleOrNull()
            }
            if (card == null) {
                call.respond(HttpStatusCode.NotFound, "Card no encontrada o no perteneciente a este usuario")
            } else {
                val cardResponse = CardResponse(
                    id = card[Cards.id],
                    userId = card[Cards.userId],
                    photo = card[Cards.photo],
                    name = card[Cards.name],
                    description = card[Cards.description],
                    averageRating = card[Cards.averageRating],
                    hasImprovements = card[Cards.hasImprovements]
                )
                call.respond(cardResponse)
            }
        }

        // Actualizar una card existente (Update)
        put("{id}") {
            val tokenUserId = getAuthenticatedUserId(call)
            if (tokenUserId == null) {
                call.respond(HttpStatusCode.Unauthorized, "Token inválido")
                return@put
            }
            val idParam = call.parameters["id"]?.toIntOrNull()
            if (idParam == null) {
                call.respond(HttpStatusCode.BadRequest, "Id inválido")
                return@put
            }
            // Verifica que la card a actualizar pertenece al usuario autenticado
            val ownerId = transaction {
                Cards.select { Cards.id eq idParam }
                    .map { it[Cards.userId] }
                    .singleOrNull()
            }
            if (ownerId == null) {
                call.respond(HttpStatusCode.NotFound, "Card no encontrada")
                return@put
            }
            if (ownerId != tokenUserId) {
                call.respond(HttpStatusCode.Forbidden, "No tienes permiso para modificar esta card")
                return@put
            }

            // Recibe los nuevos datos; ignora cualquier userId enviado en el JSON y usa tokenUserId
            val updateRequest = call.receive<CardRequest>()
            val updatedRows = transaction {
                Cards.update({ (Cards.id eq idParam) and (Cards.userId eq tokenUserId) }) {
                    it[userId] = tokenUserId
                    it[photo] = updateRequest.photo
                    it[name] = updateRequest.name
                    it[description] = updateRequest.description
                    it[averageRating] = updateRequest.averageRating
                    it[hasImprovements] = updateRequest.hasImprovements
                }
            }
            if (updatedRows == 0) {
                call.respond(HttpStatusCode.NotFound, "Card no encontrada o no perteneciente a este usuario")
            } else {
                call.respondText("Card actualizada correctamente")
            }
        }

        // Eliminar una card (Delete)
        delete("{id}") {
            val tokenUserId = getAuthenticatedUserId(call)
            if (tokenUserId == null) {
                call.respond(HttpStatusCode.Unauthorized, "Token inválido")
                return@delete
            }
            val idParam = call.parameters["id"]?.toIntOrNull()
            if (idParam == null) {
                call.respond(HttpStatusCode.BadRequest, "Id inválido")
                return@delete
            }
            val deletedRows = transaction {
                Cards.deleteWhere { (Cards.id eq idParam) and (Cards.userId eq tokenUserId) }
            }
            if (deletedRows == 0) {
                call.respond(HttpStatusCode.NotFound, "Card no encontrada o no perteneciente a este usuario")
            } else {
                call.respondText("Card eliminada correctamente")
            }
        }
    }
}

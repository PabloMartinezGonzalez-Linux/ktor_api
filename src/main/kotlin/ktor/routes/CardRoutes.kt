package ktor.routes

import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import data.models.CardRequest
import data.models.CardResponse
import domain.models.Cards
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

fun Route.cardRoutes() {
    route("/cards") {
        post {
            val cardRequest = call.receive<CardRequest>()

            val id = transaction {
                Cards.insert {
                    it[photo] = cardRequest.photo
                    it[name] = cardRequest.name
                    it[description] = cardRequest.description
                    it[averageRating] = cardRequest.averageRating
                    it[hasImprovements] = cardRequest.hasImprovements
                } get Cards.id
            }

            call.respond(HttpStatusCode.Created, "Card creada con id $id")
        }

        get {
            try {
                val cards = transaction {
                    Cards.selectAll().map { row ->
                        CardResponse(
                            id = row[Cards.id],
                            photo = row[Cards.photo],
                            name = row[Cards.name],
                            description = row[Cards.description],
                            averageRating = row[Cards.averageRating],
                            hasImprovements = row[Cards.hasImprovements]
                        )
                    }
                }
                call.respond(cards)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Error interno: ${e.localizedMessage}")
            }
        }

        get("{id}") {
            val idParam = call.parameters["id"]?.toIntOrNull()
            if (idParam == null) {
                call.respond(HttpStatusCode.BadRequest, "Id inválido")
                return@get
            }
            val card = transaction {
                Cards.select { Cards.id eq idParam }.singleOrNull()
            }
            if (card == null) {
                call.respond(HttpStatusCode.NotFound, "Card no encontrada")
            } else {
                val cardResponse = CardResponse(
                    id = card[Cards.id],
                    photo = card[Cards.photo],
                    name = card[Cards.name],
                    description = card[Cards.description],
                    averageRating = card[Cards.averageRating],
                    hasImprovements = card[Cards.hasImprovements]
                )
                call.respond(cardResponse)
            }
        }

        put("{id}") {
            val idParam = call.parameters["id"]?.toIntOrNull()
            if (idParam == null) {
                call.respond(HttpStatusCode.BadRequest, "Id inválido")
                return@put
            }
            val updateRequest = call.receive<CardRequest>()
            val updatedRows = transaction {
                Cards.update({ Cards.id eq idParam }) {
                    it[photo] = updateRequest.photo
                    it[name] = updateRequest.name
                    it[description] = updateRequest.description
                    it[averageRating] = updateRequest.averageRating
                    it[hasImprovements] = updateRequest.hasImprovements
                }
            }
            if (updatedRows == 0) {
                call.respond(HttpStatusCode.NotFound, "Card no encontrada")
            } else {
                call.respondText("Card actualizada correctamente")
            }
        }

        delete("{id}") {
            val idParam = call.parameters["id"]?.toIntOrNull()
            if (idParam == null) {
                call.respond(HttpStatusCode.BadRequest, "Id inválido")
                return@delete
            }
            val deletedRows = transaction {
                Cards.deleteWhere { Cards.id eq idParam }
            }
            if (deletedRows == 0) {
                call.respond(HttpStatusCode.NotFound, "Card no encontrada")
            } else {
                call.respondText("Card eliminada correctamente")
            }
        }
    }
}

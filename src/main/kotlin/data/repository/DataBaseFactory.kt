package data.repository

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import domain.models.Users
import domain.models.Cards
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

object DatabaseFactory {

    fun initDB() {
        Database.connect(
            url = "jdbc:mariadb://localhost:3306/ktor_api_db",
            driver = "org.mariadb.jdbc.Driver",
            user = "pablo",
            password = "admin"
        )

        transaction {
            SchemaUtils.create(Users, Cards)

            if (Users.selectAll().empty()) {
                // Inserta usuarios de prueba
                val adminId = Users.insert {
                    it[username] = "admin"
                    it[email] = "admin@admin.com"
                    it[password] = "admin"
                } get Users.id

                val pabloId = Users.insert {
                    it[username] = "pablo"
                    it[email] = "pablo@pablo.com"
                    it[password] = "admin"
                } get Users.id

                println("Usuarios iniciales creados en la base de datos")
            }

            if (Cards.selectAll().empty()) {
                Cards.insert {
                    it[photo] = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA"
                    it[name] = "Card Admin 1"
                    it[description] = "Descripción de la card de admin"
                    it[averageRating] = 4.5
                    it[hasImprovements] = true
                }

                Cards.insert {
                    it[photo] = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUB"
                    it[name] = "Card Pablo 1"
                    it[description] = "Descripción de la card de pablo"
                    it[averageRating] = 3.8
                    it[hasImprovements] = false
                }

                Cards.insert {
                    it[photo] = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUC"
                    it[name] = "Card Admin 2"
                    it[description] = "Otra card para admin"
                    it[averageRating] = 4.0
                    it[hasImprovements] = true
                }

                println("Cards iniciales creadas en la base de datos")
            }
        }
    }
}

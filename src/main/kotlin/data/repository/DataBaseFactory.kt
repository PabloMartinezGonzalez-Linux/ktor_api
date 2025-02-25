package data.repository

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import domain.models.Users
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
            // Crear las tablas si no existen
            SchemaUtils.create(Users)

            // Inicializar datos por defecto (si la tabla está vacía)
            if (Users.selectAll().empty()) {
                Users.insert {
                    it[username] = "admin"
                    it[email] = "admin@admin.com"
                    it[password] = "admin"
                }
                Users.insert {
                    it[username] = "pablo"
                    it[email] = "pablo@pablo.com"
                    it[password] = "admin"
                }
                println("Usuarios iniciales creados en la base de datos")
            }
        }
    }
}



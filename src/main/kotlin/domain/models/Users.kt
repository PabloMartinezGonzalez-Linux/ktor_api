package domain.models

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 50)
    val email = varchar("email", 100)
    val password = varchar("password", 64)

    override val primaryKey = PrimaryKey(id, name = "PK_User_ID")
}

package domain.models

import org.jetbrains.exposed.sql.Table

object Cards : Table("cards") {
    val id = integer("id").autoIncrement()
    val photo = text("photo")
    val name = varchar("name", 100)
    val description = text("description")
    val averageRating = double("score")
    val hasImprovements = bool("has_improvements")

    override val primaryKey = PrimaryKey(id, name = "PK_Card_ID")
}

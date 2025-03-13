package data.utils

import io.ktor.server.application.*
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO
import java.util.Base64

object ImageUtils {
    fun saveBase64Image(app: Application, base64Image: String?, userId: String): String? {
        if (base64Image.isNullOrBlank()) {
            return null
        }

        val allowedExtensions = listOf("jpg", "jpeg", "gif", "png")
        val regex = Regex("data:(image/[^;]+);base64,(.+)")
        val matchResult = regex.find(base64Image) ?: return null

        val type = matchResult.groupValues[1]
        var ext = type.substringAfter("/")
        val body = matchResult.groupValues[2]

        if (ext !in allowedExtensions) {
            return null
        }

        if (ext == "jpg") ext = "jpeg"

        return try {
            val imageBytes = try {
                Base64.getDecoder().decode(body)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                return null
            }

            val inputStream = ByteArrayInputStream(imageBytes)
            val bufferedImage: BufferedImage? = ImageIO.read(inputStream)

            if (bufferedImage == null) {
                return null
            }

            val basePath = app.environment.config.property("ktor.path.images").getString()
            val userDir = File(basePath, userId)
            if (!userDir.exists()) {
                userDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val fileName = "${userId}_$timestamp.$ext"
            val file = File(userDir, fileName)

            ImageIO.write(bufferedImage, ext, file)
            fileName
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

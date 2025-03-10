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
    fun saveBase64Image(app: Application, base64Image: String, userId: String): String? {
        val allowedExtensions = listOf("jpg", "jpeg", "gif", "png")
        // Expresión regular que extrae el MIME type y el cuerpo codificado en Base64
        val regex = Regex("data:(image/[^;]+);base64,(.+)")
        val matchResult = regex.find(base64Image) ?: return null

        val type = matchResult.groupValues[1]       // Ejemplo: image/png
        var ext = type.substringAfter("/")          // Obtiene "png" (o "jpeg", etc.)
        val body = matchResult.groupValues[2]

        if (ext !in allowedExtensions)
            return null

        // Ajusta la extensión en caso de que sea "jpg"
        if (ext == "jpg") ext = "jpeg"

        return try {
            val imageBytes = Base64.getDecoder().decode(body)
            val inputStream = ByteArrayInputStream(imageBytes)
            val bufferedImage: BufferedImage = ImageIO.read(inputStream)

            // Obtén la ruta base desde la configuración (asegúrate de definir "ktor.path.images" en application.conf)
            val basePath = app.environment.config.property("ktor.path.images").getString()
            val userDir = File(basePath, userId)
            if (!userDir.exists()) {
                userDir.mkdirs()
            }

            // Genera un nombre único usando el timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val fileName = "${userId}_$timestamp.$ext"
            val file = File(userDir, fileName)

            // Guarda la imagen en disco
            ImageIO.write(bufferedImage, ext, file)
            // Retorna el nombre del archivo (o podrías retornar la ruta completa, según lo necesites)
            fileName
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

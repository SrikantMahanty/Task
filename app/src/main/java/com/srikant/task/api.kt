import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

fun main() {
    val apiUrl = "https://www.jsonkeeper.com/b/6HBE" // Updated API endpoint

    try {
        val url: URL = URI.create(apiUrl).toURL()
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection

        // Request method: GET
        connection.requestMethod = "GET"

        // Response code
        val responseCode: Int = connection.responseCode
        println("Response Code: $responseCode")

        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Read and print the response data
            val reader: BufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
            var line: String?
            val response = StringBuilder()

            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }

            reader.close()

            // Print the raw JSON response
            println("Raw Response Data: $response")

            // Extract the content field from the choices array
            val responseData = response.toString()
            val contentStartIndex = responseData.indexOf("\"content\":") + 11
            val contentEndIndex = responseData.indexOf("\"refusal\":") - 2
            val contentJson = responseData.substring(contentStartIndex, contentEndIndex)

            // Remove escape characters from the content field
            val cleanedContent = contentJson.replace("\\n", "").replace("\\", "")

            // Extract titles and description using simple string manipulation
            val titlesStartIndex = cleanedContent.indexOf("\"titles\":") + 9
            val titlesEndIndex = cleanedContent.indexOf("],", titlesStartIndex) + 1
            val titlesRaw = cleanedContent.substring(titlesStartIndex, titlesEndIndex)

            val descriptionStartIndex = cleanedContent.indexOf("\"description\":") + 14
            val descriptionEndIndex = cleanedContent.length - 1
            val description = cleanedContent.substring(descriptionStartIndex, descriptionEndIndex)

            // Print the titles and description
            println("Titles:")
            titlesRaw
                .replace("[", "")
                .replace("]", "")
                .split(",")
                .forEach { title -> println("- ${title.trim()}") }

            println("Description: $description")

        } else {
            println("Error: Unable to fetch data from the API")
        }

        // Close the connection
        connection.disconnect()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

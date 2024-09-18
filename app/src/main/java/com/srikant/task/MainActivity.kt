package com.srikant.task

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var titlesTextView: TextView
    private lateinit var descriptionTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        titlesTextView = findViewById(R.id.TitleTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)

        val shareButtonTitle: Button = findViewById(R.id.sharedesButton)
        val copyButtonTitle: Button = findViewById(R.id.copyesButton)
        val shareDescButton: Button = findViewById(R.id.shareButton)
        val copyDescButton: Button = findViewById(R.id.copyButton)

        // Allow network on the main thread for simplicity (avoid in production)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        // Fetch the data and update UI
        fetchAndDisplayData()

        // Share functionality for Titles
        shareButtonTitle.setOnClickListener {
            val textToShare = titlesTextView.text.toString()
            if (textToShare.isNotEmpty()) {
                shareText(textToShare)
            } else {
                Toast.makeText(this, "No data to share!", Toast.LENGTH_SHORT).show()
            }
        }

        // Copy functionality for Titles
        copyButtonTitle.setOnClickListener {
            val textToCopy = titlesTextView.text.toString()
            if (textToCopy.isNotEmpty()) {
                copyToClipboard(textToCopy)
            } else {
                Toast.makeText(this, "No data to copy!", Toast.LENGTH_SHORT).show()
            }
        }

        // Share functionality for Description
        shareDescButton.setOnClickListener {
            val textToShare = descriptionTextView.text.toString()
            if (textToShare.isNotEmpty()) {
                shareText(textToShare)
            } else {
                Toast.makeText(this, "No data to share!", Toast.LENGTH_SHORT).show()
            }
        }

        // Copy functionality for Description
        copyDescButton.setOnClickListener {
            val textToCopy = descriptionTextView.text.toString()
            if (textToCopy.isNotEmpty()) {
                copyToClipboard(textToCopy)
            } else {
                Toast.makeText(this, "No data to copy!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchAndDisplayData() {
        val apiUrl = "https://www.jsonkeeper.com/b/6HBE"
        try {
            val url: URL = URI.create(apiUrl).toURL()
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val responseCode: Int = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader: BufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                reader.close()

                // Extract the content field from the JSON response (manual parsing)
                val responseData = response.toString()
                val contentStartIndex = responseData.indexOf("\"content\":") + 11
                val contentEndIndex = responseData.indexOf("\"refusal\":") - 2
                val contentJson = responseData.substring(contentStartIndex, contentEndIndex)
                val cleanedContent = contentJson.replace("\\n", "").replace("\\", "")

                val titlesStartIndex = cleanedContent.indexOf("\"titles\":") + 9
                val titlesEndIndex = cleanedContent.indexOf("],", titlesStartIndex) + 1
                val titlesRaw = cleanedContent.substring(titlesStartIndex, titlesEndIndex)

                val descriptionStartIndex = cleanedContent.indexOf("\"description\":") + 14
                val descriptionEndIndex = cleanedContent.length - 1
                val description = cleanedContent.substring(descriptionStartIndex, descriptionEndIndex)

                // Update the UI (TextViews) with the fetched data
                val titlesFormatted = titlesRaw.replace("[", "").replace("]", "").split(",")
                    .joinToString(separator = "\n") { it.trim().replace("\"", "") }

                titlesTextView.text = titlesFormatted
                descriptionTextView.text = description.trim().replace("\"", "")

            } else {
                titlesTextView.text = "Error fetching data."
            }

            connection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
            titlesTextView.text = "Exception: ${e.message}"
        }
    }

    private fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}

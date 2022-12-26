package de.stefan_oltmann.mediathekdownloader.server.model

import kotlinx.serialization.Serializable

private val invalidFilenameCharactersRegex = Regex("[\\\\/:*?\"<>|.]")

@Serializable
data class Movie(
    val transmitter: String,
    val topic: String,
    val title: String,
    val date: String?,
    val durationInMinutes: Int?,
    val url: String,
) {

    private fun getFileNameDatePrefix() = if (date != null)
        date.substring(6, 10) + "-" + date.substring(3, 5) + "-" + date.substring(0, 2) + " - "
    else
        ""

    fun getFolderName() =
        topic.replace(invalidFilenameCharactersRegex,"_")

    fun getFileName() =
        (getFileNameDatePrefix() + topic.replace(invalidFilenameCharactersRegex, "_")
            .take(20) + " - " +
            title.replace(invalidFilenameCharactersRegex, "_").take(60) + ".mp4")
            .replace("  ", " ")
}

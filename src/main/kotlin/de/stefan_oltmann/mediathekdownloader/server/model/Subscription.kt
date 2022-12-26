package de.stefan_oltmann.mediathekdownloader.server.model

import kotlinx.serialization.Serializable

@Serializable
data class Subscription(
    val transmitter: String,
    val topic: String,
    val title: String,
    val minDurationInMinutes: Int = 0
) {

    fun matches(movie: Movie): Boolean =
        transmitter == movie.transmitter &&
                topic == movie.topic &&
                (movie.durationInMinutes == null || movie.durationInMinutes >= minDurationInMinutes) &&
                movie.title.contains(title)
}

package de.stefan_oltmann.mediathekdownloader.server

import de.stefan_oltmann.mediathekdownloader.server.model.Movie
import de.stefan_oltmann.mediathekdownloader.server.model.Subscription
import org.tukaani.xz.XZInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL

object MovieListUtil {

    fun filterMovies(movies: List<Movie>, subscriptions: List<Subscription>): List<Movie> {

        val filteredMovies = mutableListOf<Movie>()

        for (movie in movies)
            for (subscription in subscriptions)
                if (subscription.matches(movie))
                    filteredMovies.add(movie)

        return filteredMovies
    }

    fun downloadCurrentMoviesList(fullList: Boolean = true): List<Movie> {

        val url = if (fullList) MOVIE_LIST_URL_FULL else MOVIE_LIST_URL_DIFF

        val connection = URL(url).openConnection() as HttpURLConnection

        connection.requestMethod = "GET"
        connection.connect()

        if (connection.responseCode != 200) {
            println("Connection to $url failed.")
            return emptyList()
        }

        val movies = BufferedReader(InputStreamReader(XZInputStream(connection.inputStream))).use { reader ->
            readMovies(reader)
        }

        connection.disconnect()

        return movies
    }

    private fun readMovies(reader: Reader): List<Movie> {

        val blockPrefix = listOf('"', 'X', '"', ':', '[')
        val blockSuffix = listOf('"', ']')
        val entryDelimiter = "\",\""

        var inBlock = false
        var blockPrefixIndex = 0
        var blockSuffixIndex = 0

        var inEntry = false
        var nextCharEscaped = false

        var transmitter = ""
        var topic = ""

        val sb = StringBuilder()

        val movies = mutableListOf<Movie>()

        var char: Char
        while (reader.read().also { char = it.toChar() } != -1) {

            /*
             * Sample what we are looking for:
             *
             * "X":["","","Bonus: Die Highlights der Baywatch-Challenge [\"Das schaffst du nie!\"]","05.09.2017","16:00:00","00:02:47","34","Ihr wollt es – Ihr kriegt es.","https://funk-02.akamaized.net/22679/files/21/03/01/2904212/32-LZJvHY3zMwyx4qCdXpmW.mp4","https://www.funk.net/channel/das-schaffst-du-nie-1423/bonus-die-highlights-der-baywatchchallenge-das-schaffst-du-nie-758249","","","59|5-Vfwz2NH9dGy8bFDLgkJT.mp4","","59|2-VQmNrTgwWxJ2kKDMqyb7.mp4","","1504620000","","","false"]
             *
             * Note that we can have [ inside of " blocks
             */

            /* Look for the start of the block. */
            if (!inBlock) {

                if (char == blockPrefix[blockPrefixIndex])
                    blockPrefixIndex++
                else
                    blockPrefixIndex = 0

                if (blockPrefixIndex == blockPrefix.size) {
                    inBlock = true
                    blockPrefixIndex = 0
                }

                continue
            }

            /* Look for the end of the block. */

            if (char == '\\')
                nextCharEscaped = true

            if (!nextCharEscaped && char == '"')
                inEntry = !inEntry

            if (!inEntry && char == blockSuffix[blockSuffixIndex])
                blockSuffixIndex++
            else
                blockSuffixIndex = 0

            if (blockSuffixIndex == blockSuffix.size) {

                inBlock = false
                blockSuffixIndex = 0

                val lineParts = sb.toString()
                    .substring(1, sb.length - 1)
                    .split(entryDelimiter)

                val title = lineParts[2].replace("\\\"", "\"")
                val duration = lineParts[5] // like "00:28:20" or ""
                val url = lineParts[8]

                /*
                 * The list seems to avoid duplications and only
                 * mentions a new transmitter or topic if it changes.
                 */
                val thisTransmitter = lineParts[0]
                val thisTopic = lineParts[1]

                if (thisTransmitter.isNotBlank())
                    transmitter = thisTransmitter

                if (thisTopic.isNotBlank())
                    topic = thisTopic

                /* We need at least title and url, otherwise the entry is not useful. */
                if (title.isNotBlank() && url.isNotBlank()) {

                    val urlHd = lineParts[14]

                    val finalUrl = if (urlHd.contains('|')) {

                        val urlHdParts = urlHd.split('|')

                        val endIndex = urlHdParts[0].toInt()

                        url.substring(0, endIndex) + urlHdParts[1]

                    } else {

                        url + urlHd
                    }

                    val durationInMinutes = if (duration.isNotEmpty()) {

                        val durationParts = duration.split(':')

                        durationParts[0].toInt() * 60 + durationParts[1].toInt()

                    } else
                        null

                    check(transmitter.isNotBlank()) { "Transmitter was blank: $sb" }
                    check(topic.isNotBlank()) { "Topic was blank: $sb" }

                    movies.add(
                        Movie(
                            transmitter = transmitter,
                            topic = topic,
                            title = title,
                            date = lineParts[3].ifBlank { null },
                            durationInMinutes = durationInMinutes,
                            url = finalUrl
                        )
                    )
                }

                sb.clear()

                continue
            }

            sb.append(char)

            /* Reset escaped flag. */
            if (char != '\\' && nextCharEscaped)
                nextCharEscaped = false
        }

        return movies
    }
}

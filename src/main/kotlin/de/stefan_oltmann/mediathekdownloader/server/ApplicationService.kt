/*
 * Stefans Mediathek Downloader
 * Copyright (C) 2022 Stefan Oltmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.stefan_oltmann.mediathekdownloader.server

import de.stefan_oltmann.mediathekdownloader.server.data.DownloadHistoryRepository
import de.stefan_oltmann.mediathekdownloader.server.data.impl.FileDownloadHistoryRepository
import de.stefan_oltmann.mediathekdownloader.server.data.impl.FileSubscriptionRepository
import de.stefan_oltmann.mediathekdownloader.server.data.impl.SUBSCRIPTIONS_FILE_NAME
import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Singleton

@Singleton
class ApplicationService {

    val subscriptionRepository = FileSubscriptionRepository

    val downloadHistoryRepository = FileDownloadHistoryRepository

    @Scheduled(cron="0 4 * * * ?")
    // @Scheduled(every = "8h")
    @Suppress("Unused") // it's called by the Scheduler
    suspend fun downloadMedia() {

        val subscriptions = subscriptionRepository.findAll()

        if (subscriptions.isEmpty()) {
            Log.info("There are not subscriptions. Please create a $SUBSCRIPTIONS_FILE_NAME.")
            return
        }

        val downloadedMovies = downloadHistoryRepository.findAll()

        var startTime = System.currentTimeMillis()

        val movies = withContext(Dispatchers.IO) {
            MovieListUtil.downloadCurrentMoviesList()
        }

        var duration = System.currentTimeMillis() - startTime

        Log.info("Downloaded current movie list in $duration ms.")

        val moviesToDownload = MovieListUtil.filterMovies(movies, subscriptions)

        Log.info("Found ${moviesToDownload.size} movies matching the subscriptions.")

        for (movie in moviesToDownload) {

            /* Each topic goes into its own subfolder */
            val targetFolder = File(DOWNLOAD_DIR_NAME, movie.getFolderName())

            targetFolder.mkdirs()

            val targetFile = File(targetFolder, movie.getFileName())

            /* Skip existing files. */
            if (targetFile.exists())
                continue

            /* Skip already downloaded movies - in case that the file was moved or renamed */
            if (downloadedMovies.contains(movie))
                continue

            startTime = System.currentTimeMillis()

            withContext(Dispatchers.IO) {

                val connection = URL(movie.url).openConnection() as HttpURLConnection

                connection.requestMethod = "GET"
                connection.connect()

                if (connection.responseCode == 200) {

                    FileOutputStream(targetFile).use { outputStream ->
                        connection.inputStream.copyTo(outputStream)
                    }

                } else {

                    Log.error("File ${movie.url} seems not to exist.")
                }

                connection.disconnect()
            }

            duration = System.currentTimeMillis() - startTime

            downloadHistoryRepository.add(movie)

            val mbSize = targetFile.length() / 1024 / 1024

            Log.info("Downloaded ${targetFile.absolutePath} of size $mbSize MB in $duration ms.")
        }
    }
}

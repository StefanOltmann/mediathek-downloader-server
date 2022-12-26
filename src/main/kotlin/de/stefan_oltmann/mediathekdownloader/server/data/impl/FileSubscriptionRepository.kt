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
package de.stefan_oltmann.mediathekdownloader.server.data.impl

import de.stefan_oltmann.mediathekdownloader.server.model.Subscription
import de.stefan_oltmann.mediathekdownloader.server.DATA_DIR_NAME
import de.stefan_oltmann.mediathekdownloader.server.data.SubscriptionRepository
import de.stefan_oltmann.mediathekdownloader.server.model.Movie
import io.quarkus.logging.Log
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.io.FileOutputStream

const val SUBSCRIPTIONS_FILE_NAME = "subscriptions.json"

object FileSubscriptionRepository : SubscriptionRepository {

    private val jsonFormat = Json { prettyPrint = true }

    private val file = File(DATA_DIR_NAME, SUBSCRIPTIONS_FILE_NAME)

    private val subscriptions = mutableListOf<Subscription>()

    init {

        if (file.exists()) {

            val text = file.readText()

            subscriptions.addAll(Json.decodeFromString<List<Subscription>>(text))
        }

        Log.info("Started with ${subscriptions.size} subscriptions. Using file: ${file.absolutePath}")
    }

    override fun findAll(): List<Subscription> = subscriptions

    @OptIn(ExperimentalSerializationApi::class)
    override fun add(subscription: Subscription) {

        subscriptions.add(subscription)

        FileOutputStream(file).use { outputStream ->
            jsonFormat.encodeToStream(subscriptions, outputStream)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun delete(subscription: Subscription) {

        subscriptions.remove(subscription)

        FileOutputStream(file).use { outputStream ->
            jsonFormat.encodeToStream(subscriptions, outputStream)
        }
    }

}

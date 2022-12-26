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

import de.stefan_oltmann.mediathekdownloader.server.model.Movie
import de.stefan_oltmann.mediathekdownloader.server.model.Subscription
import javax.inject.Inject
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/")
class ServerResource {

    @Inject
    lateinit var service: ApplicationService

    @GET
    @Path("/subscriptions")
    @Produces(MediaType.APPLICATION_JSON)
    fun findAllSubscriptions(): List<Subscription> {

        try {

            logger.info("[API] | Requesting subscription list...")

            return service.subscriptionRepository.findAll()

        } catch (ex: Exception) {
            logger.error("Requesting subscription list failed.", ex)
            throw ex
        }
    }

    @POST
    @Path("/subscriptions")
    @Consumes(MediaType.APPLICATION_JSON)
    fun addSubscription(subscription: Subscription) {

        try {

            logger.info("[API] | Adding subscription $subscription")

            service.subscriptionRepository.add(subscription)

        } catch (ex: Exception) {
            logger.error("Adding subscription failed.", ex)
            throw ex
        }
    }

    @DELETE
    @Path("/subscriptions")
    @Consumes(MediaType.APPLICATION_JSON)
    fun deleteSubscription(subscription: Subscription) {

        try {

            logger.info("[API] | Deleting subscription $subscription")

            service.subscriptionRepository.delete(subscription)

        } catch (ex: Exception) {
            logger.error("Deleting subscription failed.", ex)
            throw ex
        }
    }

    @GET
    @Path("/movies")
    @Produces(MediaType.APPLICATION_JSON)
    fun findAllDownloadedMovies(): List<Movie> {

        try {

            logger.info("[API] | Requesting downloaded movie list...")

            return service.downloadHistoryRepository.findAll()

        } catch (ex: Exception) {
            logger.error("Requesting downloaded movie list failed.", ex)
            throw ex
        }
    }
}

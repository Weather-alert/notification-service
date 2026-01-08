package org.acme

import jakarta.enterprise.inject.Default
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.PATCH
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/api/v1/notification")
class NotificationResource {

    @Inject
    @field: Default
    private lateinit var notificationService: NotificationService


    @POST
    @Path("/notify/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    fun notifyUser(
        @PathParam("userId") userId: String,
        weather: WeatherNotifyRequest
    ): Response {
        //println("received message $userId")
        //return Response.ok().build()
        val r = notificationService.notify(userId, weather)
        return if(r == null){
            Response.status(Response.Status.NOT_FOUND)
                .entity("User with such id does not have a token")
                .build()
        } else{
            Response.ok(r).build()
        }
    }
    @POST
    @Path("/users/{id}")
    fun createToken(
        @PathParam("id") userId: String,
        @QueryParam("token") token: String,
    ): Response {
        println("token accepted: $token")
        if(token.isEmpty()){
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Token not appended")
                .build()
        }
        val r = notificationService.createToken(userId, token)
        return if(r == null){
            Response.status(Response.Status.NOT_ACCEPTABLE)
                .entity("Token already exists")
                .build()
        }else{
            Response.ok().build()
        }
    }

    @GET
    @Path("/users/{id}")
    fun readToken(
        @PathParam("id") userId: String,
    ): Response{
        val r = notificationService.readToken(userId)
        return if(r == null){
            Response.status(Response.Status.NOT_FOUND)
                .entity("Token doesn't exists")
                .build()
        } else {
            Response.ok(r).build()
        }
    }
    @GET
    @Path("/users")
    fun readUsers(): Response{
        return Response.ok(notificationService.readUsers()).build()
    }

    @PATCH
    @Path("/users/{id}")
    fun updateToken(
        @PathParam("id") userId: String,
        @QueryParam("token") token: String,
    ): Response {
        if(token.isEmpty()){
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Token not appended")
                .build()
        }
        val r = notificationService.updateToken(userId, token)
        return if(r == null){
            Response.status(Response.Status.NOT_FOUND)
                .entity("Token doesn't exists")
                .build()
        }else{
            Response.ok().build()
        }
    }

    @DELETE
    @Path("/users/{id}")
    fun deleteToken(
        @PathParam("id") userId: String
    ): Response{
        val r = notificationService.removeToken(userId)
        return if(r == null){
            Response.status(Response.Status.NOT_FOUND)
                .entity("Token doesn't exists")
                .build()
        } else {
            Response.ok().build()
        }
    }

}
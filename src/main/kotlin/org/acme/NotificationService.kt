package org.acme

import com.google.auth.oauth2.GoogleCredentials
import io.vertx.mutiny.core.eventbus.Message
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.RequestScoped
import jakarta.enterprise.inject.Default
import jakarta.inject.Inject
import org.acme.token.Token
import org.acme.token.TokenRepository
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.io.FileInputStream
import java.io.FileNotFoundException
import javax.management.Notification


@RequestScoped
class NotificationService {

    @Inject
    @field:Default
    private lateinit var tokenRepository: TokenRepository

    //@Inject
    //private lateinit var firebaseMessaging: FirebaseMessaging

    @Inject
    private lateinit var nativePushService: NativePushService

    fun notify(userId: String, weather: WeatherNotifyRequest): String? {
        if(tokenRepository.get(userId) == null) return null
        val token = tokenRepository.get(userId)!!.token

        val temp = ((weather.weatherForecast.main.temp - 273.15).toInt()*100).toDouble()/100
        val cityName = weather.city.name
        val title = "$temp°C in $cityName"
        val body = "${weather.weatherForecast.weather.first().description}"

        /*
        val notification : Notification = Notification.builder()
            .setBody(body)
            .setTitle(title)
            .build()
        val message = Message.builder()
            .setToken(token)
            .setNotification(notification)
            .build()
        */

            //val response = firebaseMessaging.getInstance().send(message)
            //return response
        return try {
            nativePushService.sendPush(token, title,body).toString()
        } catch(e: Exception){
            println("couldn't' send message to Firebase $token")
            null
        }
    }
    fun createToken(userId: String, token: String): Boolean?{
        if(tokenRepository.get(userId) != null) return null

        val token = Token(userId,token)
        tokenRepository.create(token)

        return true
    }

    fun readToken(userId: String): String?{
        return tokenRepository.get(userId)?.token
    }
    fun readUsers(): List<Token>{
        return tokenRepository.listAll()
    }
    fun updateToken(userId: String, token: String): Boolean?{
        if(tokenRepository.get(userId) == null) return null

        val token = Token(userId,token)
        tokenRepository.create(token)

        return true
    }
    fun removeToken(userId: String): Boolean?{
        if (tokenRepository.get(userId) == null) return null

        tokenRepository.delete(userId)

        return true
    }
}
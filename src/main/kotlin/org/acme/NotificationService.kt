package org.acme

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import jakarta.enterprise.context.ApplicationScoped
import java.io.FileInputStream


@ApplicationScoped
class NotificationService {
    // K: userID, V: token
    private val tokenMap = mutableMapOf<String,String>()

    init {
        try {
            val serviceAccount = FileInputStream("src/main/resources/serviceAccountKey.json")

            val options = FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()

            FirebaseApp.initializeApp(options)
        }catch(e: Exception){
            println("Failed to init Firebase key ${e.cause}")
        }
    }
    fun notify(userId: String, weather: WeatherNotifyRequest): String? {
        if(tokenMap.get(userId) == null) return null
        val token = tokenMap[userId]

        val temp = ((weather.weatherForecast.main.temp - 273.15).toInt()*100).toDouble()/100
        val cityName = weather.city.name
        val title = "$temp°C in $cityName"
        val body = "${weather.weatherForecast.weather.first().description}"

        val notification : Notification = Notification.builder()
            .setBody(body)
            .setTitle(title)
            .build()

        val message = Message.builder()
            .setToken(token)
            .setNotification(notification)
            .build()
        return try {
            val response = FirebaseMessaging.getInstance().send(message)
            return response
        } catch(e: Exception){
            println("couldn't' send message to Firebase $token")
            ""
        }
    }
    fun createToken(userId: String, token: String): Boolean?{
        if(tokenMap.get(userId) != null) return null

        tokenMap[userId] = token

        return true
    }

    fun readToken(userId: String): String?{
        return tokenMap[userId]
    }
    fun readUsers(): List<String>{
        return tokenMap.keys.toList()
    }
    fun updateToken(userId: String, token: String): Boolean?{
        if(tokenMap.get(userId) == null) return null

        tokenMap[userId] = token

        return true
    }
    fun removeToken(userId: String): Boolean?{
        if (tokenMap.get(userId) == null) return null

        tokenMap.remove(userId)

        return true
    }
}
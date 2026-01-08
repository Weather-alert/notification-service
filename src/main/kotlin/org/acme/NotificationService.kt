package org.acme

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Default
import jakarta.inject.Inject
import org.acme.token.Token
import org.acme.token.TokenRepository
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.io.FileInputStream


@ApplicationScoped
class NotificationService {

    @Inject
    @field:Default
    private lateinit var tokenRepository: TokenRepository

    var initialized = false

    @ConfigProperty(name = "service.account.path")
    lateinit var serviceAccountPath: String
    fun initFirebase(){
        try {
            val serviceAccount = FileInputStream(serviceAccountPath)

            val options = FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()

            FirebaseApp.initializeApp(options)
            initialized = true
        }catch(e: Exception){
            println("Failed to init Firebase key ${e.cause}")
            println("Failed to read serviceAccountKey.json check ENV SERVICE_ACCOUNT_PATH")
            initialized = false
        }
    }
    fun notify(userId: String, weather: WeatherNotifyRequest): String? {
        if(tokenRepository.get(userId) == null) return null
        val token = tokenRepository.get(userId)!!.token

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
        if(initialized== false)
            initFirebase()
        return try {
            val response = FirebaseMessaging.getInstance().send(message)
            return response
        } catch(e: Exception){
            println("couldn't' send message to Firebase $token")
            ""
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
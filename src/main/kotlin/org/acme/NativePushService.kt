package org.acme

import com.google.auth.oauth2.GoogleCredentials
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.function.Consumer


@ApplicationScoped
class NativePushService {
    // Move Project ID to a config/env variable for better practice
    private val fcmUrl = "https://fcm.googleapis.com/v1/projects/weather-alert-7667b/messages:send"
    private var httpClient: HttpClient? = null
    private var credentials: GoogleCredentials? = null

    @ConfigProperty(name = "service.account.path")
    private lateinit var serviceAccountPath: String

    @PostConstruct
    fun init() {
        // We initialize the client once to reuse connections (saves memory/CPU)
        this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build()

        try {
            val scopes = listOf("https://www.googleapis.com/auth/cloud-platform")
            val serviceAccount = FileInputStream(serviceAccountPath)

            // Load from resources (GraalVM friendly)
            this.credentials = GoogleCredentials
                .fromStream(serviceAccount)
                .createScoped(scopes)


        } catch (e: FileNotFoundException){
            println("FileNotFoundException: Failed to find file ${e.cause}")
            println("At path: $serviceAccountPath")
            println("Failed to read serviceAccountKey.json check ENV SERVICE_ACCOUNT_PATH")
        } catch(e: Exception){
            println("Failed to init Firebase key ${e.cause}")
        }
    }
    private fun myGetAcessToken(): String {
        try {
            val scopes = listOf("https://www.googleapis.com/auth/cloud-platform")

            val googleCredentials = GoogleCredentials
                .fromStream(FileInputStream(serviceAccountPath))
                .createScoped(scopes)

            googleCredentials.refresh()
            return googleCredentials.accessToken.tokenValue
        } catch (e: FileNotFoundException){
            println("FileNotFoundException: Failed to find file ${e.cause}")
            println("At path: $serviceAccountPath")
            println("Failed to read serviceAccountKey.json check ENV SERVICE_ACCOUNT_PATH")
        } catch(e: Exception){
            println("Failed to init Firebase key ${e.cause}")
        }
        return ""
    }

    private var accessToken: String? = null
        get() {
            credentials?.refreshIfExpired()
            return credentials?.getAccessToken()?.getTokenValue()
        }

    fun sendPush(targetToken: String?, title: String?, body: String?):Boolean{
        try {
            val jsonPayload: String = """
            {
              "message": {
                "token": "$targetToken",
                "notification": {
                 "title": "$title",
                 "body": "$body" 
                }
              }
            }
            
            """.trimIndent()
            //println("TOKENN: ${myGetAcessToken()}")
            println("TOKEN: $accessToken")

            if(accessToken == null){
                accessToken = myGetAcessToken()
                println("token is null getting new one")
            }
            val request = HttpRequest.newBuilder()
                .uri(URI.create(fcmUrl))
                .header("Authorization", "Bearer $accessToken")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build()

            val response = httpClient?.send(request, HttpResponse.BodyHandlers.ofString())
            return if (response!!.statusCode() >= 400) {
                println("FCM Error Code: ${response.statusCode()} - ${response.body()}")
                false
            } else {
                println("FCM Success: ${response.statusCode()}")
                true
            }
        } catch (e: Exception) {
            println("EXCEPTION: ${e.toString()}")
        }
        return false
    }
}
/* package org.acme.deprecated

import com.google.auth.oauth2.GoogleCredentials
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.io.FileInputStream
import java.io.FileNotFoundException

@ApplicationScoped
class FirebaseMessaging {

    private var initialized = false

    @ConfigProperty(name = "service.account.path")
    private lateinit var serviceAccountPath: String

    @PostConstruct
    private fun init() {
        initFirebase()
    }
    fun initFirebase(): Boolean{

        try {
            val serviceAccount = FileInputStream(serviceAccountPath)

            val options = FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()
            if(FirebaseApp.getApps().isEmpty()){
                FirebaseApp.initializeApp(options)
            } else{
                println("Warning: Tried to duplicate initialization")
                println("Firebase was already initialized")
            }
            initialized = true
            println("Firebase Successfully initialized")
        } catch (e: FileNotFoundException){
            println("FileNotFoundException: Failed to find file ${e.cause}")
            println("At path: $serviceAccountPath")
            println("Failed to read serviceAccountKey.json check ENV SERVICE_ACCOUNT_PATH")
            initialized = false
        } catch(e: Exception){
            println("Failed to init Firebase key ${e.cause}")
            if (FirebaseApp.getApps().isNotEmpty()) {
                println("Firebase is not empty that means it was already initialized")
            }
            initialized = false
        }
        return initialized
    }
    fun getInstance(): FirebaseMessaging {
        if(initialized == true || ( initialized == false && initFirebase() ) ){
                return FirebaseMessaging.getInstance()
        }
        throw RuntimeException("Failed to initialize app")

    }

}
*/
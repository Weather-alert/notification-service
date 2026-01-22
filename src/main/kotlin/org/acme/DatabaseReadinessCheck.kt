package org.acme

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.health.HealthCheck
import org.eclipse.microprofile.health.HealthCheckResponse
import org.eclipse.microprofile.health.Readiness
import javax.sql.DataSource


@Readiness
@ApplicationScoped
class DatabaseReadinessCheck : HealthCheck {
    @Inject
    var dataSource: DataSource? = null

    override fun call(): HealthCheckResponse? {
        try {
            dataSource?.getConnection().use { connection ->
                return HealthCheckResponse.up("database")
            }
        } catch (e: Exception) {
            return HealthCheckResponse.down("database")
        }
    }
}
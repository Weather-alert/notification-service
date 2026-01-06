package org.acme

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "token")
data class Token(
    @Id
    val userId: String = "",

    val token: String = "",
)
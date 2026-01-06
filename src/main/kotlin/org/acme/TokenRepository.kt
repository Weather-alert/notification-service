package org.acme

import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional

@ApplicationScoped
class TokenRepository {
    @PersistenceContext
    private lateinit var em: EntityManager

    @Transactional
    fun create(token: Token) {
        em.persist(token)
    }

    fun get(userId: String): Token? =
        em.find(Token::class.java, userId)

    @Transactional
    fun update(token: Token): Token = em.merge(token)

    @Transactional
    fun delete(userId: String): Boolean {
        val token = em.find(Token::class.java, userId) ?: return false
        em.remove(token)
        return true
    }

    fun listAll(): List<String> =
        em.createQuery("SELECT t FROM Token t", Token::class.java).resultList
            .map { token-> token.token }

}


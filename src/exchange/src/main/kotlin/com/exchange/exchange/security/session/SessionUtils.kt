package com.exchange.exchange.security.session

import com.exchange.exchange.domain.user.UserEntity
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.web.server.WebSession

/**
 * exchange-all
 *
 * @author uuhnaut69
 *
 */
object SessionUtils {

    private val objectMapper = jacksonObjectMapper()
        .registerModules(JavaTimeModule())
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    /**
     * Serialize user to session
     * @param webSession the [WebSession] to serialize user
     * @param userEntity the [UserEntity] to serialize
     */
    fun serializeUser(webSession: WebSession, userEntity: UserEntity) {
        webSession.attributes["user"] = objectMapper.writeValueAsString(userEntity)
    }

    /**
     * Deserialize user from session
     * @param webSession the [WebSession] to deserialize user
     * @return the [UserEntity] if found, otherwise null
     */
    fun deserializeUser(webSession: WebSession): UserEntity? {
        return webSession.attributes["user"]?.let {
            objectMapper.readValue(it.toString(), UserEntity::class.java)
        }
    }
}

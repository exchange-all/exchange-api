package com.exchange.exchange.security.session

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.BeanClassLoaderAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.security.jackson2.SecurityJackson2Modules


/**
 * exchange-all
 *
 * @author uuhnaut69
 *
 */
@Configuration
class SessionConfig : BeanClassLoaderAware {
    private lateinit var loader: ClassLoader

    @Bean
    fun springSessionDefaultRedisSerializer(): RedisSerializer<Any> {
        return GenericJackson2JsonRedisSerializer(objectMapper())
    }

    /**
     * Customized [ObjectMapper] to add mix-in for class that doesn't have default
     * constructors
     * @return the [ObjectMapper] to use
     */
    private fun objectMapper(): ObjectMapper {
        val mapper = jacksonObjectMapper()
            .registerModules(JavaTimeModule())
        mapper.registerModules(SecurityJackson2Modules.getModules(loader))
        return mapper
    }

    override fun setBeanClassLoader(classLoader: ClassLoader) {
        this.loader = classLoader
    }
}

package com.exchange.exchange.domain.user

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

/**

 *
 * @author uuhnaut69
 *
 */
@Repository
interface UserRepository : ReactiveMongoRepository<UserEntity, String> {

    fun existsByEmailIgnoreCase(email: String): Mono<Boolean>

    fun findByEmailIgnoreCase(email: String): Mono<UserEntity>
}

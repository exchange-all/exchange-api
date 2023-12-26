package com.exchange.exchange.domain.user

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.Instant
import java.util.*

/**

 *
 * @author uuhnaut69
 *
 */
class UserEntity : UserDetails {

    lateinit var id: String

    lateinit var email: String

    private lateinit var password: String

    lateinit var roles: List<Role>

    private var accountNonExpired: Boolean = true

    private var accountNonLocked: Boolean = true

    private var credentialsNonExpired: Boolean = true

    private var enabled: Boolean = true

    var createdAt: Instant? = null

    var updatedAt: Instant? = null

    companion object {
        fun newUser(
            email: String,
            password: String,
            roles: List<Role>,
        ): UserEntity {
            return UserEntity().apply {
                this.id = UUID.randomUUID().toString()
                this.email = email
                this.password = password
                this.roles = roles
            }
        }
    }

    @JsonIgnore
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return this.roles.map { GrantedAuthority { it.name } }.toMutableList()
    }

    override fun getPassword(): String {
        return this.password
    }

    override fun getUsername(): String {
        return this.email
    }

    override fun isAccountNonExpired(): Boolean {
        return this.accountNonExpired
    }

    override fun isAccountNonLocked(): Boolean {
        return this.accountNonLocked
    }

    override fun isCredentialsNonExpired(): Boolean {
        return this.credentialsNonExpired
    }

    override fun isEnabled(): Boolean {
        return this.enabled
    }
}

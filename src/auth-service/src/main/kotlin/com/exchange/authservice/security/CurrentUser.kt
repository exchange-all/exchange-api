package com.exchange.authservice.security

import org.springframework.security.core.annotation.AuthenticationPrincipal

/**
 *
 * @author uuhnaut69
 *
 */
@AuthenticationPrincipal
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class CurrentUser()

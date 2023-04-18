package org.zalando.logbook.common

import kotlin.RequiresOptIn.Level.WARNING
import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.CONSTRUCTOR
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.LOCAL_VARIABLE
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.PROPERTY_SETTER
import kotlin.annotation.AnnotationTarget.TYPEALIAS
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER


/**
 * This annotation marks the API is considered experimental and the behavior of such API may be changed or the API may be removed completely in any further release.
 *
 * Any usage of a declaration annotated with `@ExperimentalLogbookKtorApi` must be accepted either by
 * annotating that usage with the [OptIn] annotation, e.g. `@OptIn(ExperimentalLogbookKtorApi::class)`,
 * or by using the compiler argument `-Opt-in=org.zalando.logbook.ktor.ExperimentalLogbookKtorApi`.
 */
@RequiresOptIn(level = WARNING)
@Retention(BINARY)
@Target(
    CLASS,
    ANNOTATION_CLASS,
    PROPERTY,
    FIELD,
    LOCAL_VARIABLE,
    VALUE_PARAMETER,
    CONSTRUCTOR,
    FUNCTION,
    PROPERTY_GETTER,
    PROPERTY_SETTER,
    TYPEALIAS
)
annotation class ExperimentalLogbookKtorApi

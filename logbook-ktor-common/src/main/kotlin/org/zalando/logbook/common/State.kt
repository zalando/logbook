package org.zalando.logbook.common


sealed class State {
    var body: ByteArray = EMPTY_BODY
    open fun with(): State = this
    open fun without(): State = this
    open fun buffer(content: ByteArray): State = this


    object Buffering : State() {
        override fun without(): State = Ignoring(this)
        override fun buffer(content: ByteArray): State = apply { body = content }
    }

    object Unbuffered : State() {
        override fun with(): State = Offering
    }

    object Offering : State() {
        override fun without(): State = Unbuffered
        override fun buffer(content: ByteArray): State = Buffering.buffer(content)
    }

    class Ignoring(private val delegate: Buffering) : State() {
        override fun with(): State = delegate
        override fun buffer(content: ByteArray): State = apply { delegate.buffer(content) }
    }
}
package com.github.ryarnyah.requery.datastore

import java.util.stream.Stream

class MicronautClosableStream<E>(private val delegate: Stream<E>, private val decoratedCloseHandler: Runnable) :
    Stream<E> by delegate {

    override fun close() {
        delegate.close()
        decoratedCloseHandler.run()
    }
}
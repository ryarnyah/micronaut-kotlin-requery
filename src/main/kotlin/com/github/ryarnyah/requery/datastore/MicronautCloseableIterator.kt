package com.github.ryarnyah.requery.datastore

import io.micronaut.transaction.TransactionStatus
import io.micronaut.transaction.jdbc.DataSourceTransactionManager
import io.requery.util.CloseableIterator
import java.sql.Connection

class MicronautCloseableIterator<E>(
    private val delegate: CloseableIterator<E>,
    private val transactionManager: DataSourceTransactionManager,
    private val status: TransactionStatus<Connection>
): CloseableIterator<E> by delegate {

    override fun close() {
        transactionManager.commit(status)
        delegate.close()
    }
}
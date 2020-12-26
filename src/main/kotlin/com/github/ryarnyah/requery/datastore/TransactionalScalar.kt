package com.github.ryarnyah.requery.datastore

import io.micronaut.transaction.jdbc.DataSourceTransactionManager
import io.requery.query.Scalar
import io.requery.sql.Configuration
import io.requery.util.function.Consumer
import io.requery.util.function.Supplier
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

class TransactionalScalar<E>(
    private val delegate: Scalar<E>,
    private val transactionManager: DataSourceTransactionManager,
    private val configuration: Configuration
) : Scalar<E> {
    override fun call(): E {
        return transactionManager.executeRead {
            delegate.call()
        }
    }

    override fun value(): E {
        return transactionManager.executeRead {
            delegate.value()
        }
    }

    override fun consume(action: Consumer<in E>?) {
        return transactionManager.executeRead {
            delegate.consume(action)
        }
    }

    override fun toCompletableFuture(): CompletableFuture<E> {
        return toCompletableFuture(configuration.writeExecutor)
    }

    override fun toCompletableFuture(executor: Executor?): CompletableFuture<E> {
        val supplier = java.util.function.Supplier<E> { value() }
        return if (executor == null) CompletableFuture.supplyAsync(supplier) else CompletableFuture.supplyAsync(
            supplier,
            executor
        )
    }

    override fun toSupplier(): Supplier<E> {
        return Supplier { value() }
    }
}
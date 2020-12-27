package com.github.ryarnyah.requery.datastore

import io.micronaut.transaction.jdbc.DataSourceTransactionManager
import io.requery.BlockingEntityStore
import io.requery.Transaction
import io.requery.TransactionIsolation
import io.requery.meta.Attribute
import io.requery.meta.QueryAttribute
import io.requery.query.*
import io.requery.query.element.QueryElement
import io.requery.sql.Configuration
import java.util.concurrent.Callable

class MicronautEntityDataStore<T: Any>(
    private val delegate: BlockingEntityStore<T>,
    private val transactionManager: DataSourceTransactionManager,
    private val configuration: Configuration
): BlockingEntityStore<T> by delegate {

    override fun transaction(): Transaction = delegate.transaction()

    @Suppress("UNCHECKED_CAST")
    private fun <E> result(
        query: Return<out Result<E>>,
        transactionManager: DataSourceTransactionManager
    ): QueryElement<TransactionalResult<E>> {
        val element = query as QueryElement<Result<E>>
        return element.extend { result -> TransactionalResult(result, transactionManager) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <E> scalar(
        query: Return<out Scalar<E>>,
        transactionManager: DataSourceTransactionManager,
        configuration: Configuration
    ): QueryElement<TransactionalScalar<E>> {
        val element = query as QueryElement<Scalar<E>>
        return element.extend { result -> TransactionalScalar(result, transactionManager, configuration) }
    }

    override fun <E : T, K> findByKey(type: Class<E>?, key: K): E {
        return transactionManager.executeRead {
            delegate.findByKey(type, key)
        }
    }

    override fun <E : T> refresh(entity: E, vararg attributes: Attribute<*, *>): E {
        return transactionManager.executeRead {
            delegate.refresh(entity, *attributes)
        }
    }

    override fun <E : T> refresh(entities: Iterable<E>, vararg attributes: Attribute<*, *>): Iterable<E> {
        return transactionManager.executeRead {
            delegate.refresh(entities, *attributes)
        }
    }

    override fun <E : T> refreshAll(entity: E): E {
        return transactionManager.executeRead {
            delegate.refreshAll(entity)
        }
    }

    override fun <V : Any?> runInTransaction(callable: Callable<V>?): V {
        return transactionManager.executeWrite {
            delegate.runInTransaction(callable)
        }
    }

    override fun <V : Any?> runInTransaction(callable: Callable<V>?, isolation: TransactionIsolation?): V {
        return transactionManager.executeWrite {
            delegate.runInTransaction(callable, isolation)
        }
    }

    override fun toBlocking(): BlockingEntityStore<T> = this

    override fun count(vararg attributes: QueryAttribute<*, *>?): Selection<out Scalar<Int>> =
        scalar(delegate.count(*attributes), transactionManager, configuration)


    override fun <E : T> count(type: Class<E>): Selection<out Scalar<Int>> =
        scalar(delegate.count(type), transactionManager, configuration)


    override fun select(vararg expressions: Expression<*>): Selection<out Result<Tuple>> =
        transactionManager.executeRead {
            result(delegate.select(*expressions), transactionManager)
        }

    override fun <E : T> select(
        type: Class<E>,
        attributes: MutableSet<out QueryAttribute<E, *>>
    ): Selection<out Result<E>> = transactionManager.executeRead {
        result(delegate.select(type, attributes), transactionManager)
    }

    override fun select(expressions: MutableSet<out Expression<*>>): Selection<out Result<Tuple>> = transactionManager.executeRead {
        result(delegate.select(expressions), transactionManager)
    }

    override fun <E : T> select(type: Class<E>, vararg attributes: QueryAttribute<*, *>?): Selection<out Result<E>> =
        result(delegate.select(type, *attributes), transactionManager)

    override fun <E : T> refresh(entity: E): E {
        return transactionManager.executeRead {
            delegate.refresh(entity)
        }
    }
}
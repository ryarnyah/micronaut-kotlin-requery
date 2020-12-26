package com.github.ryarnyah.requery.datastore

import io.micronaut.transaction.jdbc.DataSourceTransactionManager
import io.requery.Transaction
import io.requery.TransactionIsolation
import io.requery.kotlin.BlockingEntityStore
import io.requery.kotlin.QueryDelegate
import io.requery.kotlin.QueryableAttribute
import io.requery.kotlin.Selection
import io.requery.meta.Attribute
import io.requery.query.*
import io.requery.sql.Configuration
import kotlin.reflect.KClass

class MicronautKotlinEntityDataStore<T : Any>(
    val delegate: BlockingEntityStore<T>,
    private val transactionManager: DataSourceTransactionManager,
    private val configuration: Configuration
) : BlockingEntityStore<T> by delegate {

    @Suppress("UNCHECKED_CAST")
    private fun <E> result(
        query: Return<out Result<E>>,
        transactionManager: DataSourceTransactionManager
    ): QueryDelegate<TransactionalResult<E>> {
        val element = query as QueryDelegate<Result<E>>
        return element.extend { result -> TransactionalResult(result, transactionManager) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <E> scalar(
        query: Return<out Scalar<E>>,
        transactionManager: DataSourceTransactionManager,
        configuration: Configuration
    ): QueryDelegate<TransactionalScalar<E>> {
        val element = query as QueryDelegate<Scalar<E>>
        return element.extend { result -> TransactionalScalar(result, transactionManager, configuration) }
    }

    override val transaction: Transaction
        get() = delegate.transaction

    override fun <E : T, K> findByKey(type: KClass<E>, key: K): E? {
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

    override fun <V> withTransaction(isolation: TransactionIsolation, body: BlockingEntityStore<T>.() -> V): V {
        return transactionManager.executeWrite {
            delegate.withTransaction(isolation, body)
        }
    }

    override fun <V> withTransaction(body: BlockingEntityStore<T>.() -> V): V {
        return transactionManager.executeWrite {
            delegate.withTransaction(body)
        }
    }

    override fun toBlocking(): BlockingEntityStore<T> = this

    override fun count(vararg attributes: QueryableAttribute<T, *>): Selection<out Scalar<Int>> =
        scalar(delegate.count(*attributes), transactionManager, configuration)

    override fun <E : T> count(type: KClass<E>): Selection<out Scalar<Int>> =
        scalar(delegate.count(type), transactionManager, configuration)

    override fun select(vararg expressions: Expression<*>): Selection<out Result<Tuple>> =
        transactionManager.executeRead {
            result(delegate.select(*expressions), transactionManager)
        }

    override fun <E : T> select(type: KClass<E>): Selection<out Result<E>> = transactionManager.executeRead {
        result(delegate.select(type), transactionManager)
    }

    override fun <E : T> select(
        type: KClass<E>,
        vararg attributes: QueryableAttribute<E, *>
    ): Selection<out Result<E>> = transactionManager.executeRead {
        result(delegate.select(type, *attributes), transactionManager)
    }

    override fun <E : T> refresh(entity: E): E {
        return transactionManager.executeRead {
            delegate.refresh(entity)
        }
    }
}
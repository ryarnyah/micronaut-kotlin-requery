package com.github.ryarnyah.requery.datastore

import io.micronaut.transaction.TransactionDefinition
import io.micronaut.transaction.TransactionStatus
import io.micronaut.transaction.jdbc.DataSourceTransactionManager
import io.requery.query.Expression
import io.requery.query.Result
import io.requery.util.CloseableIterator
import io.requery.util.function.Consumer
import io.requery.util.function.Supplier
import java.lang.reflect.UndeclaredThrowableException
import java.sql.Connection
import java.util.stream.Stream

class TransactionalResult<E>(
    private val delegate: Result<E>,
    private val transactionManager: DataSourceTransactionManager
) : Result<E> by delegate {

    override fun first(): E {
        return transactionManager.executeRead {
            delegate.first()
        }
    }

    override fun iterator(): CloseableIterator<E> {
        return readOnlyTransactionalOperation { status ->
            val iterator = delegate.iterator()
            MicronautCloseableIterator<E>(iterator, transactionManager, status)
        }
    }

    override fun iterator(skip: Int, take: Int): CloseableIterator<E> {
        return readOnlyTransactionalOperation { status ->
            val iterator = delegate.iterator(skip, take)
            MicronautCloseableIterator(iterator, transactionManager, status)
        }
    }

    override fun close() {
        delegate.close()
    }

    override fun stream(): Stream<E> {
        return readOnlyTransactionalOperation { status ->
            MicronautClosableStream(delegate.stream()) {
                transactionManager.commit(status)
            }
        }
    }

    override fun <C : MutableCollection<E>?> collect(collection: C): C {
        return transactionManager.executeRead {
            delegate.collect(collection)
        }
    }

    override fun firstOr(defaultElement: E): E {
        return transactionManager.executeRead {
            delegate.firstOr(defaultElement)
        }
    }

    override fun firstOr(supplier: Supplier<E>?): E {
        return transactionManager.executeRead {
            delegate.firstOr(supplier)
        }
    }

    override fun firstOrNull(): E {
        return transactionManager.executeRead {
            delegate.firstOrNull()
        }
    }

    override fun each(action: Consumer<in E>?) {
        return transactionManager.executeRead {
            delegate.each(action)
        }
    }

    override fun toList(): MutableList<E> {
        return transactionManager.executeRead {
            delegate.toList()
        }
    }

    override fun <K : Any?> toMap(key: Expression<K>?): MutableMap<K, E> {
        return transactionManager.executeRead {
            delegate.toMap(key)
        }
    }

    override fun <K : Any?> toMap(key: Expression<K>?, map: MutableMap<K, E>?): MutableMap<K, E> {
        return transactionManager.executeRead {
            delegate.toMap(key, map)
        }
    }

    private fun <Z> readOnlyTransactionalOperation(operation: (status: TransactionStatus<Connection>) -> Z): Z {
        val status = transactionManager.getTransaction(TransactionDefinition.READ_ONLY)
        try {
            return operation(status)
        } catch (ex: RuntimeException) {
            // Transactional code threw application exception -> rollback
            transactionManager.rollback(status)
            throw ex
        } catch (ex: Error) {
            transactionManager.rollback(status)
            throw ex
        } catch (ex: Throwable) {
            // Transactional code threw unexpected exception -> rollback
            transactionManager.rollback(status)
            throw UndeclaredThrowableException(ex, "TransactionCallback threw undeclared checked exception")
        }
    }
}
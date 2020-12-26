package com.github.ryarnyah.requery.configuration

import io.micronaut.context.annotation.EachProperty
import io.micronaut.context.annotation.Parameter
import io.requery.sql.EntityStateListener
import io.requery.sql.StatementListener
import io.requery.util.function.Function
import java.util.*
import java.util.concurrent.Executor

@EachProperty(RequeryDataSourceConfiguration.PREFIX)
class RequeryDataSourceConfiguration {
    companion object {
        const val PREFIX = "requery"
        const val DEFAULT = "default"
    }

    var useDefaultLogging: Boolean = false
    var statementCacheSize: Int = 0
    var batchUpdateSize: Int = 64
    var quoteTableNames: Boolean = false
    var quoteColumnNames: Boolean = false
    var tableTransformer: Function<String, String>? = null
    var columnTransformer: Function<String, String>? = null
    var statementListeners: Set<StatementListener> = LinkedHashSet()
    var entityStateListeners: Set<EntityStateListener<Any>> = LinkedHashSet()
    var writeExecutor: Executor? = null
}
package com.github.ryarnyah.requery.configuration

import com.github.ryarnyah.requery.datastore.MicronautEntityDataStore
import com.github.ryarnyah.requery.datastore.MicronautKotlinEntityDataStore
import com.github.ryarnyah.requery.linked.LinkedDatasource
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.*
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.transaction.jdbc.DataSourceTransactionManager
import io.requery.Persistable
import io.requery.cache.EmptyEntityCache
import io.requery.meta.EntityModel
import io.requery.sql.EntityDataStore
import io.requery.sql.KotlinConfiguration
import io.requery.sql.KotlinEntityDataStore
import io.requery.sql.TransactionMode
import javax.sql.DataSource

@Factory
@Requires(classes = [EntityModel::class], beans = [DataSource::class])
class RequeryConfigurationFactory {

    @Bean
    @EachBean(DataSource::class)
    fun micronautKotlinEntityDataStore(
        @Parameter name: String,
        applicationContext: ApplicationContext,
        dataSource: DataSource
    ): MicronautKotlinEntityDataStore<Persistable> {

        val requeryDataSourceConfiguration =
            applicationContext.findBean(RequeryDataSourceConfiguration::class.java, Qualifiers.byName(name))
                .orElse(RequeryDataSourceConfiguration())
        val qualifier = if (name == RequeryDataSourceConfiguration.DEFAULT) null else name

        val transactionManager = applicationContext.getBean(
            DataSourceTransactionManager::class.java, if (qualifier != null) Qualifiers.byName(qualifier) else null
        )
        val entityModel = applicationContext.getBean(
            EntityModel::class.java, if (qualifier != null) Qualifiers.byName(qualifier) else null
        )

        val configuration = KotlinConfiguration(
            entityModel,
            // Needed because requery use resultset.getStatement -> statement.getConnection to close connection and
            // Micronaut TransactionalConnection return original Connection in this case not the TransactionalConnection
            LinkedDatasource(dataSource),
            // Cache disabled <=> transaction managed by micronaut
            cache = EmptyEntityCache(),
            // Managed by micronaut transaction manager.
            transactionMode = TransactionMode.NONE,
            // Custom properties
            useDefaultLogging = requeryDataSourceConfiguration.useDefaultLogging,
            statementCacheSize = requeryDataSourceConfiguration.statementCacheSize,
            batchUpdateSize = requeryDataSourceConfiguration.batchUpdateSize,
            quoteColumnNames = requeryDataSourceConfiguration.quoteColumnNames,
            quoteTableNames = requeryDataSourceConfiguration.quoteTableNames,
            tableTransformer = requeryDataSourceConfiguration.tableTransformer,
            columnTransformer = requeryDataSourceConfiguration.columnTransformer,
            statementListeners = requeryDataSourceConfiguration.statementListeners,
            entityStateListeners = requeryDataSourceConfiguration.entityStateListeners,
            writeExecutor = requeryDataSourceConfiguration.writeExecutor
        )
        return MicronautKotlinEntityDataStore(
            KotlinEntityDataStore(configuration),
            transactionManager,
            configuration
        )
    }

    @Bean
    @EachBean(DataSource::class)
    fun micronautEntityDataStore(
        @Parameter name: String,
        applicationContext: ApplicationContext,
        dataSource: DataSource
    ): MicronautEntityDataStore<Persistable> {

        val requeryDataSourceConfiguration =
            applicationContext.findBean(RequeryDataSourceConfiguration::class.java, Qualifiers.byName(name))
                .orElse(RequeryDataSourceConfiguration())
        val qualifier = if (name == RequeryDataSourceConfiguration.DEFAULT) null else name

        val transactionManager = applicationContext.getBean(
            DataSourceTransactionManager::class.java, if (qualifier != null) Qualifiers.byName(qualifier) else null
        )
        val entityModel = applicationContext.getBean(
            EntityModel::class.java, if (qualifier != null) Qualifiers.byName(qualifier) else null
        )

        val configuration = KotlinConfiguration(
            entityModel,
            // Needed because requery use resultset.getStatement -> statement.getConnection to close connection and
            // Micronaut TransactionalConnection return original Connection in this case not the TransactionalConnection
            LinkedDatasource(dataSource),
            // Cache disabled <=> transaction managed by micronaut
            cache = EmptyEntityCache(),
            // Managed by micronaut transaction manager.
            transactionMode = TransactionMode.NONE,
            // Custom properties
            useDefaultLogging = requeryDataSourceConfiguration.useDefaultLogging,
            statementCacheSize = requeryDataSourceConfiguration.statementCacheSize,
            batchUpdateSize = requeryDataSourceConfiguration.batchUpdateSize,
            quoteColumnNames = requeryDataSourceConfiguration.quoteColumnNames,
            quoteTableNames = requeryDataSourceConfiguration.quoteTableNames,
            tableTransformer = requeryDataSourceConfiguration.tableTransformer,
            columnTransformer = requeryDataSourceConfiguration.columnTransformer,
            statementListeners = requeryDataSourceConfiguration.statementListeners,
            entityStateListeners = requeryDataSourceConfiguration.entityStateListeners,
            writeExecutor = requeryDataSourceConfiguration.writeExecutor
        )
        return MicronautEntityDataStore(
            EntityDataStore(configuration),
            transactionManager,
            configuration
        )
    }
}
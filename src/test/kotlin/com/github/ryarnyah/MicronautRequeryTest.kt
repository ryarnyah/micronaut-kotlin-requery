package com.github.ryarnyah

import com.github.ryarnyah.model.Models
import com.github.ryarnyah.model.TestData
import com.github.ryarnyah.requery.configuration.RequeryDataSourceConfiguration
import com.github.ryarnyah.requery.datastore.MicronautKotlinEntityDataStore
import io.micronaut.context.DefaultApplicationContext
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import io.micronaut.context.env.MapPropertySource
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.transaction.annotation.TransactionalAdvice
import io.micronaut.transaction.exceptions.NoTransactionException
import io.micronaut.transaction.jdbc.DataSourceTransactionManager
import io.requery.meta.EntityModel
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.stream.Collectors
import javax.inject.Named
import javax.inject.Singleton
import javax.sql.DataSource
import javax.transaction.Transactional

class MicronautRequeryTest {

    private val setupDatabase = """
        DROP TABLE IF EXISTS TESTDATA;
        CREATE TABLE TESTDATA(
            UID VARCHAR(256) PRIMARY KEY
        );
    """.trimIndent()

    @Test
    fun testNoConfiguration() {
        val applicationContext = DefaultApplicationContext("test")
        applicationContext.start()

        try {
            Assertions.assertFalse(applicationContext.containsBean(MicronautKotlinEntityDataStore::class.java))
        } finally {
            applicationContext.stop()
        }
    }

    @Test
    fun testWithDatabaseConfiguration() {
        val applicationContext = DefaultApplicationContext("test")
        applicationContext.environment.addPropertySource(
            MapPropertySource.of(
                "test",
                mapOf(
                    "datasources.default.url" to "jdbc:h2:mem:default;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                    "datasources.default.username" to "sa",
                    "datasources.default.password" to "",
                )
            )
        )
        applicationContext.start()

        try {
            Assertions.assertTrue(applicationContext.containsBean(MicronautKotlinEntityDataStore::class.java))
        } finally {
            applicationContext.stop()
        }
    }

    @Test
    fun testSaveData() {
        val applicationContext = DefaultApplicationContext("test")
        applicationContext.environment.addPropertySource(
            MapPropertySource.of(
                "test",
                mapOf(
                    "datasources.default.url" to "jdbc:h2:mem:default;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                    "datasources.default.username" to "sa",
                    "datasources.default.password" to "",
                )
            )
        )
        applicationContext.start()

        try {
            Assertions.assertTrue(applicationContext.containsBean(DataSource::class.java))
            Assertions.assertTrue(applicationContext.containsBean(DataSourceTransactionManager::class.java))

            val transactionManager = applicationContext.getBean(DataSourceTransactionManager::class.java)

            val dataSource = applicationContext.getBean(DataSource::class.java)
            transactionManager.executeWrite {
                dataSource.connection.use { conn ->
                    conn.createStatement().use { statement ->
                        statement.execute(setupDatabase)
                    }
                }
            }
            Assertions.assertTrue(applicationContext.containsBean(TestService::class.java))
            val testService = applicationContext.getBean(TestService::class.java)
            testService.save(TestData("Hello World"))
            Assertions.assertNotNull(testService.findById("Hello World"))

            val testServiceJava = applicationContext.getBean(TestServiceJava::class.java)
            Assertions.assertNotNull(testServiceJava.findById("Hello World"))
        } finally {
            applicationContext.stop()
        }
    }

    @Test
    fun testSaveDataTransactional() {
        val applicationContext = DefaultApplicationContext("test")
        applicationContext.environment.addPropertySource(
            MapPropertySource.of(
                "test",
                mapOf(
                    "datasources.default.url" to "jdbc:h2:mem:default;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                    "datasources.default.username" to "sa",
                    "datasources.default.password" to ""
                )
            )
        )
        applicationContext.start()

        try {
            Assertions.assertTrue(applicationContext.containsBean(DataSource::class.java))
            Assertions.assertTrue(applicationContext.containsBean(DataSourceTransactionManager::class.java))

            val transactionManager = applicationContext.getBean(DataSourceTransactionManager::class.java)

            val dataSource = applicationContext.getBean(DataSource::class.java)
            transactionManager.executeWrite {
                dataSource.connection.use { conn ->
                    conn.createStatement().use { statement ->
                        statement.execute(setupDatabase)
                    }
                }
            }
            Assertions.assertTrue(applicationContext.containsBean(TestService::class.java))
            val testService = applicationContext.getBean(TestService::class.java)

            Assertions.assertThrows(RuntimeException::class.java) {
                testService.saveWithException(TestData("test"))
            }
            Assertions.assertNull(testService.findById("test"))
        } finally {
            applicationContext.stop()
        }
    }

    @Test
    fun testSaveStreamData() {
        val applicationContext = DefaultApplicationContext("test")
        applicationContext.environment.addPropertySource(
            MapPropertySource.of(
                "test",
                mapOf(
                    "datasources.default.url" to "jdbc:h2:mem:default;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                    "datasources.default.username" to "sa",
                    "datasources.default.password" to ""
                )
            )
        )
        applicationContext.start()

        try {
            Assertions.assertTrue(applicationContext.containsBean(DataSource::class.java))
            Assertions.assertTrue(applicationContext.containsBean(DataSourceTransactionManager::class.java))

            val transactionManager = applicationContext.getBean(DataSourceTransactionManager::class.java)

            val dataSource = applicationContext.getBean(DataSource::class.java)
            transactionManager.executeWrite {
                dataSource.connection.use { conn ->
                    conn.createStatement().use { statement ->
                        statement.execute(setupDatabase)
                    }
                }
            }
            Assertions.assertTrue(applicationContext.containsBean(TestService::class.java))
            val testService = applicationContext.getBean(TestService::class.java)

            val entities = setOf(TestData("one"), TestData("two"))
            val resultEntities = testService.saveReadStream(entities)

            entities.forEach { expected ->
                Assertions.assertTrue(resultEntities.contains(expected))
            }
        } finally {
            applicationContext.stop()
        }
    }

    @Test
    fun testSaveIteratorDataNonTransactional() {
        val applicationContext = DefaultApplicationContext("test")
        applicationContext.environment.addPropertySource(
            MapPropertySource.of(
                "test",
                mapOf(
                    "datasources.default.url" to "jdbc:h2:mem:default;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                    "datasources.default.username" to "sa",
                    "datasources.default.password" to ""
                )
            )
        )
        applicationContext.start()

        try {
            Assertions.assertTrue(applicationContext.containsBean(DataSource::class.java))
            Assertions.assertTrue(applicationContext.containsBean(DataSourceTransactionManager::class.java))

            val transactionManager = applicationContext.getBean(DataSourceTransactionManager::class.java)

            val dataSource = applicationContext.getBean(DataSource::class.java)
            transactionManager.executeWrite {
                dataSource.connection.use { conn ->
                    conn.createStatement().use { statement ->
                        statement.execute(setupDatabase)
                    }
                }
            }
            Assertions.assertTrue(applicationContext.containsBean(TestService::class.java))
            val testService = applicationContext.getBean(TestService::class.java)

            val entities = setOf(TestData("one"), TestData("two"))
            entities.forEach { testData ->
                testService.save(testData)
            }
            val resultEntities = testService.readAllIterator()

            entities.forEach { expected ->
                Assertions.assertTrue(resultEntities.contains(expected))
            }
        } finally {
            applicationContext.stop()
        }
    }

    @Test
    fun testSaveStreamDataNonTransactional() {
        val applicationContext = DefaultApplicationContext("test")
        applicationContext.environment.addPropertySource(
            MapPropertySource.of(
                "test",
                mapOf(
                    "datasources.default.url" to "jdbc:h2:mem:default;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                    "datasources.default.username" to "sa",
                    "datasources.default.password" to ""
                )
            )
        )
        applicationContext.start()

        try {
            Assertions.assertTrue(applicationContext.containsBean(DataSource::class.java))
            Assertions.assertTrue(applicationContext.containsBean(DataSourceTransactionManager::class.java))

            val transactionManager = applicationContext.getBean(DataSourceTransactionManager::class.java)

            val dataSource = applicationContext.getBean(DataSource::class.java)
            transactionManager.executeWrite {
                dataSource.connection.use { conn ->
                    conn.createStatement().use { statement ->
                        statement.execute(setupDatabase)
                    }
                }
            }
            Assertions.assertTrue(applicationContext.containsBean(TestService::class.java))
            val testService = applicationContext.getBean(TestService::class.java)

            val entities = setOf(TestData("one"), TestData("two"))
            entities.forEach { testData ->
                testService.save(testData)
            }
            val resultEntities = testService.readAllStream()

            entities.forEach { expected ->
                Assertions.assertTrue(resultEntities.contains(expected))
            }
        } finally {
            applicationContext.stop()
        }
    }

    @Test
    fun testWithConfiguration() {
        val applicationContext = DefaultApplicationContext("test")
        applicationContext.environment.addPropertySource(
            MapPropertySource.of(
                "test",
                mapOf(
                    "datasources.default.url" to "jdbc:h2:mem:default;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                    "datasources.default.username" to "sa",
                    "datasources.default.password" to "",
                    "requery.default.statement-cache-size" to "1"
                )
            )
        )
        applicationContext.start()

        try {
            Assertions.assertTrue(applicationContext.containsBean(RequeryDataSourceConfiguration::class.java))
            val configuration = applicationContext.getBean(RequeryDataSourceConfiguration::class.java)

            Assertions.assertEquals(configuration.statementCacheSize, 1)
        } finally {
            applicationContext.stop()
        }
    }

    @Test
    fun testSaveDataWithoutTransaction() {
        val applicationContext = DefaultApplicationContext("test")
        applicationContext.environment.addPropertySource(
            MapPropertySource.of(
                "test",
                mapOf(
                    "datasources.default.url" to "jdbc:h2:mem:default;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                    "datasources.default.username" to "sa",
                    "datasources.default.password" to ""
                )
            )
        )
        applicationContext.start()

        try {
            Assertions.assertTrue(applicationContext.containsBean(DataSource::class.java))
            Assertions.assertTrue(applicationContext.containsBean(DataSourceTransactionManager::class.java))

            val transactionManager = applicationContext.getBean(DataSourceTransactionManager::class.java)

            val dataSource = applicationContext.getBean(DataSource::class.java)
            transactionManager.executeWrite {
                dataSource.connection.use { conn ->
                    conn.createStatement().use { statement ->
                        statement.execute(setupDatabase)
                    }
                }
            }
            Assertions.assertTrue(applicationContext.containsBean(TestService::class.java))
            val testService = applicationContext.getBean(TestService::class.java)

            Assertions.assertNull(testService.findById("Hello World"))
            Assertions.assertThrows(NoTransactionException::class.java) {
                testService.saveWithoutTransaction(TestData("Hello World"))
            }
            Assertions.assertNull(testService.findById("Hello World"))
        } finally {
            applicationContext.stop()
        }
    }

    @Test
    fun testMultipleDataSource() {
        val applicationContext = DefaultApplicationContext("test")
        applicationContext.environment.addPropertySource(
            MapPropertySource.of(
                "test",
                mapOf(
                    "datasources.default.url" to "jdbc:h2:mem:default;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                    "datasources.default.username" to "sa",
                    "datasources.default.password" to "",
                    "datasources.second-datasource.url" to "jdbc:h2:mem:second-datasource;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                    "datasources.second-datasource.username" to "sa",
                    "datasources.second-datasource.password" to ""
                )
            )
        )
        applicationContext.start()

        try {
            Assertions.assertTrue(applicationContext.containsBean(DataSource::class.java))
            Assertions.assertTrue(applicationContext.containsBean(DataSourceTransactionManager::class.java))

            val transactionManager = applicationContext.getBean(DataSourceTransactionManager::class.java)

            val dataSource = applicationContext.getBean(DataSource::class.java)
            transactionManager.executeWrite {
                dataSource.connection.use { conn ->
                    conn.createStatement().use { statement ->
                        statement.execute(setupDatabase)
                    }
                }
            }

            val transactionManagerSecondDatasource = applicationContext.getBean(DataSourceTransactionManager::class.java, Qualifiers.byName("second-datasource"))
            val dataSourceSecondDatasource = applicationContext.getBean(DataSource::class.java, Qualifiers.byName("second-datasource"))
            transactionManagerSecondDatasource.executeWrite {
                dataSourceSecondDatasource.connection.use { conn ->
                    conn.createStatement().use { statement ->
                        statement.execute(setupDatabase)
                    }
                }
            }
            Assertions.assertTrue(applicationContext.containsBean(TestService::class.java))
            Assertions.assertTrue(applicationContext.containsBean(TestServiceSecondDatasource::class.java))
            val testService = applicationContext.getBean(TestService::class.java)
            val testServiceSecondDatasource = applicationContext.getBean(TestServiceSecondDatasource::class.java)

            Assertions.assertNull(testService.findById("Hello World"))
            testService.save(TestData("Hello World"))
            Assertions.assertNotNull(testService.findById("Hello World"))

            Assertions.assertNull(testServiceSecondDatasource.findById("Hello World"))
            testServiceSecondDatasource.save(TestData("Hello World"))
            Assertions.assertNotNull(testServiceSecondDatasource.findById("Hello World"))
        } finally {
            applicationContext.stop()
        }
    }
}

@Factory
class MicronautTestFactory {

    @Bean
    @Primary
    fun entityModel(): EntityModel {
        return Models.DEFAULT
    }

    @Bean
    @Named("second-datasource")
    fun secondEntityModel(): EntityModel {
        return Models.DEFAULT
    }
}

@Singleton
open class TestServiceSecondDatasource(
    @Named("second-datasource") private val entityDataStore: MicronautKotlinEntityDataStore<TestData>
) {
    @TransactionalAdvice("second-datasource")
    @Transactional
    open fun save(entity: TestData) {
        entityDataStore.upsert(entity)
    }

    open fun findById(id: String): TestData? {
        return entityDataStore.findByKey(TestData::class, id)
    }
}

@Singleton
open class TestService(
    private val entityDataStore: MicronautKotlinEntityDataStore<TestData>
) {
    @Transactional
    open fun save(entity: TestData) {
        entityDataStore.upsert(entity)
    }

    @Transactional
    open fun saveWithException(entity: TestData) {
        entityDataStore.upsert(entity)
        throw RuntimeException("test")
    }

    @Transactional
    open fun saveReadStream(entities: Set<TestData>): Set<TestData> {
        entityDataStore.upsert(entities)
        return readAllStream()
    }

    open fun readAllStream(): Set<TestData> {
        return entityDataStore.select(TestData::class)
            .get()
            // Stream must be closed
            .stream().use {
                it.collect(Collectors.toSet())
            }
    }

    open fun readAllIterator(): Set<TestData> {
        val entities = HashSet<TestData>()
        entityDataStore.select(TestData::class)
            .get()
            // Stream must be closed
            .iterator().use {
                while (it.hasNext()) {
                    entities.add(it.next())
                }
            }
        return entities
    }

    open fun saveWithoutTransaction(entity: TestData) {
        entityDataStore.upsert(entity)
    }

    open fun findById(id: String): TestData? {
        return entityDataStore.findByKey(TestData::class, id)
    }
}
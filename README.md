# Micronaut-Kotlin-Requery

Micronaut support for [Requery](https://github.com/requery/requery)

## HowTo To install
```kotlin
dependencies {
    //...
    kapt("io.requery:requery-processor:${requeryVersion}")
    //...
    implementation("com.github.ryarnyah:micronaut-kotlin-requery:${micronautKotlinRequeryVersion}")
    implementation("io.requery:requery:${requeryVersion}")
    implementation("io.requery:requery-kotlin:${requeryVersion}")
    //...
}
```

## HowToUse
```kotlin
@Entity
data class TestData (
    @get:Key
    @get:Column(nullable = false, length = 256)
    var uid: String
)

@Factory
class MicronautTestFactory {

    @Bean
    fun entityModel(): EntityModel {
        return Models.DEFAULT
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
    
    open fun findById(id: String): TestData? {
        return entityDataStore.findByKey(TestData::class, id)
    }
}
```

## Configuration
Basic configuration can be applied
```yaml
requery:
  default:
    use-default-logging: false # boolean
    statement-cache-size: 0 # integer
    batch-update-size: 64 # integer
    quote-table-names: false # boolean
    quote-column-names: false # boolean
```
For more specific configuration, define a bean with `RequeryDataSourceConfiguration.DEFAULT` quelifier.
```kotlin
@Factory
class MicronautTestFactory {

    @Bean
    @Named(RequeryDataSourceConfiguration.DEFAULT)
    fun requeryDataSourceConfiguration(): RequeryDataSourceConfiguration {
        val configuration = RequeryDataSourceConfiguration()
        configuration.useDefaultLogging = true
        return configuration
    }
}
```
You can use multiple dataSource like with JDBC (it will try to find `dataSource`, `transactionManager` and `entityModel` with the same qualifier)
```yaml
requery:
  second-datasource:
```
```kotlin
@Factory
class MicronautTestFactory {
    
    @Bean
    // Define it as primary
    @Primary
    fun entityModel(): EntityModel {
        return Models.DEFAULT
    }

    @Bean
    @Named("second-datasource")
    fun entityModel(): EntityModel {
        return Models.DEFAULT
    }

}
```

## Current status
- [X] Add `LinkedDataSource` to solve `statement.getConnection().close()` (in requery)
- [X] Disable requery transaction to prefer using Micronaut transaction support
- [X] Disable requery cache using `EmptyEntityCache`
# Micronaut-Requery

Micronaut support for [Requery](https://github.com/requery/requery)

## HowTo To install
```kotlin
dependencies {
    //...
    kapt("io.requery:requery-processor:${requeryVersion}")
    //...
    implementation("com.github.ryarnyah:micronaut-requery:${micronautKotlinRequeryVersion}")
    implementation("io.requery:requery:${requeryVersion}")
    implementation("io.requery:requery-kotlin:${requeryVersion}")
    //...
}
```

## HowToUse
### Kotlin version
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
### Java version
```java
@Entity
public class TestData extends Observable, Parcelable, Persistable {
    @Key
    @Generated
    String getUid();
}

@Factory
public class MicronautTestFactory {

    @Bean
    public EntityModel entityModel() {
        return Models.DEFAULT;
    }
}

@Singleton
public class TestService {
    
    private final MicronautEntityDataStore<TestData> entityDataStore;

    public TestService(MicronautEntityDataStore<TestData> entityDataStore) {
        this.entityDataStore = entityDataStore;
    }

    @Transactional
    public void save(TestData entity) {
        entityDataStore.upsert(entity);
    }
    
    public TestData findById(String id) {
        return entityDataStore.findByKey(TestData.class, id);
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
For more specific configuration, define a bean with `RequeryDataSourceConfiguration.DEFAULT` qualifier.
### Kotlin version
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
### Java version
```java
@Factory
public class MicronautTestFactory {

    @Bean
    @Named(RequeryDataSourceConfiguration.DEFAULT)
    public RequeryDataSourceConfiguration requeryDataSourceConfiguration() {
        RequeryDataSourceConfiguration configuration = new RequeryDataSourceConfiguration();
        configuration.setUseDefaultLogging(true);
        return configuration;
    }
}
```
You can use multiple dataSource like with JDBC (it will try to find `dataSource`, `transactionManager` and `entityModel` with the same qualifier)
```yaml
requery:
  second-datasource:
```
### Kotlin version
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
    fun secondEntityModel(): EntityModel {
        return Models.DEFAULT
    }

}
```
### Java version
```java
@Factory
public class MicronautTestFactory {
    
    @Bean
    // Define it as primary
    @Primary
    public EntityModel entityModel() {
        return Models.DEFAULT;
    }

    @Bean
    @Named("second-datasource")
    public EntityModel secondEntityModel() {
        return Models.DEFAULT;
    }

}
```

## Current status
- [X] Add `LinkedDataSource` to solve `statement.getConnection().close()` (in requery)
- [X] Disable requery transaction to prefer using Micronaut transaction support
- [X] Disable requery cache using `EmptyEntityCache`
- [X] Add Java Support
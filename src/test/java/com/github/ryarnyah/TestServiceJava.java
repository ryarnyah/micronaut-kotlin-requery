package com.github.ryarnyah;

import com.github.ryarnyah.model.TestData;
import com.github.ryarnyah.requery.datastore.MicronautKotlinEntityDataStore;
import kotlin.jvm.JvmClassMappingKt;

import javax.inject.Singleton;

@Singleton
public class TestServiceJava {

    private final MicronautKotlinEntityDataStore<TestData> entityStore;

    public TestServiceJava(MicronautKotlinEntityDataStore<TestData> entityStore) {
        this.entityStore = entityStore;
    }

    public TestData findById(String id) {
        return entityStore.findByKey(JvmClassMappingKt.getKotlinClass(TestData.class), id);
    }
}

package com.github.ryarnyah;

import com.github.ryarnyah.model.TestData;
import com.github.ryarnyah.requery.datastore.MicronautEntityDataStore;

import javax.inject.Singleton;

@Singleton
public class TestServiceJava {

    private final MicronautEntityDataStore<TestData> entityStore;

    public TestServiceJava(MicronautEntityDataStore<TestData> entityStore) {
        this.entityStore = entityStore;
    }

    public TestData findById(String id) {
        return entityStore.findByKey(TestData.class, id);
    }
}

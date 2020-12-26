package com.github.ryarnyah.model

import io.requery.Column
import io.requery.Entity
import io.requery.Key

@Entity
data class TestData (
    @get:Key
    @get:Column(nullable = false, length = 256)
    var uid: String
)
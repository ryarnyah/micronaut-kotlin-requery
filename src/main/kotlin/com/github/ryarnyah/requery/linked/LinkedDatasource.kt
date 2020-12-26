package com.github.ryarnyah.requery.linked

import java.sql.Connection
import javax.sql.DataSource

class LinkedDatasource(private val delegate: DataSource): DataSource by delegate {

    override fun getConnection(): Connection {
        return LinkedConnection(delegate.connection)
    }

    override fun getConnection(username: String?, password: String?): Connection {
        return LinkedConnection(delegate.getConnection(username, password))
    }
}
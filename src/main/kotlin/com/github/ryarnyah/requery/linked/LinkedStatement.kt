package com.github.ryarnyah.requery.linked

import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

class LinkedStatement(private val delegate: Statement, private val originalConnection: Connection): Statement by delegate {
    override fun getConnection(): Connection {
        return originalConnection
    }

    override fun getResultSet(): ResultSet {
        return LinkedResultSet(delegate.resultSet, this, originalConnection)
    }

    override fun executeQuery(sql: String?): ResultSet {
        return LinkedResultSet(delegate.executeQuery(sql), this, originalConnection)
    }

    override fun getGeneratedKeys(): ResultSet {
        return LinkedResultSet(delegate.generatedKeys, this, originalConnection)
    }
}
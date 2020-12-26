package com.github.ryarnyah.requery.linked

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class LinkedPreparedStatement(private val delegate: PreparedStatement, private val originalConnection: Connection): PreparedStatement by delegate {
    override fun getConnection(): Connection {
        return originalConnection
    }

    override fun getResultSet(): ResultSet {
        return LinkedResultSet(delegate.resultSet, this, originalConnection)
    }

    override fun executeQuery(): ResultSet {
        return LinkedResultSet(delegate.executeQuery(), this, originalConnection)
    }

    override fun executeQuery(sql: String?): ResultSet {
        return LinkedResultSet(delegate.executeQuery(sql), this, originalConnection)
    }

    override fun getGeneratedKeys(): ResultSet {
        return LinkedResultSet(delegate.generatedKeys, this, originalConnection)
    }
}
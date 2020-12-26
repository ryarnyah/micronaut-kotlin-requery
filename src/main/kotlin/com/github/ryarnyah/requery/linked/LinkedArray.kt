package com.github.ryarnyah.requery.linked

import java.sql.Connection
import java.sql.ResultSet

class LinkedArray(private val delegate: java.sql.Array, private val originalConnection: Connection): java.sql.Array by delegate {
    override fun getResultSet(): ResultSet {
        return LinkedResultSet(delegate.resultSet, null, originalConnection)
    }

    override fun getResultSet(map: MutableMap<String, Class<*>>?): ResultSet {
        return LinkedResultSet(delegate.getResultSet(map), null, originalConnection)
    }

    override fun getResultSet(index: Long, count: Int): ResultSet {
        return LinkedResultSet(delegate.getResultSet(index, count), null, originalConnection)
    }

    override fun getResultSet(index: Long, count: Int, map: MutableMap<String, Class<*>>?): ResultSet {
        return LinkedResultSet(delegate.getResultSet(index, count, map), null, originalConnection)
    }
}
package com.github.ryarnyah.requery.linked

import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

class LinkedResultSet(private val delegate: ResultSet,
                      private val originalStatement: Statement?,
                      private val originalConnection: Connection
): ResultSet by delegate {
    override fun getStatement(): Statement {
        return originalStatement ?: LinkedStatement(delegate.statement, originalConnection)
    }
}
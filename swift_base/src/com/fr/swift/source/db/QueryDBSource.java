package com.fr.swift.source.db;

import com.fr.swift.source.core.CoreField;

import java.util.Map;

public class QueryDBSource extends AbstractDBDataSource {

    @CoreField
    protected String query;
    @CoreField
    protected String connection;

    public QueryDBSource(String query, String connection) {
        this.query = query;
        this.connection = connection;
    }

    public QueryDBSource(String query, String connection, Map<String, Integer> fieldColumnTypes) {
        super(fieldColumnTypes);
        this.query = query;
        this.connection = connection;
        this.fieldColumnTypes = fieldColumnTypes;
    }


    public String getQuery() {
        return query;
    }

    public String getConnectionName() {
        return connection;
    }

    @Override
    protected void initOuterMetaData() {
        ConnectionInfo connectionInfo = ConnectionManager.getInstance().getConnectionInfo(connection);
        outerMetaData = DBSourceUtils.getQueryMetaData(connectionInfo.getFrConnection(), query);
    }
}

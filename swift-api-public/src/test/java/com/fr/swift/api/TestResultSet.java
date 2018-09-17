package com.fr.swift.api;

import com.fr.swift.source.Row;
import com.fr.swift.source.SwiftMetaData;
import com.fr.swift.source.SwiftResultSet;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

/**
 * @author yee
 * @date 2018/8/24
 */
public class TestResultSet implements SwiftResultSet, Serializable {

    int cursor = 0;
    private List<Row> rows;

    public TestResultSet(List<Row> rows) {
        this.rows = rows;
    }

    @Override
    public int getFetchSize() {
        return rows.size();
    }

    @Override
    public SwiftMetaData getMetaData() throws SQLException {
        return null;
    }

    @Override
    public boolean hasNext() throws SQLException {
        return cursor < rows.size();
    }

    @Override
    public Row getNextRow() throws SQLException {
        return rows.get(cursor++);
    }

    @Override
    public void close() throws SQLException {

    }
}
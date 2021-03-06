package com.fr.swift.cloud.jdbc.sql;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author yee
 * @date 2018/11/19
 */
public interface SwiftStatement extends Statement {
    /**
     * statement id
     *
     * @return statement id
     */
    String getObjId();

    /**
     * reset the statement
     */
    void reset() throws SQLException;
}

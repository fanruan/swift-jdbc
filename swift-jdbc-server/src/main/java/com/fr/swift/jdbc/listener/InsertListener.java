package com.fr.swift.jdbc.listener;

import com.fr.swift.jdbc.adaptor.InsertionBeanParser;
import com.fr.swift.jdbc.adaptor.bean.InsertionBean;
import com.fr.swift.jdbc.antlr4.SwiftSqlParser;
import com.fr.swift.jdbc.antlr4.SwiftSqlParserBaseListener;
import com.fr.swift.jdbc.visitor.insert.InsertValueVisitor;
import com.fr.swift.source.Row;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yee
 * @date 2019-07-19
 */
public class InsertListener extends SwiftSqlParserBaseListener implements InsertionBeanParser {
    private InsertionBean insertionBean;

    @Override
    public void enterInsert(SwiftSqlParser.InsertContext ctx) {
        List<Row> rows = new ArrayList<>();
        InsertValueVisitor visitor = new InsertValueVisitor();
        for (SwiftSqlParser.ValuesContext value : ctx.values()) {
            rows.add(value.accept(visitor));
        }
        String tableName = ctx.name().getText();
        List<String> fields = new ArrayList<>();
        if (ctx.columnNames != null) {
            for (SwiftSqlParser.NameContext nameContext : ctx.columnNames.name()) {
                fields.add(nameContext.getText());
            }
        }
        insertionBean = new InsertionBean();
        insertionBean.setFields(fields);
        insertionBean.setRows(rows);
        insertionBean.setTableName(tableName);
    }

    @Override
    public InsertionBean getInsertionBean() {
        return insertionBean;
    }

}
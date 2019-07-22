/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fr.swift.jdbc.druid.sql.repository;

import com.fr.swift.jdbc.druid.sql.SQLUtils;
import com.fr.swift.jdbc.druid.sql.ast.SQLExpr;
import com.fr.swift.jdbc.druid.sql.ast.SQLName;
import com.fr.swift.jdbc.druid.sql.ast.expr.SQLAggregateExpr;
import com.fr.swift.jdbc.druid.sql.ast.expr.SQLAllColumnExpr;
import com.fr.swift.jdbc.druid.sql.ast.expr.SQLIdentifierExpr;
import com.fr.swift.jdbc.druid.sql.ast.expr.SQLPropertyExpr;
import com.fr.swift.jdbc.druid.sql.ast.statement.SQLColumnDefinition;
import com.fr.swift.jdbc.druid.sql.ast.statement.SQLExprTableSource;
import com.fr.swift.jdbc.druid.sql.ast.statement.SQLJoinTableSource;
import com.fr.swift.jdbc.druid.sql.ast.statement.SQLSelectItem;
import com.fr.swift.jdbc.druid.sql.ast.statement.SQLTableSource;
import com.fr.swift.jdbc.druid.util.FnvHash;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wenshao on 21/07/2017.
 */
public class Schema {
    protected final Map<Long, SchemaObject> objects = new ConcurrentHashMap<Long, SchemaObject>();
    protected final Map<Long, SchemaObject> functions = new ConcurrentHashMap<Long, SchemaObject>();
    private String name;
    private SchemaRepository repository;

    public Schema(SchemaRepository repository) {
        this(repository, null);
    }

    public Schema(SchemaRepository repository, String name) {
        this.repository = repository;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public SchemaObject findTable(String tableName) {
        long hashCode64 = FnvHash.hashCode64(tableName);
        return findTable(hashCode64);
    }

    public SchemaObject findTable(long nameHashCode64) {
        SchemaObject object = objects.get(nameHashCode64);

        if (object != null && object.getType() == SchemaObjectType.Table) {
            return object;
        }

        return null;
    }

    public SchemaObject findTableOrView(String tableName) {
        long hashCode64 = FnvHash.hashCode64(tableName);
        return findTableOrView(hashCode64);
    }

    public SchemaObject findTableOrView(long hashCode64) {
        SchemaObject object = objects.get(hashCode64);

        if (object == null) {
            return null;
        }

        SchemaObjectType type = object.getType();
        if (type == SchemaObjectType.Table || type == SchemaObjectType.View) {
            return object;
        }

        return null;
    }

    public SchemaObject findFunction(String functionName) {
        functionName = SQLUtils.normalize(functionName);
        String lowerName = functionName.toLowerCase();
        return functions.get(lowerName);
    }

    public boolean isSequence(String name) {
        long nameHashCode64 = FnvHash.hashCode64(name);
        SchemaObject object = objects.get(nameHashCode64);
        return object != null
                && object.getType() == SchemaObjectType.Sequence;
    }


    public SchemaObject findTable(SQLTableSource tableSource, String alias) {
        if (tableSource instanceof SQLExprTableSource) {
            if (alias.equalsIgnoreCase(tableSource.computeAlias())) {
                SQLExprTableSource exprTableSource = (SQLExprTableSource) tableSource;

                SchemaObject tableObject = exprTableSource.getSchemaObject();
                if (tableObject != null) {
                    return tableObject;
                }

                SQLExpr expr = exprTableSource.getExpr();
                if (expr instanceof SQLIdentifierExpr) {
                    long tableNameHashCode64 = ((SQLIdentifierExpr) expr).nameHashCode64();

                    tableObject = findTable(tableNameHashCode64);
                    if (tableObject != null) {
                        exprTableSource.setSchemaObject(tableObject);
                    }
                    return tableObject;
                }

                if (expr instanceof SQLPropertyExpr) {
                    long tableNameHashCode64 = ((SQLPropertyExpr) expr).nameHashCode64();

                    tableObject = findTable(tableNameHashCode64);
                    if (tableObject != null) {
                        exprTableSource.setSchemaObject(tableObject);
                    }
                    return tableObject;
                }
            }
            return null;
        }

        if (tableSource instanceof SQLJoinTableSource) {
            SQLJoinTableSource join = (SQLJoinTableSource) tableSource;
            SQLTableSource left = join.getLeft();

            SchemaObject tableObject = findTable(left, alias);
            if (tableObject != null) {
                return tableObject;
            }

            SQLTableSource right = join.getRight();
            tableObject = findTable(right, alias);
            return tableObject;
        }

        return null;
    }

    public SQLColumnDefinition findColumn(SQLTableSource tableSource, SQLSelectItem selectItem) {
        if (selectItem == null) {
            return null;
        }

        return findColumn(tableSource, selectItem.getExpr());
    }

    public SQLColumnDefinition findColumn(SQLTableSource tableSource, SQLExpr expr) {
        SchemaObject object = findTable(tableSource, expr);
        if (object != null) {
            if (expr instanceof SQLAggregateExpr) {
                SQLAggregateExpr aggregateExpr = (SQLAggregateExpr) expr;
                String function = aggregateExpr.getMethodName();
                if ("min".equalsIgnoreCase(function)
                        || "max".equalsIgnoreCase(function)) {
                    SQLExpr arg = aggregateExpr.getArguments().get(0);
                    expr = arg;
                }
            }

            if (expr instanceof SQLName) {
                return object.findColumn(((SQLName) expr).getSimpleName());
            }
        }

        return null;
    }

    public SchemaObject findTable(SQLTableSource tableSource, SQLSelectItem selectItem) {
        if (selectItem == null) {
            return null;
        }

        return findTable(tableSource, selectItem.getExpr());
    }

    public SchemaObject findTable(SQLTableSource tableSource, SQLExpr expr) {
        if (expr instanceof SQLAggregateExpr) {
            SQLAggregateExpr aggregateExpr = (SQLAggregateExpr) expr;
            String function = aggregateExpr.getMethodName();
            if ("min".equalsIgnoreCase(function)
                    || "max".equalsIgnoreCase(function)) {
                SQLExpr arg = aggregateExpr.getArguments().get(0);
                return findTable(tableSource, arg);
            }
        }

        if (expr instanceof SQLPropertyExpr) {
            String ownerName = ((SQLPropertyExpr) expr).getOwnernName();
            return findTable(tableSource, ownerName);
        }

        if (expr instanceof SQLAllColumnExpr || expr instanceof SQLIdentifierExpr) {
            if (tableSource instanceof SQLExprTableSource) {
                return findTable(tableSource, tableSource.computeAlias());
            }

            if (tableSource instanceof SQLJoinTableSource) {
                SQLJoinTableSource join = (SQLJoinTableSource) tableSource;

                SchemaObject table = findTable(join.getLeft(), expr);
                if (table == null) {
                    table = findTable(join.getRight(), expr);
                }
                return table;
            }
            return null;
        }

        return null;
    }

    public Map<String, SchemaObject> getTables(SQLTableSource x) {
        Map<String, SchemaObject> tables = new LinkedHashMap<String, SchemaObject>();
        computeTables(x, tables);
        return tables;
    }

    protected void computeTables(SQLTableSource x, Map<String, SchemaObject> tables) {
        if (x == null) {
            return;
        }

        if (x instanceof SQLExprTableSource) {
            SQLExprTableSource exprTableSource = (SQLExprTableSource) x;

            SQLExpr expr = exprTableSource.getExpr();
            if (expr instanceof SQLIdentifierExpr) {
                long tableNameHashCode64 = ((SQLIdentifierExpr) expr).nameHashCode64();
                String tableName = ((SQLIdentifierExpr) expr).getName();

                SchemaObject table = exprTableSource.getSchemaObject();
                if (table == null) {
                    table = findTable(tableNameHashCode64);

                    if (table != null) {
                        exprTableSource.setSchemaObject(table);
                    }
                }

                if (table != null) {
                    tables.put(tableName, table);

                    String alias = x.getAlias();
                    if (alias != null && !alias.equalsIgnoreCase(tableName)) {
                        tables.put(alias, table);
                    }
                }
            }

            return;
        }

        if (x instanceof SQLJoinTableSource) {
            SQLJoinTableSource join = (SQLJoinTableSource) x;
            computeTables(join.getLeft(), tables);
            computeTables(join.getRight(), tables);
        }
    }

    public int getTableCount() {
        int count = 0;
        for (SchemaObject object : this.objects.values()) {
            if (object.getType() == SchemaObjectType.Table) {
                count++;
            }
        }
        return count;
    }

    public Collection<SchemaObject> getObjects() {
        return this.objects.values();
    }

    public int getViewCount() {
        int count = 0;
        for (SchemaObject object : this.objects.values()) {
            if (object.getType() == SchemaObjectType.View) {
                count++;
            }
        }
        return count;
    }

    public List<String> showTables() {
        List<String> tables = new ArrayList<String>(objects.size());
        for (SchemaObject object : objects.values()) {
            if (object.getType() == SchemaObjectType.Table) {
                tables.add(object.getName());
            }
        }
        Collections.sort(tables);
        return tables;
    }
}

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
package com.fr.swift.jdbc.druid.sql.ast.expr;

import com.fr.swift.jdbc.druid.sql.SQLUtils;
import com.fr.swift.jdbc.druid.sql.ast.SQLDataType;
import com.fr.swift.jdbc.druid.sql.ast.SQLExpr;
import com.fr.swift.jdbc.druid.sql.ast.SQLExprImpl;
import com.fr.swift.jdbc.druid.sql.ast.SQLObject;
import com.fr.swift.jdbc.druid.sql.ast.statement.SQLSelect;
import com.fr.swift.jdbc.druid.sql.visitor.SQLASTOutputVisitor;
import com.fr.swift.jdbc.druid.sql.visitor.SQLASTVisitor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class SQLInSubQueryExpr extends SQLExprImpl implements Serializable {

    private static final long serialVersionUID = -1661112482512130596L;
    public SQLSelect subQuery;
    private boolean not = false;
    private SQLExpr expr;

    public SQLInSubQueryExpr() {

    }

    public SQLInSubQueryExpr(SQLSelect select) {

        this.subQuery = select;
    }

    public SQLInSubQueryExpr clone() {
        SQLInSubQueryExpr x = new SQLInSubQueryExpr();
        x.not = not;
        if (expr != null) {
            x.setExpr(expr.clone());
        }
        if (subQuery != null) {
            x.setSubQuery(subQuery.clone());
        }
        return x;
    }

    public boolean isNot() {
        return not;
    }

    public void setNot(boolean not) {
        this.not = not;
    }

    public SQLExpr getExpr() {
        return expr;
    }

    public void setExpr(SQLExpr expr) {
        if (expr != null) {
            expr.setParent(this);
        }
        this.expr = expr;
    }

    public SQLSelect getSubQuery() {
        return this.subQuery;
    }

    public void setSubQuery(SQLSelect subQuery) {
        if (subQuery != null) {
            subQuery.setParent(this);
        }
        this.subQuery = subQuery;
    }

    public void output(StringBuffer buf) {
        SQLASTOutputVisitor visitor = SQLUtils.createOutputVisitor(buf, null);
        this.accept(visitor);
    }

    @Override
    protected void accept0(SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, this.expr);
            acceptChild(visitor, this.subQuery);
        }

        visitor.endVisit(this);
    }

    public List<SQLObject> getChildren() {
        return Arrays.asList(this.expr, this.subQuery);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expr == null) ? 0 : expr.hashCode());
        result = prime * result + (not ? 1231 : 1237);
        result = prime * result + ((subQuery == null) ? 0 : subQuery.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SQLInSubQueryExpr other = (SQLInSubQueryExpr) obj;
        if (expr == null) {
            if (other.expr != null) {
                return false;
            }
        } else if (!expr.equals(other.expr)) {
            return false;
        }
        if (not != other.not) {
            return false;
        }
        if (subQuery == null) {
            return other.subQuery == null;
        } else return subQuery.equals(other.subQuery);
    }

    public SQLDataType computeDataType() {
        return SQLBooleanExpr.DEFAULT_DATA_TYPE;
    }
}

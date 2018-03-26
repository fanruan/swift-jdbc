package com.fr.swift.cal.result.detail;

import com.fr.swift.cal.Query;
import com.fr.swift.query.adapter.target.DetailTarget;
import com.fr.swift.result.DetailResultSet;
import com.fr.swift.result.SortMultiSegmentDetailResultSet;


import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

/**
 * Created by pony on 2017/11/27.
 */
public class SortDetailResultQuery extends AbstractDetailResultQuery {

    private Comparator comparator;

    public SortDetailResultQuery(List<Query<DetailResultSet>> queries, Comparator comparator) {
        super(queries);
        this.comparator = comparator;
    }

    public SortDetailResultQuery(List<Query<DetailResultSet>> queries, DetailTarget[] targets, Comparator comparator) {
        super(queries, targets);
        this.comparator = comparator;
    }

    @Override
    public DetailResultSet getQueryResult() throws SQLException {

        if(queryList.size() == 0) {
            return null;
        }

        if(queryList.size() == 1) {
            return queryList.get(0).getQueryResult();
        }
        return new SortMultiSegmentDetailResultSet(queryList, comparator);
    }
}

package com.fr.swift.segment;

import com.fr.swift.source.ListBasedRow;
import com.fr.swift.source.MetaDataColumn;
import com.fr.swift.source.SwiftMetaData;
import com.fr.swift.source.SwiftMetaDataImpl;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author yee
 * @date 2018/1/9
 */
public class DateResultSet extends SingleColumnResultSet {

    int count;

    @Override
    public SwiftMetaData getMetaData() throws SQLException {
        return new SwiftMetaDataImpl("DATE_TABLE",
                Arrays.asList(new MetaDataColumn("date", Types.BIGINT)));
    }


    @Override
    protected void initData() {
        count = (int) (Math.random() * 1000000);
        for (int i = 0; i < count; i++) {
            List data = new ArrayList<Long>();
            data.add((long) (System.currentTimeMillis() + Math.random() * 1000000));
            datas.add(new ListBasedRow(data));
        }
        List data = new ArrayList<Long>();
        data.add(null);
        datas.add(new ListBasedRow(data));
    }
}

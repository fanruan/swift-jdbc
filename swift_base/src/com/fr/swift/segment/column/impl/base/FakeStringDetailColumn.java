package com.fr.swift.segment.column.impl.base;

import com.fr.swift.compare.Comparators;
import com.fr.swift.segment.column.Column;
import com.fr.swift.segment.column.DetailColumn;
import com.fr.swift.segment.column.DictionaryEncodedColumn;
import com.fr.swift.source.ColumnTypeConstants.CLASS;
import com.fr.swift.structure.array.IntList;
import com.fr.swift.structure.array.IntListFactory;
import com.fr.swift.structure.external.map.ExternalMap;
import com.fr.swift.structure.external.map.intlist.map2.ExternalIntArrayMapFactory;
import com.fr.swift.util.Crasher;

import java.util.Comparator;
import java.util.concurrent.TimeUnit;

/**
 * @author anchore
 * @date 2017/12/26
 * <p>
 * 假的明细列，put get都操作的dict和index
 */
public class FakeStringDetailColumn implements DetailColumn<String> {
    private Column<String> hostColumn;
    private DictionaryEncodedColumn<String> dictColumn;

    private ExternalMap<String, IntList> map;

    public FakeStringDetailColumn(Column<String> hostColumn) {
        this.hostColumn = hostColumn;
        map = newIntListExternalMap(Comparators.PINYIN_ASC);
    }

    @Override
    public void put(int row, String val) {
        // val不判空，由上层判断，并写nullIndex
        IntList list = map.get(val);
        if (list == null) {
            list = IntListFactory.createIntList();
            list.add(row);
            map.put(val, list);
        } else {
            list.add(row);
        }
    }

    @Override
    public String get(int row) {
        initDictColumn();
        int index = dictColumn.getIndexByRow(row);
        return dictColumn.getValue(index);
    }

    private void initDictColumn() {
        if (dictColumn == null) {
            dictColumn = hostColumn.getDictionaryEncodedColumn();
        }
    }

    /**
     * 计算对应的外排数据存放位置
     * Column数据位置：.../table/segment/column/...
     * 对应的字典External数据位置 ：.../table/segment/column/external/...
     *
     * @return extMap位置
     */
    private String calExternalLocation() {
        return hostColumn.getLocation()
                .buildChildLocation("external")
                .getPath();
    }

    private ExternalMap<String, IntList> newIntListExternalMap(Comparator<String> c) {
        return ExternalIntArrayMapFactory.getIntListExternalMap(CLASS.STRING, c, calExternalLocation(), true);
    }

    private void waitUtilDumpOver() {
        try {
            while (!map.isDumpComplete()) {
                TimeUnit.MILLISECONDS.sleep(1);
            }
        } catch (InterruptedException e) {
            Crasher.crash(e);
        }
    }

    @Override
    public void flush() {
        if (map != null) {
            // dump完，持久化map的一些信息，以便下一步直接索引
            map.dumpMap();
            waitUtilDumpOver();
            map.writeExternal();
            map.release();
            map = null;

        }
    }

    @Override
    public void release() {
        flush();

        if (dictColumn != null) {
            dictColumn.release();
            dictColumn = null;
        }
    }

    @Override
    public int getInt(int pos) {
        return Crasher.crash("not allowed");
    }

    @Override
    public long getLong(int pos) {
        return Crasher.crash("not allowed");
    }

    @Override
    public double getDouble(int pos) {
        return Crasher.crash("not allowed");
    }
}
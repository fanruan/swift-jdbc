package com.fr.swift.segment.column.impl.base;

import com.fr.swift.segment.column.DictionaryEncodedColumn;
import com.fr.swift.test.TestIo;
import org.junit.Assert;
import org.junit.Test;

import java.util.Comparator;
import java.util.Random;

/**
 * @author anchore
 * @date 2017/11/10
 */
public abstract class BaseDictColumnTest<T> extends TestIo {

    static final String BASE_PATH = "cubes/table/seg0/column";
    Random r = new Random();
    T[] values;
    Comparator<T> c;

    abstract DictionaryEncodedColumn<T> getDictColumn();

    @Test
    public void testPutValueThenGet() {
        DictionaryEncodedColumn<T> dictColumn = getDictColumn();
        dictColumn.putter().putValue(0, null);
        for (int i = 1; i < values.length; i++) {
            dictColumn.putter().putValue(i, values[i]);
        }
        dictColumn.putter().putSize(values.length);
        dictColumn.release();

        dictColumn = getDictColumn();
        for (int i = 1; i < values.length; i++) {
            Assert.assertEquals(i, dictColumn.getIndex(values[i]));
            Assert.assertEquals(values[i], dictColumn.getValue(i));
        }
        dictColumn.release();
    }

    @Test
    public void testPutSizeThenGet() {
        DictionaryEncodedColumn<T> dictColumn = getDictColumn();
        int size = r.nextInt(1000000000);
        dictColumn.putter().putSize(size);
        dictColumn.release();

        dictColumn = getDictColumn();
        Assert.assertEquals(size, dictColumn.size());
        dictColumn.release();
    }

    @Test
    public void testPutIndexThenGet() {
        int size = 100000;
        DictionaryEncodedColumn<T> dictColumn = getDictColumn();
        int[] indices = r.ints(size, 0, size << 1).toArray();
        for (int i = 0; i < indices.length; i++) {
            dictColumn.putter().putIndex(i, indices[i]);
        }
        dictColumn.release();

        dictColumn = getDictColumn();
        for (int i = 0; i < indices.length; i++) {
            Assert.assertEquals(indices[i], dictColumn.getIndexByRow(i));
        }
        dictColumn.release();
    }
}
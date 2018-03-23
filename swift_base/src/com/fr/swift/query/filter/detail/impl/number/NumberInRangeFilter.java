package com.fr.swift.query.filter.detail.impl.number;

import com.fr.swift.compare.Comparators;
import com.fr.swift.constant.SwiftConstants;
import com.fr.swift.query.filter.detail.impl.AbstractFilter;
import com.fr.swift.query.filter.detail.impl.util.LookupFactory;
import com.fr.swift.result.SwiftNode;
import com.fr.swift.segment.column.Column;
import com.fr.swift.segment.column.DictionaryEncodedColumn;
import com.fr.swift.structure.array.IntListFactory;
import com.fr.swift.structure.iterator.IntListRowTraversal;
import com.fr.swift.structure.iterator.RowTraversal;
import com.fr.swift.util.ArrayLookupHelper;
import com.fr.swift.util.MatchAndIndex;
import com.fr.swift.util.Util;

/**
 * Created by Lyon on 2017/11/27.
 */
public class NumberInRangeFilter extends AbstractFilter<Number> {

    private final static int START_INDEX = SwiftConstants.DICTIONARY.NOT_NULL_START_INDEX;

    protected final Double min;
    protected final Double max;
    protected final boolean minIncluded;
    protected final boolean maxIncluded;

    public NumberInRangeFilter(Double min, Double max, boolean minIncluded, boolean maxIncluded, Column<Number> column) {
        Util.requireNotGreater(min, max);
        this.min = min;
        this.max = max;
        this.minIncluded = minIncluded;
        this.maxIncluded = maxIncluded;
        this.column = column;
    }

    @Override
    protected RowTraversal getIntIterator(final DictionaryEncodedColumn<Number> dict) {
        ArrayLookupHelper.Lookup<Number> lookup = LookupFactory.create(dict, Comparators.numberAsc());
        // 获取过滤条件对应的RangeIntList区间
        int start = min == Double.MIN_VALUE ? START_INDEX : getStart(ArrayLookupHelper.binarySearch(lookup, min));
        int end = max == Double.MAX_VALUE ? dict.size() - 1 : getEnd(ArrayLookupHelper.binarySearch(lookup, max));
        start = start < START_INDEX ? START_INDEX : start;
        if (start >= dict.size() || end < START_INDEX || start > end) {
            return new IntListRowTraversal(IntListFactory.createEmptyIntList());
        }
        return new IntListRowTraversal(IntListFactory.createRangeIntList(start, end));
    }

    private int getEnd(MatchAndIndex maxMatchAndIndex) {
        int end;
        if (maxMatchAndIndex.isMatch()) {
            if (maxIncluded) {
                end = maxMatchAndIndex.getIndex();
            } else {
                // max这个分组值存在但是不包含该分组值的条件下，索引值减1
                end = maxMatchAndIndex.getIndex() - 1;
            }
        } else {
            // max这个分组值不存在的条件下，取当前索引值
            end = maxMatchAndIndex.getIndex();
        }
        return end;
    }

    private int getStart(MatchAndIndex minMatchAndIndex) {
        int start;
        if (minMatchAndIndex.isMatch()) {
            if (minIncluded) {
                start = minMatchAndIndex.getIndex();
            } else {
                // min这个分组值存在但是不包含该分组值的条件下，索引值加1
                start = minMatchAndIndex.getIndex() + 1;
            }
        } else {
            // min这个分组值不存在的条件下，索引值要加1
            start = minMatchAndIndex.getIndex() + 1;
        }
        return start;
    }

    @Override
    public boolean matches(SwiftNode node) {
        Object data = node.getData();
        if (data == null) {
            return false;
        }
        double value = ((Number) data).doubleValue();
        double minValue = min.doubleValue();
        double maxValue = max.doubleValue();
        return (minIncluded ? value >= minValue : value > minValue) &&
                (maxIncluded ? value <= maxValue : value < maxValue);
    }
}

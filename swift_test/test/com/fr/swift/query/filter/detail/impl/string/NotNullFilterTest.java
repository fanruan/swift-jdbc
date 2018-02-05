package com.fr.swift.query.filter.detail.impl.string;

import com.fr.swift.query.filter.detail.impl.NotNullFilter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Lyon on 2017/11/30.
 */
public class NotNullFilterTest extends BaseStringFilterTest {

    public NotNullFilterTest() {
        this.filter = new NotNullFilter(details.size(), column);
    }

    @Override
    protected List<Integer> getExpectedIndexes() {
        return IntStream.range(0, details.size()).filter(i -> !details.get(i).equals(NULL_VALUE))
                .mapToObj(Integer::new).collect(Collectors.toList());
    }

    @Override
    public void testMatch() {}
}

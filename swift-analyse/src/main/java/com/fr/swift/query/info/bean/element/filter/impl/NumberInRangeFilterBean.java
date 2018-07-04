package com.fr.swift.query.info.bean.element.filter.impl;

import com.fr.swift.query.filter.SwiftDetailFilterType;

/**
 * Created by Lyon on 2018/6/28.
 */
public class NumberInRangeFilterBean extends DetailFilterInfoBean<RangeFilterValueBean> {

    {
        type = SwiftDetailFilterType.NUMBER_IN_RANGE;
    }

    @Override
    public RangeFilterValueBean getFilterValue() {
        return filterValue;
    }

    @Override
    public void setFilterValue(RangeFilterValueBean filterValue) {
        this.filterValue = filterValue;
    }
}

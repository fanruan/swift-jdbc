package com.fr.bi.stable.report.result;

import com.finebi.cube.api.ICubeDataLoader;
import com.finebi.cube.api.ICubeTableService;
import com.finebi.cube.conf.table.BusinessTable;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.report.key.TargetGettingKey;

import java.io.Serializable;

/**
 * Created by 小灰灰 on 2015/6/30.
 */
public interface TargetCalculator extends Serializable {


    /**
     * 计算
     *
     * @param cr   tableindex对象
     * @param node 节点
     */
    void doCalculator(ICubeTableService cr, SummaryContainer node, GroupValueIndex gvi, TargetGettingKey key);

    /**
     * 构建计算指标-
     *
     * @return 计算指标
     */
    TargetCalculator[] createTargetCalculators();

    /**
     * 先计算好过滤的index
     */
    void calculateFilterIndex(ICubeDataLoader loader);

    BusinessTable createTableKey();

    String getName();

    TargetGettingKey createTargetGettingKey();
}
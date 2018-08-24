package com.fr.swift.adaptor.log;

import com.fr.intelli.record.scene.impl.BaseMetric;
import com.fr.stable.query.condition.QueryCondition;
import com.fr.stable.query.data.DataList;
import com.fr.swift.context.SwiftContext;
import com.fr.swift.db.Database;
import com.fr.swift.db.Table;
import com.fr.swift.db.impl.SwiftDatabase;
import com.fr.swift.db.impl.SwiftDatabase.Schema;
import com.fr.swift.db.impl.SwiftWhere;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.query.query.FilterBean;
import com.fr.swift.segment.Segment;
import com.fr.swift.segment.SwiftSegmentManager;
import com.fr.swift.segment.operator.delete.WhereDeleter;
import com.fr.swift.service.RealtimeService;
import com.fr.swift.source.Row;
import com.fr.swift.source.SourceKey;
import com.fr.swift.source.SwiftMetaData;
import com.fr.swift.source.SwiftMetaDataColumn;
import com.fr.swift.util.JpaAdaptor;
import com.fr.swift.util.concurrent.PoolThreadFactory;
import com.fr.swift.util.concurrent.SwiftExecutors;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author anchore
 * @date 2018/4/26
 */
public class SwiftLogOperator extends BaseMetric {

    private final Database db = SwiftDatabase.getInstance();

    @Override
    public <T> DataList<T> find(Class<T> entity, QueryCondition queryCondition) {
        DataList<T> dataList = new DataList<T>();
        try {
            Table table = db.getTable(new SourceKey(JpaAdaptor.getTableName(entity)));
            DecisionRowAdaptor<T> adaptor = new DecisionRowAdaptor<T>(entity, table.getMeta());
            List<T> tList = new ArrayList<T>();
            DataList<Row> rowDataList = LogQueryUtils.detailQuery(entity, queryCondition);
            List<Row> rows = rowDataList.getList();
            for (Row row : rows) {
                tList.add(adaptor.apply(row));
            }
            dataList.list(tList);
            dataList.setTotalCount(rowDataList.getTotalCount());
        } catch (Exception e) {
            SwiftLoggers.getLogger().error(e);
        }
        return dataList;
    }

    @Override
    public <T> DataList<List<T>> find(String s) {
        return null;
    }

    @Override
    public void submit(Object o) {
        if (o == null) {
            return;
        }
        sync.stage(Collections.singletonList(o));
    }

    @Override
    public void submit(List<Object> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        sync.stage(list);
    }

    @Override
    public void pretreatment(List<Class> list) throws Exception {
        for (Class table : list) {
            initTable(table);
        }
    }

    private void initTable(Class table) throws SQLException {
        SwiftMetaData meta = JpaAdaptor.adapt(table, Schema.DECISION_LOG);
        SourceKey tableKey = new SourceKey(meta.getTableName());
        synchronized (db) {
            if (!db.existsTable(tableKey)) {
                db.createTable(tableKey, meta);
            }
        }
    }

    @Override
    public void clean(QueryCondition condition) throws Exception {
        List<Table> tables = SwiftDatabase.getInstance().getAllTables();
        SwiftSegmentManager localSegmentProvider = SwiftContext.get().getBean("localSegmentProvider", SwiftSegmentManager.class);
        FilterBean filterBean = QueryConditionAdaptor.restriction2FilterInfo(condition.getRestriction());
        for (Table table : tables) {
            for (Segment segment : localSegmentProvider.getSegment(table.getSourceKey())) {
                WhereDeleter whereDeleter = (WhereDeleter) SwiftContext.get().getBean("decrementer", table.getSourceKey(), segment);
                whereDeleter.delete(new SwiftWhere(filterBean));
            }
        }
    }

    private Sync sync = new Sync();

    class Sync implements Runnable {
        static final int FLUSH_SIZE_THRESHOLD = 10000;

        private ScheduledExecutorService scheduler = SwiftExecutors.newScheduledThreadPool(1, new PoolThreadFactory(getClass()));

        private Map<Class<?>, List<Object>> dataMap = new ConcurrentHashMap<Class<?>, List<Object>>();

        private RealtimeService realtimeService = SwiftContext.get().getBean("swiftRealtimeService", RealtimeService.class);

        Sync() {
            scheduler.scheduleWithFixedDelay(this, 0, 5, TimeUnit.SECONDS);
        }

        @Override
        public void run() {
            try {
                for (Class<?> entity : dataMap.keySet()) {
                    initTable(entity);
                    record(entity);
                }
            } catch (Exception e) {
                SwiftLoggers.getLogger().error(e);
            }
        }

        synchronized
        private void record(final Class<?> entity) {
            final List<Object> data = dataMap.get(entity);
            if (data == null || data.isEmpty()) {
                return;
            }

            Table table = db.getTable(new SourceKey(JpaAdaptor.getTableName(entity)));
            try {
                realtimeService.insert(table.getSourceKey(), new LogRowSet(table.getMetadata(), data, entity));
                dataMap.remove(entity);
            } catch (Exception e) {
                SwiftLoggers.getLogger().error(e);
            }
        }

        synchronized
        private void stage(List<Object> data) {
            Object first = data.get(0);
            Class<?> entity = first.getClass();
            if (!dataMap.containsKey(entity)) {
                dataMap.put(entity, new ArrayList<Object>());
            }
            List<Object> curData = dataMap.get(entity);
            curData.addAll(data);

            if (curData.size() > FLUSH_SIZE_THRESHOLD) {
                record(entity);
            }
        }
    }
}
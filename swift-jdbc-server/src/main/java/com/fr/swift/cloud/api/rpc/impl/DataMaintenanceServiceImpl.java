package com.fr.swift.cloud.api.rpc.impl;

import com.fr.swift.cloud.SwiftContext;
import com.fr.swift.cloud.annotation.SwiftApi;
import com.fr.swift.cloud.api.rpc.DataMaintenanceService;
import com.fr.swift.cloud.api.rpc.SelectService;
import com.fr.swift.cloud.api.rpc.TableService;
import com.fr.swift.cloud.basics.annotation.ProxyService;
import com.fr.swift.cloud.beans.annotation.SwiftBean;
import com.fr.swift.cloud.config.entity.SwiftMetaDataEntity;
import com.fr.swift.cloud.db.SwiftDatabase;
import com.fr.swift.cloud.db.Table;
import com.fr.swift.cloud.db.Where;
import com.fr.swift.cloud.exception.meta.SwiftMetaDataException;
import com.fr.swift.cloud.result.SwiftResultSet;
import com.fr.swift.cloud.source.Row;
import com.fr.swift.cloud.source.SourceKey;
import com.fr.swift.cloud.source.SwiftMetaData;
import com.fr.swift.cloud.source.SwiftMetaDataColumn;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yee
 * @date 2018/8/23
 */
@ProxyService(value = DataMaintenanceService.class, type = ProxyService.ServiceType.EXTERNAL)
@SwiftApi
@SwiftBean
public class DataMaintenanceServiceImpl implements DataMaintenanceService {
    private TableService tableService = SwiftContext.get().getBean(TableService.class);

    @Override
    @SwiftApi
    public int insert(SwiftDatabase schema, String tableName, List<String> fields, List<Row> rows) throws SQLException {
        SwiftMetaDataEntity metaData = (SwiftMetaDataEntity) tableService.detectiveMetaData(schema, tableName);
        insert(schema, tableName, new InsertResultSet(metaData, fields, rows));
        return rows.size();
    }

    @Override
    @SwiftApi
    public int insert(SwiftDatabase schema, String tableName, List<Row> rows) throws SQLException {
        return insert(schema, tableName, null, rows);
    }

    @Override
    @SwiftApi
    public int insert(SwiftDatabase schema, String tableName, String queryJson) throws Exception {
        SwiftResultSet resultSet = SwiftContext.get().getBean(SelectService.class).query(schema, queryJson);
        return insert(schema, tableName, resultSet);
    }

    private int insert(SwiftDatabase schema, String tableName, SwiftResultSet resultSet) throws SQLException {
        SwiftMetaDataEntity metaData = (SwiftMetaDataEntity) tableService.detectiveMetaData(schema, tableName);
        SourceKey sourceKey = new SourceKey(metaData.getId());
//        try {
//            SwiftContext.get().getBean(ServiceContext.class).insert(sourceKey, resultSet);
//        } catch (SQLException e) {
//            throw e;
//        } catch (Exception e) {
//            return -1;
//        }
        return resultSet.getFetchSize();
    }

    @Override
    @SwiftApi
    public int delete(SwiftDatabase schema, String tableName, Where where) throws SQLException {
        try {
            SwiftMetaDataEntity metaData = (SwiftMetaDataEntity) tableService.detectiveMetaData(schema, tableName);
            if (null == metaData) {
                return 0;
            }
//            if (SwiftProperty.getProperty().isCluster()) {
//                ProxySelector.getInstance().getFactory().getProxy(RemoteSender.class).trigger(event);
//            } else {
//                SwiftServiceListenerManager.getInstance().triggerEvent(event);
//            }
//            SwiftContext.get().getBean(ServiceContext.class).delete(new SourceKey(tableName), where);
            return 1;
        } catch (Exception e) {
            throw new SQLException("Table which named " + tableName + " is not exists", e);
        }
    }

    @Override
    @SwiftApi(enable = false)
    public int update(SwiftDatabase schema, String tableName, SwiftResultSet resultSet, Where where) throws SQLException {

        try {
            SwiftMetaDataEntity metaData = (SwiftMetaDataEntity) tableService.detectiveMetaData(schema, tableName);
            if (null == metaData) {
                return 0;
            }
            Table table = com.fr.swift.cloud.db.impl.SwiftDatabase.getInstance().getTable(new SourceKey(metaData.getId()));
            throw new UnsupportedOperationException();
//            return table.update(where, resultSet);
        } catch (Exception e) {
            throw new SQLException("Table which named " + tableName + " is not exists", e);
        }
    }

    private class InsertResultSet implements SwiftResultSet {

        private SwiftMetaData base;
        private SwiftMetaData insertMetaData;
        private int cursor = 0;
        private List<Row> rows;

        public InsertResultSet(SwiftMetaData base, List<String> fields, List<Row> rows) throws SQLException {
            this.base = base;
            this.rows = rows;
            initInsertMetaData(fields);
        }

        private void initInsertMetaData(List<String> fields) throws SwiftMetaDataException {
            if (null == fields || fields.isEmpty()) {
                insertMetaData = base;
            } else {
                List<SwiftMetaDataColumn> columns = new ArrayList<SwiftMetaDataColumn>();
                for (String field : fields) {
                    columns.add(base.getColumn(field));
                }
                SwiftMetaDataEntity bean = (SwiftMetaDataEntity) base;
                insertMetaData = new SwiftMetaDataEntity(bean.getId(), bean.getSchemaName(), bean.getTableName(), bean.getRemark(), columns);
                ((SwiftMetaDataEntity) insertMetaData).setSwiftDatabase(bean.getSwiftDatabase());
            }
        }

        @Override
        public int getFetchSize() {
            return rows.size();
        }

        @Override
        public SwiftMetaData getMetaData() {
            return insertMetaData;
        }

        @Override
        public boolean hasNext() {
            return cursor < rows.size();
        }

        @Override
        public Row getNextRow() {
            final Row row = rows.get(cursor);
            cursor++;
            return row;
        }

        @Override
        public void close() {

        }
    }
}

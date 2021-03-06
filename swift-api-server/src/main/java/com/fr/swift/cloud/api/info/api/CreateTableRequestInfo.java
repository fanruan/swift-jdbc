package com.fr.swift.cloud.api.info.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fr.swift.cloud.api.info.ApiInvocation;
import com.fr.swift.cloud.api.info.RequestType;
import com.fr.swift.cloud.api.rpc.bean.Column;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yee
 * @date 2018-12-07
 */
public class CreateTableRequestInfo extends TableRequestInfo {
    @JsonProperty(value = "columns")
    private List<Column> columns = new ArrayList<Column>();

    public CreateTableRequestInfo() {
        super(RequestType.CREATE_TABLE);
    }


    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    @Override
    public ApiInvocation accept(ApiRequestParserVisitor visitor) {
        return visitor.visit(this);
    }
}

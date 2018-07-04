package com.fr.swift.query.info.bean.query;

import com.fr.swift.query.info.bean.element.DimensionBean;
import com.fr.swift.query.info.bean.post.PostQueryInfoBean;
import com.fr.third.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lyon on 2018/6/7.
 */
public class ResultJoinQueryInfoBean extends AbstractQueryInfoBean {

    @JsonProperty
    private List<QueryInfoBean> queryInfoBeans = new ArrayList<QueryInfoBean>(0);
    @JsonProperty
    private List<DimensionBean> joinedFields = new ArrayList<DimensionBean>(0);
    @JsonProperty
    private List<PostQueryInfoBean> postQueryInfoBeans = new ArrayList<PostQueryInfoBean>(0);

    public List<QueryInfoBean> getQueryInfoBeans() {
        return queryInfoBeans;
    }

    public void setQueryInfoBeans(List<QueryInfoBean> queryInfoBeans) {
        this.queryInfoBeans = queryInfoBeans;
    }

    public List<DimensionBean> getJoinedFields() {
        return joinedFields;
    }

    public void setJoinedFields(List<DimensionBean> joinedFields) {
        this.joinedFields = joinedFields;
    }

    public List<PostQueryInfoBean> getPostQueryInfoBeans() {
        return postQueryInfoBeans;
    }

    public void setPostQueryInfoBeans(List<PostQueryInfoBean> postQueryInfoBeans) {
        this.postQueryInfoBeans = postQueryInfoBeans;
    }
}

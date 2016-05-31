package com.fr.bi.stable.data.db;

import com.fr.bi.stable.constant.DBConstant;
import com.fr.bi.stable.data.BIFieldID;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.utils.BIDBUtils;
import com.fr.json.JSONObject;
import com.fr.stable.StringUtils;


/**
 * 数据库列转成的基础列, 表示生成cube时的字段信息
 * Created by GUY on 2015/4/10.
 */
public class BICubeFieldSource implements ICubeFieldSource {

    private int classType;
    protected int fieldType;
    protected int fieldSize;
    protected BIFieldID fieldID;
    protected String fieldName = StringUtils.EMPTY;
    protected CubeTableSource tableBelongTo;
    protected boolean usable = true;
    private boolean canSetUsable = true;


    public BICubeFieldSource(CubeTableSource tableBelongTo, String fieldName, int classType, int fieldSize) {
        this.tableBelongTo = tableBelongTo;
        this.fieldName = fieldName;
        this.fieldType = BIDBUtils.checkColumnTypeFromClass(classType);
        this.fieldSize = fieldSize;
        this.fieldID = new BIFieldID(fieldName);
        this.classType = classType;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public int getFieldType() {
        return fieldType;
    }

    public void setTableBelongTo(CubeTableSource tableBelongTo) {
        this.tableBelongTo = tableBelongTo;
    }

    public BIFieldID getFieldID() {
        return fieldID;
    }

    public CubeTableSource getTableBelongTo() {
        return tableBelongTo;
    }

    public int getFieldSize() {
        return fieldSize;
    }

    /**
     * 返回字段对应的java类
     *
     * @return
     */
    @Override
    public int getClassType() {
        return classType;
    }

    public boolean isUsable() {
        return usable;
    }

    @Override
    public boolean hasSubField() {
        return (getFieldType() == DBConstant.COLUMN.DATE);
    }

    public void setUsable(boolean usable) {
        this.usable = usable;
    }

    public void setClassType(int classType) {
        this.classType = classType;
    }

    public void setFieldType(int fieldType) {
        this.fieldType = fieldType;
    }

    public void setFieldSize(int fieldSize) {
        this.fieldSize = fieldSize;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * 转成JSON
     *
     * @param jo json对象
     * @throws Exception
     */
    @Override
    public void parseJSON(JSONObject jo) throws Exception {
        if (jo.has("field_name")) {
            this.setFieldName(jo.getString("field_name"));
        }
        if (jo.has("field_id")) {
            String fieldId = jo.getString("field_id");
            this.fieldID = new BIFieldID(fieldId);
        }
        if (jo.has("field_type")) {
            fieldType = jo.optInt("field_type", 0);
        }
        if (jo.has("field_size")) {
            fieldSize = jo.optInt("field_size", 0);
        }
        if (jo.has("is_usable")) {
            usable = jo.optBoolean("is_usable", true);
        }

        if (jo.has("is_enable")) {
            canSetUsable = jo.optBoolean("is_enable", true);
        }
        if (jo.has("class_type")) {
            classType = jo.getInt("class_type");
        }
    }

    /**
     * 创建JSON
     *
     * @return JSON对象
     * @throws Exception
     */
    @Override
    public JSONObject createJSON() throws Exception {
        JSONObject jo = new JSONObject();
        jo.put("field_name", getFieldName());
        jo.put("id", fieldID.getIdentityValue());
        jo.put("table_id", getTableBelongTo().getSourceID());
        jo.put("field_type", fieldType)
                .put("field_size", fieldSize)
                .put("is_usable", isUsable())
                .put("is_enable", canSetUsable)
                .put("class_type", classType);

        return jo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BICubeFieldSource that = (BICubeFieldSource) o;

        if (fieldName != null ? !fieldName.equals(that.fieldName) : that.fieldName != null) return false;
        return tableBelongTo != null ? tableBelongTo.equals(that.tableBelongTo) : that.tableBelongTo == null;

    }

    @Override
    public int hashCode() {
        int result = fieldName != null ? fieldName.hashCode() : 0;
        result = 31 * result + (tableBelongTo != null ? tableBelongTo.hashCode() : 0);
        return result;
    }
}
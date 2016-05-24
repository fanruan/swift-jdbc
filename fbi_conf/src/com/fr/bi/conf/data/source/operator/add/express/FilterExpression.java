/**
 * 
 */
package com.fr.bi.conf.data.source.operator.add.express;

import com.finebi.cube.api.ICubeTableService;
import com.fr.bi.base.BICore;
import com.fr.bi.base.key.BIKey;
import com.fr.bi.conf.report.widget.field.filtervalue.FilterValue;
import com.fr.bi.stable.constant.DBConstant;
import com.fr.bi.stable.engine.index.key.IndexKey;
import com.fr.json.JSONObject;
import com.fr.json.JSONTransform;

/**
 * @author Daniel
 *
 */
public class FilterExpression<T> implements Expression {
	
	private FilterValue filter;
	private transient BIKey key;
	private Field field;
	private Object value;
	private int field_type;

	@Override
	public void parseJSON(JSONObject jo) throws Exception {
		field = new Field();
		field.parseJSON(jo.getJSONObject("field"));
		field_type = jo.getInt("field_type");
		filter = ExpressionFilterValueFactory.createRowValue(jo, field.fieldType);
		value = jo.get("value");
	}

	private BIKey getKey() {
		if(key == null){
			key = new IndexKey(field.value);
		}
		return key;
	}

	@Override
	public JSONObject createJSON() throws Exception {
		JSONObject jo = new JSONObject();
		jo.put("value", value);
		jo.put("field", field.createJSON());
		jo.put("field_type", field_type);
		jo.join(filter.createJSON());
		return jo;
	}

	@Override
	public BICore fetchObjectCore() {
		return null;
	}

	@Override
	public Object get(ICubeTableService ti, int row) {
		Object v = ti.getRow(getKey(), row);
		if(filter.isMatchValue((T) v)){
			return value;
		}
		return null;
	}


	private class Field implements JSONTransform{

		private String value;
		private int fieldType = DBConstant.COLUMN.STRING;

		@Override
		public JSONObject createJSON() throws Exception {
			JSONObject jo = new JSONObject();
			jo.put("fieldType", fieldType);
			jo.put("text", value);
			jo.put("value", value);
			return jo;
		}

		@Override
		public void parseJSON(JSONObject jo) throws Exception {
			if(jo.has("fieldType")){
				fieldType = jo.getInt("fieldType");
			}
			if(jo.has("value")){
				this.value = jo.getString("value");
			}
		}
	}

}
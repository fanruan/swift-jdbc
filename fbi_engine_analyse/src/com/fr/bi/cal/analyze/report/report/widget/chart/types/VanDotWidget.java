package com.fr.bi.cal.analyze.report.report.widget.chart.types;

import com.fr.general.FRLogger;
import com.fr.json.JSONArray;
import com.fr.json.JSONException;
import com.fr.json.JSONObject;
import com.fr.stable.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by eason on 2017/2/27.
 */
public class VanDotWidget extends VanCartesianWidget{

    private static final int BUBBLE = 1;
    private static final int SCATTER = 2;

    private static final String TARGET = "50000";

    //显示颜色的规则
    private static final int SERIES_RULE = 1;
    private static final int INTERVAL_RULE = 2;
    private static final int GRADUAL_RULE = 3;

    //气泡图和散点图的指标个数
    private static final int BUBBLE_COUNT = 3;
    private static final int SCATTER_COUNT = 2;

    private static final int NO_SHADOW =  16;  //没有投影的气泡
    private static final int SHADOW =  17;       //有投影的气泡

    //点的样式
    private static final int SQUARE = 1;
    private static final int TRIANGLE = 2;

    //气泡的大小
    private static final int MIN_SIZE = 15;
    private static final int MAX_SIZE = 80;

    //值区间的默认颜色
    private static final String[] INTERVAL_COLORS = new String[]{"#65B3EE", "#95E1AA", "#F8D08E"};
    private static final int INTERVAL = 100;

    private static final int BUBBLE_DIMENSION = 3;

    protected JSONObject populateDefaultSettings() throws JSONException {
        JSONObject settings = super.populateDefaultSettings();

        settings.put("displayRules", SERIES_RULE);
        settings.put("bubbleStyle", NO_SHADOW);
        settings.put("dotStyle", SQUARE);

        settings.put("bubbleSizeFrom", MIN_SIZE);
        settings.put("bubbleSizeTo", MAX_SIZE);

        JSONArray fixedStyle = JSONArray.create();
        for(int i = 0, len = INTERVAL_COLORS.length; i < len; i++){
            JSONObject range = JSONObject.create();
            range.put("min", i * INTERVAL).put("max", (i + 1) * INTERVAL);
            fixedStyle.put(JSONObject.create().put("color", INTERVAL_COLORS[i]).put("range", range));
        }

        JSONObject gradualStyle = JSONObject.create();
        gradualStyle.put("range", JSONObject.create().put("min", 0).put("max", 100));
        gradualStyle.put("color_range", JSONObject.create().put("from_color", "#65B3EE").put("to_color", "#95E1AA"));

        settings.put("fixedStyle", fixedStyle);
        settings.put("gradientStyle", JSONArray.create().put(gradualStyle));

        return settings;
    }

    public JSONArray createSeries(JSONObject originData) throws Exception{

        JSONArray series = JSONArray.create();
        String[] ids = this.getUsedTargetID();

        if(ids.length < SCATTER_COUNT){
            return series;
        }

        String seriesType = this.getSeriesType(StringUtils.EMPTY);

        HashMap<String, ArrayList<JSONArray>> seriesMap = new HashMap<String, ArrayList<JSONArray>>();
        Iterator iterator = originData.keys();
        while (iterator.hasNext()) {
            String key = iterator.next().toString();
            JSONArray children = originData.optJSONObject(key).optJSONArray("c");
            for(int i = 0, len = children.length(); i < len; i++){
                JSONObject obj = children.getJSONObject(i);
                String seriesName = obj.optString("n");
                ArrayList<JSONArray> seriesArray = seriesMap.containsKey(seriesName) ? seriesMap.get(seriesName) : new ArrayList<JSONArray>();
                seriesMap.put(seriesName, seriesArray);
                seriesArray.add(obj.optJSONArray("c"));
            }
        }

        iterator = seriesMap.keySet().iterator();
        while (iterator.hasNext()){
            String seriesName = iterator.next().toString();
            ArrayList<JSONArray> seriesArray = seriesMap.get(seriesName);
            JSONObject ser = JSONObject.create();
            JSONArray data = JSONArray.create();
            for(int i = 0, size = seriesArray.size(); i < size; i++){
                JSONArray dataArray = seriesArray.get(i);
                for(int j = 0, count = dataArray.length(); j < count; j++){
                    JSONObject obj = dataArray.optJSONObject(j);
                    JSONArray dimensions = obj.optJSONArray("s");

                    double x = dimensions.optDouble(0);
                    double y = dimensions.optDouble(1);
                    double value = dimensions.length() > 2 ? dimensions.optDouble(2) : 0;

                    data.put(JSONObject.create().put("x", x).put("y", y).put("size", value));
                }
            }

            ser.put("data", data).put("name", seriesName).put("dimensionID", ids[ids.length - 1]);
            series.put(ser);
        }
        return series;
    }

    protected JSONArray parseCategoryAxis(JSONObject settings) throws JSONException{

        JSONObject baseAxis = this.parseRightValueAxis(settings).put("position", "bottom").put("type", "value");

        return JSONArray.create().put(baseAxis);
    }

    protected JSONArray parseValueAxis(JSONObject settings) throws JSONException{

        return JSONArray.create().put(this.parseLeftValueAxis(settings));
    }

    public String getSeriesType(String dimensionID){

        JSONObject scopes = this.getChartSetting().getScopes();

        int type = SCATTER;
        try {
            if(scopes.has(TARGET)){
                type = scopes.getJSONObject(TARGET).optInt("valueType", BUBBLE);
            }
        }catch (Exception e){
            FRLogger.getLogger().error(e.getMessage(), e);
        }

        int idCount = this.getUsedTargetID().length;

        return (idCount == BUBBLE_DIMENSION && type == BUBBLE ) ? "bubble" : "scatter";
    }
}

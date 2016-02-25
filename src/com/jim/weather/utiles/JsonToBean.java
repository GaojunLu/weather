package com.jim.weather.utiles;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.jim.weather.bean.WeatherInfo;

public class JsonToBean {
	public static WeatherInfo getWeatherInfo(String json){
		JSONObject jsonObject;
		JSONArray jsonArray;
		WeatherInfo weatherInfo = null;
		try {
			jsonObject = new JSONObject(json);
			jsonArray = jsonObject.getJSONArray("HeWeather data service 3.0");
			Gson gson = new Gson();
			weatherInfo = gson.fromJson(jsonArray.getJSONObject(0).toString(), WeatherInfo.class);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			jsonObject = null;
			jsonArray = null;
		}
		return weatherInfo;
	}
}

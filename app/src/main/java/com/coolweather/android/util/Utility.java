package com.coolweather.android.util;

import android.text.TextUtils;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Response;

/**
 * Created by 97475 on 2017/10/27.
 */

public class Utility {

    public static boolean handlerProvinceResponse(String response) {
        try {
            if (!TextUtils.isEmpty(response)) {
                JSONArray array = new JSONArray(response);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    String name = object.getString("name");
                    int code = object.getInt("id");
                    Province province = new Province();
                    province.setProvinceName(name);
                    province.setProvinceCode(code);
                    province.save();
                }
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean handlerCityResponse(String response, int provinceId) {
        try {
            if (!TextUtils.isEmpty(response)) {
                JSONArray array = new JSONArray(response);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    String name = object.getString("name");
                    int code = object.getInt("id");
                    City city = new City();
                    city.setCityName(name);
                    city.setCityCode(code);
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean handlerCountyResponse(String response, int cityId) {
        try {
            if (!TextUtils.isEmpty(response)) {
                JSONArray array = new JSONArray(response);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    String name = object.getString("name");
                    String weatherId = object.getString("weather_id");
                    County county = new County();
                    county.setCountyName(name);
                    county.setWeatherId(weatherId);
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}

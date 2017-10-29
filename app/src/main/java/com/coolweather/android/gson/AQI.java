package com.coolweather.android.gson;


/**
 * Created by 97475 on 2017/10/29.
 */

public class AQI {

    public AQICity city;

    public class AQICity {
        public String aqi;
        public String pm25;
    }
}

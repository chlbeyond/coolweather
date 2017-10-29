package com.coolweather.android;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by 97475 on 2017/10/28.
 */

public class ChooseAreaFragment extends Fragment {
    private static final String url = "http://guolin.tech/api/china/";
    private static final int LEVEL_PROVINCE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_COUNTY = 2;
    private int currentLevel;

    private TextView titleText;
    private ImageView imageView;
    private ListView listView;
    private List<String> dataList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private List<Province> provincesList;
    private List<City> citiesList;
    private List<County> countiesList;

    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;

    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        imageView = (ImageView) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        queryProvinces();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provincesList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = citiesList.get(position);
                    queryCounty();
                } else if (currentLevel == LEVEL_COUNTY) {
                    selectedCounty = countiesList.get(position);
                    String weatherId = selectedCounty.getWeatherId();
                    if (getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        getActivity().startActivity(intent);
                        getActivity().finish();
                    } else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity weatherActivity = (WeatherActivity) getActivity();
                        weatherActivity.drawerLayout.closeDrawers();
                        weatherActivity.refreshLayout.setRefreshing(true);
                        weatherActivity.requestWeather(weatherId);
                    }
                }
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                } else if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                }
            }
        });
    }

    private void queryProvinces() {
        titleText.setText("中国");
        imageView.setVisibility(View.GONE);
        provincesList = DataSupport.findAll(Province.class);
        if (!provincesList.isEmpty()) { //provincesList.size() > 0
            dataList.clear();
            for (Province province : provincesList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer(url, "province");
        }
    }

    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        imageView.setVisibility(View.VISIBLE);
        citiesList = DataSupport.where("provinceId = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (!citiesList.isEmpty()) { //provincesList.size() > 0
            dataList.clear();
            for (City city : citiesList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = url + provinceCode;
            queryFromServer(address, "city");
        }
    }

    private void queryCounty() {
        titleText.setText(selectedCity.getCityName());
        imageView.setVisibility(View.VISIBLE);
        countiesList = DataSupport.where("cityId = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (!countiesList.isEmpty()) { //provincesList.size() > 0
            dataList.clear();
            for (County county : countiesList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceId = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = url + provinceId + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sentOKHttpRequest(address, new Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                Boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handlerProvinceResponse(body);
                } else if ("city".equals(type)) {
                    int provinceId = selectedProvince.getId();
                    result = Utility.handlerCityResponse(body,provinceId);
                } else {
                    int cityId = selectedCity.getId();
                    result = Utility.handlerCountyResponse(body,cityId);
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else {
                                queryCounty();
                            }
                        }
                    });
                }
                closeProgressDialog();
            }

            @Override
            public void onFailure(Call call, IOException e) {
                closeProgressDialog();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("加载中...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}

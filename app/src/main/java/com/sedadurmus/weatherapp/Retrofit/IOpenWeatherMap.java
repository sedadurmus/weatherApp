package com.sedadurmus.weatherapp.Retrofit;

import com.sedadurmus.weatherapp.model.WeatherForecastResult;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IOpenWeatherMap {

//    @GET("weather")
//    Observable<WeatherResult> getWeatherByLatLng (@Query("lat") String lat,
//                                                                  @Query("lon") String lng,
//                                                                  @Query("appid") String appid,
//                                                                  @Query("units") String unit);
    @GET("weather")
    Observable<WeatherForecastResult> getWeatherByCityName (@Query("q") String cityName,
                                                    @Query("appid") String appid,
                                                    @Query("units") String unit);

    @GET("forecast")
    Observable<WeatherForecastResult> getForecastWeatherByLatLng (@Query("q") String cityName,
                                                                  @Query("appid") String appid,
                                                                  @Query("units") String unit);
}

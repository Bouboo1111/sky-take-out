package com.sky.utils;

import com.alibaba.fastjson.JSON;
import com.sky.dto.BaiduGeocodingResult;
import com.sky.dto.BaiduDirectionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class BaiduMapUtil {

    @Value("${sky.baidu.ak}")
    private String ak;

    private static final String GEOCODING_URL = "https://api.map.baidu.com/geocoding/v3/";
    private static final String DIRECTION_URL = "https://api.map.baidu.com/directionlite/v1/driving";

    /**
     * 地理编码：将地址转换为经纬度
     * @param address 地址
     * @return 经纬度
     */
    public BaiduGeocodingResult.Result geocode(String address) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = GEOCODING_URL + "?address=" + address + "&ak=" + ak;
            String response = restTemplate.getForObject(url, String.class);
            log.info("百度地图地理编码响应：{}", response);

            BaiduGeocodingResult result = JSON.parseObject(response, BaiduGeocodingResult.class);
            if (result.getStatus() == 0) {
                return result.getResult();
            } else {
                log.error("地理编码失败：{}", result.getMessage());
                return null;
            }
        } catch (Exception e) {
            log.error("地理编码异常", e);
            return null;
        }
    }

    /**
     * 计算两点之间的驾车距离
     * @param originLat 起点纬度
     * @param originLng 起点经度
     * @param destLat 终点纬度
     * @param destLng 终点经度
     * @return 距离（米）
     */
    public Integer getDistance(Double originLat, Double originLng, Double destLat, Double destLng) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = DIRECTION_URL + "?origin=" + originLat + "," + originLng
                    + "&destination=" + destLat + "," + destLng + "&ak=" + ak;
            String response = restTemplate.getForObject(url, String.class);
            log.info("百度地图路线规划响应：{}", response);

            BaiduDirectionResult result = JSON.parseObject(response, BaiduDirectionResult.class);
            if (result.getStatus() == 0) {
                return result.getResult().getRoutes().getDistance().getValue();
            } else {
                log.error("路线规划失败：{}", result.getMessage());
                return null;
            }
        } catch (Exception e) {
            log.error("路线规划异常", e);
            return null;
        }
    }
}

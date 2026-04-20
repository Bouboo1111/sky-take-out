package com.sky.dto;

import lombok.Data;

@Data
public class BaiduGeocodingResult {
    private Integer status;
    private String message;
    private Result result;

    @Data
    public static class Result {
        private Location location;
        private String precise;
        private String confidence;
        private String comprehension;
        private String level;

        @Data
        public static class Location {
            private Double lat;
            private Double lng;
        }
    }
}

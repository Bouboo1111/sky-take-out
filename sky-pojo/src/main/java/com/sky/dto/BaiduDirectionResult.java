
package com.sky.dto;

import lombok.Data;

@Data
public class BaiduDirectionResult {
    private Integer status;
    private String message;
    private Result result;
    
    @Data
    public static class Result {
        private Routes routes;
        
        @Data
        public static class Routes {
            private Distance distance;
            
            @Data
            public static class Distance {
                private Integer value;
                private String text;
            }
        }
    }
}

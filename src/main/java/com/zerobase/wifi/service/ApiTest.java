package com.zerobase.wifi.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ApiTest {
    public static void main(String[] args) {
        // 테스트할 좌표값 설정 (서울시청 좌표)
        double testLat = 37.561924;
        double testLnt = 126.96675;
        
        try {
            // 1. API 키와 URL 설정
            String API_KEY = "74785668416d61643133314a584f6568";
            
            // 1000개의 데이터를 가져오도록 설정
            String url = String.format(
                "http://openapi.seoul.go.kr:8088/%s/json/TbPublicWifiInfo/1/1000",
                API_KEY
            );
            
            System.out.println("Testing API URL: " + url);
            
            // 2. HTTP 클라이언트 설정
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            
            // 3. API 호출 및 응답 확인
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new Exception("API call failed: " + response);
                }
                
                String responseBody = response.body().string();
                System.out.println("\nAPI Response received. First 1000 characters:");
                System.out.println(responseBody.substring(0, Math.min(responseBody.length(), 1000)));
                
                // 4. JSON 파싱 테스트
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
                
                // 5. 데이터 개수 확인
                int totalCount = jsonObject.getAsJsonObject("TbPublicWifiInfo")
                                        .get("list_total_count")
                                        .getAsInt();
                
                System.out.println("\nTotal WiFi locations available: " + totalCount);
                
                // 6. 응답 코드 확인
                String resultCode = jsonObject.getAsJsonObject("TbPublicWifiInfo")
                                           .getAsJsonObject("RESULT")
                                           .get("CODE")
                                           .getAsString();
                
                String resultMessage = jsonObject.getAsJsonObject("TbPublicWifiInfo")
                                               .getAsJsonObject("RESULT")
                                               .get("MESSAGE")
                                               .getAsString();
                
                System.out.println("Result Code: " + resultCode);
                System.out.println("Result Message: " + resultMessage);
                
                // 7. 첫 번째 데이터 출력
                JsonObject firstWifi = jsonObject.getAsJsonObject("TbPublicWifiInfo")
                                               .getAsJsonArray("row")
                                               .get(0)
                                               .getAsJsonObject();
                
                System.out.println("\nFirst WiFi location details:");
                System.out.println("관리번호: " + firstWifi.get("X_SWIFI_MGR_NO").getAsString());
                System.out.println("와이파이명: " + firstWifi.get("X_SWIFI_MAIN_NM").getAsString());
                System.out.println("주소: " + firstWifi.get("X_SWIFI_ADRES1").getAsString());
                System.out.println("위도: " + firstWifi.get("LAT").getAsString());
                System.out.println("경도: " + firstWifi.get("LNT").getAsString());
                System.out.println("작업일자: " + firstWifi.get("WORK_DTTM").getAsString());
            }
            
        } catch (Exception e) {
            System.out.println("Error occurred:");
            e.printStackTrace();
        }
    }
}
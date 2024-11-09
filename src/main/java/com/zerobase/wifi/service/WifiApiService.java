package com.zerobase.wifi.service;

import java.io.IOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WifiApiService {
    private static final String API_KEY = "74785668416d61643133314a584f6568"; // 실제 API 키로 변경
    private static final String API_URL = "http://openapi.seoul.go.kr:8088/" + API_KEY + "/json/TbPublicWifiInfo/1/1000/";
    
    private OkHttpClient client;

    public WifiApiService() {
        this.client = new OkHttpClient();
    }

    public JsonObject getWifiData(int start, int end) throws IOException {
        String url = API_URL.replace("1/1000", start + "/" + end);
        
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API call failed: " + response);
            }

            String jsonData = response.body().string();
            return JsonParser.parseString(jsonData).getAsJsonObject();
        }
    }

    public int getTotalCount() throws IOException {
        JsonObject response = getWifiData(1, 1);
        return response.getAsJsonObject("TbPublicWifiInfo")
                      .get("list_total_count")
                      .getAsInt();
    }
}
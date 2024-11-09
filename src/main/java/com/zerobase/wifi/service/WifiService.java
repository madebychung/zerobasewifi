package com.zerobase.wifi.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.zerobase.wifi.dto.WifiInfo;
import com.zerobase.wifi.dto.History;
import com.zerobase.wifi.util.DatabaseUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.sql.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class WifiService {
    private static final String API_KEY = "74785668416d61643133314a584f6568";
    
    private final OkHttpClient client = new OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    // 와이파이 데이터 로드
    public int loadWifiData() throws IOException {
        log.info("Starting WiFi data load process");
        int totalCount = 0;

        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM WIFI");
            }

            String url = String.format("http://openapi.seoul.go.kr:8088/%s/json/TbPublicWifiInfo/1/1000", API_KEY);

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("API call failed: " + response);
                }

                String responseBody = response.body().string();
                JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);
                JsonArray wifiArray = jsonObject.getAsJsonObject("TbPublicWifiInfo")
                                             .getAsJsonArray("row");

                String insertSql = """
                    INSERT INTO WIFI (
                        X_SWIFI_MGR_NO, X_SWIFI_MAIN_NM, X_SWIFI_ADRES1, 
                        LAT, LNT, WORK_DTTM
                    ) VALUES (?, ?, ?, ?, ?, ?)
                """;

                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    for (int i = 0; i < wifiArray.size(); i++) {
                        JsonObject wifi = wifiArray.get(i).getAsJsonObject();
                        
                        pstmt.setString(1, wifi.get("X_SWIFI_MGR_NO").getAsString());
                        pstmt.setString(2, wifi.get("X_SWIFI_MAIN_NM").getAsString());
                        pstmt.setString(3, wifi.get("X_SWIFI_ADRES1").getAsString());
                        pstmt.setDouble(4, wifi.get("LAT").getAsDouble());
                        pstmt.setDouble(5, wifi.get("LNT").getAsDouble());
                        pstmt.setString(6, wifi.get("WORK_DTTM").getAsString());
                        
                        pstmt.addBatch();
                        totalCount++;
                        
                        if (i % 100 == 0) {
                            pstmt.executeBatch();
                            conn.commit();
                        }
                    }
                    
                    pstmt.executeBatch();
                    conn.commit();
                }
            }
            
            log.info("Successfully loaded {} WiFi locations", totalCount);
            return totalCount;
            
        } catch (Exception e) {
            log.error("Failed to load WiFi data", e);
            throw new RuntimeException("Failed to load WiFi data", e);
        }
    }

    // 근처 와이파이 정보 조회 + 히스토리 저장
    public List<WifiInfo> getNearbyWifi(double lat, double lnt) {
        // 먼저 히스토리 저장
        insertHistory(lat, lnt);
        log.info("Searching for WiFi spots near lat: {}, lnt: {}", lat, lnt);
        
        String sql = """
            SELECT *,
            (6371 * acos(cos(radians(?)) * cos(radians(LAT)) * cos(radians(LNT) - radians(?)) + sin(radians(?)) * sin(radians(LAT)))) AS distance
            FROM WIFI 
            HAVING distance <= 3
            ORDER BY distance
            LIMIT 20
        """;
        
        List<WifiInfo> wifiList = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, lat);
            pstmt.setDouble(2, lnt);
            pstmt.setDouble(3, lat);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    WifiInfo wifi = new WifiInfo();
                    wifi.setDistance(rs.getDouble("distance"));
                    wifi.setMgrNo(rs.getString("X_SWIFI_MGR_NO"));
                    wifi.setMainNm(rs.getString("X_SWIFI_MAIN_NM"));
                    wifi.setAddress(rs.getString("X_SWIFI_ADRES1"));
                    wifi.setLat(rs.getDouble("LAT"));
                    wifi.setLnt(rs.getDouble("LNT"));
                    wifi.setWorkDttm(rs.getString("WORK_DTTM"));
                    wifiList.add(wifi);
                }
            }
            
            log.info("Found {} nearby WiFi spots", wifiList.size());
            return wifiList;
            
        } catch (SQLException e) {
            log.error("Error getting nearby WiFi", e);
            throw new RuntimeException("와이파이 정보 조회 실패", e);
        }
    }

    // 히스토리 저장
    public void insertHistory(double lat, double lnt) {
        log.info("Saving location history - lat: {}, lnt: {}", lat, lnt);
        String sql = "INSERT INTO LOCATION_HISTORY (LAT, LNT, SEARCH_DTTM) VALUES (?, ?, NOW())";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, lat);
            pstmt.setDouble(2, lnt);
            
            int result = pstmt.executeUpdate();
            if (result > 0) {
                log.info("Successfully saved location history");
            }
            
        } catch (SQLException e) {
            log.error("Error inserting history", e);
            throw new RuntimeException("히스토리 저장 실패", e);
        }
    }

    // 히스토리 목록 조회
 // WifiService.java의 getHistoryList 메소드에 쿼리 로깅 추가
    public List<History> getHistoryList() {
        log.info("Retrieving location history list");
        List<History> historyList = new ArrayList<>();
        String sql = "SELECT * FROM LOCATION_HISTORY ORDER BY ID DESC";
        log.info("Executing SQL: {}", sql);  // 실행되는 SQL 출력

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                History history = new History();
                history.setId(rs.getInt("ID"));
                history.setLat(rs.getDouble("LAT"));
                history.setLnt(rs.getDouble("LNT"));
                
                Timestamp timestamp = rs.getTimestamp("SEARCH_DTTM");
                if (timestamp != null) {
                    history.setSearchDttm(timestamp.toString());
                }
                
                historyList.add(history);
                log.info("Added history record: id={}, lat={}, lnt={}, searchDttm={}", 
                         history.getId(), history.getLat(), history.getLnt(), history.getSearchDttm());
            }
            
            log.info("Retrieved {} history records", historyList.size());
            return historyList;

        } catch (SQLException e) {
            log.error("Error retrieving history list: {}", e.getMessage(), e);
            throw new RuntimeException("히스토리 조회 실패", e);
        }
    }

    // 히스토리 삭제
    public int deleteHistory(int id) {
        log.info("Deleting history entry with ID: {}", id);
        String sql = "DELETE FROM LOCATION_HISTORY WHERE ID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int result = pstmt.executeUpdate();
            
            log.info("Successfully deleted history with ID {}", id);
            return result;
            
        } catch (SQLException e) {
            log.error("Failed to delete history with ID: {}", id, e);
            throw new RuntimeException("히스토리 삭제 실패", e);
        }
    }
}
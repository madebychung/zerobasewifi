package com.zerobase.wifi.dao;

import com.zerobase.wifi.dto.WifiInfo;
import com.zerobase.wifi.util.DatabaseUtil;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class WifiDAO {
    
    public List<WifiInfo> getNearbyWifi(double lat, double lnt) {
        String sql = """
            SELECT *,
            ST_Distance_Sphere(POINT(?, ?), POINT(LNT, LAT)) AS distance
            FROM WIFI_INFO
            ORDER BY distance
            LIMIT 20
        """;
        
        List<WifiInfo> wifiList = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, lnt);
            pstmt.setDouble(2, lat);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    WifiInfo wifi = new WifiInfo();
                    wifi.setDistance(rs.getDouble("distance") / 1000.0); // 미터를 킬로미터로 변환
                    wifi.setMgrNo(rs.getString("X_SWIFI_MGR_NO"));
                    wifi.setMainNm(rs.getString("X_SWIFI_MAIN_NM"));
                    wifi.setAddress(rs.getString("X_SWIFI_ADRES1"));
                    wifi.setLat(rs.getDouble("LAT"));
                    wifi.setLnt(rs.getDouble("LNT"));
                    wifi.setWorkDttm(rs.getString("WORK_DTTM"));
                    wifiList.add(wifi);
                }
            }
            
        } catch (SQLException e) {
            log.error("Error getting nearby WiFi", e);
            throw new RuntimeException("와이파이 정보 조회 실패", e);
        }
        
        return wifiList;
    }
}
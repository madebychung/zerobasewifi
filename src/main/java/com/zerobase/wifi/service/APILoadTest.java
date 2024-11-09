package com.zerobase.wifi.service;

import com.zerobase.wifi.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class APILoadTest {
    public static void main(String[] args) {
        try {
            System.out.println("=== WIFI 데이터 로드 테스트 시작 ===");
            
            // 1. 기존 데이터 확인
            Connection conn = DatabaseUtil.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM WIFI");
            rs.next();
            System.out.println("로드 전 WIFI 데이터 수: " + rs.getInt("count"));
            
            // 2. WifiService 인스턴스 생성
            WifiService wifiService = new WifiService();
            
            // 3. 데이터 로드 실행
            System.out.println("\n데이터 로드 시작...");
            int loadedCount = wifiService.loadWifiData();
            System.out.println("데이터 로드 완료: " + loadedCount + "개 처리됨");
            
            // 4. 로드 후 데이터 확인
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM WIFI");
            rs.next();
            System.out.println("\n로드 후 WIFI 데이터 수: " + rs.getInt("count"));
            
            // 5. 샘플 데이터 확인
            rs = stmt.executeQuery("SELECT * FROM WIFI LIMIT 1");
            if (rs.next()) {
                System.out.println("\n=== 샘플 데이터 ===");
                System.out.println("관리번호: " + rs.getString("X_SWIFI_MGR_NO"));
                System.out.println("와이파이명: " + rs.getString("X_SWIFI_MAIN_NM"));
                System.out.println("주소: " + rs.getString("X_SWIFI_ADRES1"));
                System.out.println("위도: " + rs.getDouble("LAT"));
                System.out.println("경도: " + rs.getDouble("LNT"));
                System.out.println("작업일자: " + rs.getString("WORK_DTTM"));
            }
            
            conn.close();
            System.out.println("\n테스트 완료!");
            
        } catch (Exception e) {
            System.out.println("\n!!! 테스트 중 오류 발생 !!!");
            e.printStackTrace();
        }
    }
}
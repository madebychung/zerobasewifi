package com.zerobase.wifi.service;

import com.zerobase.wifi.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TableTest {
    public static void main(String[] args) {
        try {
            Connection conn = DatabaseUtil.getConnection();
            Statement stmt = conn.createStatement();
            
            // 1. 테이블 존재 여부 확인
            ResultSet rs = stmt.executeQuery("""
                SELECT TABLE_NAME 
                FROM INFORMATION_SCHEMA.TABLES 
                WHERE TABLE_SCHEMA = 'wifi_db' 
                AND TABLE_NAME IN ('WIFI', 'HISTORY')
            """);
            
            System.out.println("=== 테이블 존재 여부 확인 ===");
            while (rs.next()) {
                System.out.println("테이블 발견: " + rs.getString("TABLE_NAME"));
            }
            
            // 2. WIFI 테이블 구조 확인
            rs = stmt.executeQuery("DESCRIBE WIFI");
            System.out.println("\n=== WIFI 테이블 구조 ===");
            while (rs.next()) {
                System.out.printf("필드: %-20s 타입: %-15s\n", 
                    rs.getString("Field"), 
                    rs.getString("Type"));
            }
            
            // 3. HISTORY 테이블 구조 확인
            rs = stmt.executeQuery("DESCRIBE HISTORY");
            System.out.println("\n=== HISTORY 테이블 구조 ===");
            while (rs.next()) {
                System.out.printf("필드: %-20s 타입: %-15s\n", 
                    rs.getString("Field"), 
                    rs.getString("Type"));
            }
            
            // 4. 데이터 존재 여부 확인
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM WIFI");
            rs.next();
            System.out.println("\n=== 데이터 수 확인 ===");
            System.out.println("WIFI 테이블 레코드 수: " + rs.getInt("count"));
            
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM HISTORY");
            rs.next();
            System.out.println("HISTORY 테이블 레코드 수: " + rs.getInt("count"));
            
            conn.close();
            System.out.println("\n테이블 테스트 완료!");
            
        } catch (Exception e) {
            System.out.println("테이블 테스트 중 오류 발생:");
            e.printStackTrace();
        }
    }
}
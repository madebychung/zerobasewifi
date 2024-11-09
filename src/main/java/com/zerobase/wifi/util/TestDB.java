package com.zerobase.wifi.util;

import com.zerobase.wifi.util.DatabaseUtil;
import java.sql.Connection;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection conn = DatabaseUtil.getConnection();
            if (conn != null) {
                System.out.println("연결 성공!");
                conn.close();
            }
        } catch (Exception e) {
            System.out.println("연결 실패!");
            e.printStackTrace();
        }
    }
}
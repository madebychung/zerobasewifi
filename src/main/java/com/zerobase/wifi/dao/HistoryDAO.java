package com.zerobase.wifi.dao;

import com.zerobase.wifi.dto.History;
import com.zerobase.wifi.util.DatabaseUtil;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class HistoryDAO {
    
    public List<History> getHistoryList() {
        String sql = "SELECT * FROM LOCATION_HISTORY ORDER BY ID DESC";
        List<History> historyList = new ArrayList<>();
        
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
            }
            
        } catch (SQLException e) {
            log.error("Error getting history list", e);
            throw new RuntimeException("히스토리 조회 실패", e);
        }
        
        return historyList;
    }
    
    public void insertHistory(double lat, double lnt) {
        String sql = "INSERT INTO LOCATION_HISTORY (LAT, LNT, SEARCH_DTTM) VALUES (?, ?, NOW())";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, lat);
            pstmt.setDouble(2, lnt);
            
            int result = pstmt.executeUpdate();
            if (result > 0) {
                log.info("History saved successfully");
            }
            
        } catch (SQLException e) {
            log.error("Error inserting history", e);
            throw new RuntimeException("히스토리 저장 실패", e);
        }
    }
    
    public void deleteHistory(int id) {
        String sql = "DELETE FROM LOCATION_HISTORY WHERE ID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int result = pstmt.executeUpdate();
            
            if (result == 0) {
                throw new RuntimeException("삭제할 히스토리가 존재하지 않습니다.");
            }
            
            log.info("Successfully deleted history with id: {}", id);
            
        } catch (SQLException e) {
            log.error("Error deleting history", e);
            throw new RuntimeException("히스토리 삭제 실패", e);
        }
    }
}
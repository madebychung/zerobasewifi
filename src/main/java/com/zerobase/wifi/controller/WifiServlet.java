package com.zerobase.wifi.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.zerobase.wifi.service.WifiService;
import com.zerobase.wifi.dto.WifiInfo;
import com.zerobase.wifi.dto.History;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;

@WebServlet("/wifi-project/wifi/*")
public class WifiServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(WifiServlet.class);
    private final WifiService wifiService;
    private final Gson gson;

    public WifiServlet() {
        this.wifiService = new WifiService();
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                @Override
                public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(src.toString());
                }
            })
            .create();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        logger.info("Received GET request with pathInfo: {}", pathInfo);
        
        try {
            if ("/nearby".equals(pathInfo)) {
                getNearbyWifi(request, response);
            } else if ("/history".equals(pathInfo)) {
                getHistory(request, response);
            } else if ("/load-wifi".equals(pathInfo)) {
                loadWifiData(request, response);
            } else {
                logger.warn("Invalid pathInfo: {}", pathInfo);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Error processing request", e);
            sendErrorResponse(response, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        logger.info("Received POST request with pathInfo: {}", pathInfo);

        try {
            if ("/history".equals(pathInfo)) {
                saveHistory(request, response);
            } else {
                logger.warn("Invalid pathInfo for POST: {}", pathInfo);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Error processing POST request", e);
            sendErrorResponse(response, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        logger.info("Received DELETE request with pathInfo: {}", pathInfo);

        try {
            if ("/history".equals(pathInfo)) {
                String idStr = request.getParameter("id");
                if (idStr != null && !idStr.isEmpty()) {
                    int id = Integer.parseInt(idStr);
                    wifiService.deleteHistory(id);
                    sendJsonResponse(response, "{\"result\":\"success\"}");
                } else {
                    throw new IllegalArgumentException("히스토리 ID가 필요합니다.");
                }
            } else {
                logger.warn("Invalid pathInfo for DELETE: {}", pathInfo);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Error processing DELETE request", e);
            sendErrorResponse(response, e.getMessage());
        }
    }

    private void getNearbyWifi(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String lat = request.getParameter("lat");
        String lnt = request.getParameter("lnt");
        logger.info("Getting nearby WiFi for coordinates: lat={}, lnt={}", lat, lnt);
        
        if (lat != null && !lat.isEmpty() && lnt != null && !lnt.isEmpty()) {
            try {
                double latValue = Double.parseDouble(lat);
                double lntValue = Double.parseDouble(lnt);
                
                // 위치 히스토리 저장
                try {
                    wifiService.insertHistory(latValue, lntValue);
                    logger.info("Successfully saved location history: lat={}, lnt={}", latValue, lntValue);
                } catch (Exception e) {
                    logger.error("Failed to save location history", e);
                }
                
                List<WifiInfo> wifiList = wifiService.getNearbyWifi(latValue, lntValue);
                request.setAttribute("wifiList", wifiList);
                request.setAttribute("lat", latValue);
                request.setAttribute("lnt", lntValue);
                logger.info("Found {} nearby WiFi spots", wifiList.size());
                
            } catch (NumberFormatException e) {
                logger.error("Invalid coordinate format", e);
                throw new IllegalArgumentException("잘못된 좌표 형식입니다.");
            }
        }
        
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    private void getHistory(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        logger.info("Starting getHistory method");
        try {
            List<History> historyList = wifiService.getHistoryList();
            logger.info("History list size: {}", historyList.size());
            if (!historyList.isEmpty()) {
                logger.info("First history item: {}", historyList.get(0));
            }
            request.setAttribute("historyList", historyList);
            request.getRequestDispatcher("/history.jsp").forward(request, response);
            logger.info("Successfully forwarded to history.jsp");
        } catch (Exception e) {
            logger.error("Error retrieving history", e);
            throw new RuntimeException("히스토리 조회 실패", e);
        }
    }

    private void loadWifiData(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int count = wifiService.loadWifiData();
            sendJsonResponse(response, "{\"count\":" + count + "}");
            logger.info("Successfully loaded {} WiFi records", count);
        } catch (Exception e) {
            logger.error("Error loading WiFi data", e);
            throw new RuntimeException("와이파이 데이터 로드 실패", e);
        }
    }

    private void saveHistory(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String lat = request.getParameter("lat");
        String lnt = request.getParameter("lnt");
        
        if (lat == null || lnt == null || lat.trim().isEmpty() || lnt.trim().isEmpty()) {
            throw new IllegalArgumentException("위도와 경도가 필요합니다.");
        }

        try {
            double latValue = Double.parseDouble(lat);
            double lntValue = Double.parseDouble(lnt);
            wifiService.insertHistory(latValue, lntValue);
            logger.info("Successfully saved history for coordinates: lat={}, lnt={}", latValue, lntValue);
            sendJsonResponse(response, "{\"result\":\"success\"}");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("잘못된 좌표 형식입니다.");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message) 
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }

    private void sendJsonResponse(HttpServletResponse response, String json) 
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }
}
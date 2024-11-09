package com.zerobase.wifi.dto;

public class WifiInfo {
    private String mgrNo;        // X_SWIFI_MGR_NO
    private String mainNm;       // X_SWIFI_MAIN_NM
    private String address;      // X_SWIFI_ADRES1
    private double lat;         // LAT
    private double lnt;         // LNT
    private String workDttm;    // WORK_DTTM
    private double distance;    // 거리계산
    
    // Getters
    public String getMgrNo() { return mgrNo; }
    public String getMainNm() { return mainNm; }
    public String getAddress() { return address; }
    public double getLat() { return lat; }
    public double getLnt() { return lnt; }
    public String getWorkDttm() { return workDttm; }
    public double getDistance() { return distance; }
    
    // Setters
    public void setMgrNo(String mgrNo) { this.mgrNo = mgrNo; }
    public void setMainNm(String mainNm) { this.mainNm = mainNm; }
    public void setAddress(String address) { this.address = address; }
    public void setLat(double lat) { this.lat = lat; }
    public void setLnt(double lnt) { this.lnt = lnt; }
    public void setWorkDttm(String workDttm) { this.workDttm = workDttm; }
    public void setDistance(double distance) { this.distance = distance; }
}
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>와이파이 정보 구하기</title>
    <style>
        table {
            width: 100%;
            border-collapse: collapse;
        }
        th, td {
            border: 1px solid #ddd;
            padding: 8px;
            text-align: left;
        }
        th {
            background-color: #04AA6D;
            color: white;
        }
        tr:nth-child(even) {
            background-color: #f2f2f2;
        }
        button {
            margin: 5px;
            padding: 5px 10px;
        }
    </style>
</head>
<body>
    <h1>와이파이 정보 구하기</h1>
    
    <div>
        <a href="${pageContext.request.contextPath}/">홈</a> |
        <a href="${pageContext.request.contextPath}/history.jsp">위치 히스토리 목록</a> |
        <a href="javascript:void(0);" onclick="loadWifiData()">Open API 와이파이 정보 가져오기</a>
    </div>
    
    <div style="margin: 20px 0;">
        LAT: <input type="text" id="lat" value="0">, 
        LNT: <input type="text" id="lnt" value="0">
        <button onclick="getLocation()">내 위치 가져오기</button>
        <button onclick="getWifiInfo()">근처 WIFI 정보 보기</button>
    </div>
    
<table>
    <thead>
        <tr>
            <th>거리(Km)</th>
            <th>관리번호</th>
            <th>와이파이명</th>
            <th>도로명주소</th>
            <th>X좌표</th>
            <th>Y좌표</th>
            <th>작업일자</th>
        </tr>
    </thead>
    <tbody>
        <c:choose>
            <c:when test="${empty wifiList}">
                <tr>
                    <td colspan="7" style="text-align: center;">위치 정보를 입력한 후에 조회해 주세요.</td>
                </tr>
            </c:when>
            <c:otherwise>
                <c:forEach var="wifi" items="${wifiList}">
                    <tr>
                        <td>${wifi.distance}</td>
                        <td>${wifi.mgrNo}</td>
                        <td>${wifi.mainNm}</td>
                        <td>${wifi.address}</td>
                        <td>${wifi.lat}</td>
                        <td>${wifi.lnt}</td>
                        <td>${wifi.workDttm}</td>
                    </tr>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </tbody>
</table>

    <script>
    function getLocation() {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(showPosition);
        } else {
            alert("이 브라우저에서는 위치 정보를 가져올 수 없습니다.");
        }
    }

    function showPosition(position) {
        document.getElementById('lat').value = position.coords.latitude;
        document.getElementById('lnt').value = position.coords.longitude;
    }

    function getWifiInfo() {
        var lat = document.getElementById('lat').value;
        var lnt = document.getElementById('lnt').value;
        
        if(!lat || !lnt || lat === "0" || lnt === "0") {
            alert('위도와 경도를 입력해주세요.');
            return;
        }
        
        // URL에 context path 추가
        const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf("/",2));
        window.location.href = contextPath + '/wifi/nearby?lat=' + lat + '&lnt=' + lnt;
    }
    
    function loadWifiData() {
        fetch('${pageContext.request.contextPath}/wifi-project/wifi/load', {
            method: 'POST'
        })
        .then(response => response.json())
        .then(data => {
            alert(data.count + '개의 와이파이 정보를 가져왔습니다.');
            window.location.reload();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('와이파이 정보 가져오기 실패');
        });
    }
    
    function loadWifiData() {
        fetch('${pageContext.request.contextPath}/wifi/load', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            alert(data.count + '개의 와이파이 정보를 가져왔습니다.');
            window.location.reload();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('와이파이 정보 가져오기에 실패했습니다.');
        });
    }
    </script>
</body>
</html>
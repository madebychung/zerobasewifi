<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>위치 히스토리 목록</title>
    <style>
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        th, td {
            border: 1px solid #ddd;
            padding: 8px;
            text-align: center;
        }
        th {
            background-color: #f2f2f2;
        }
        tr:hover {
            background-color: #f5f5f5;
        }
        .btn-delete {
            background-color: #ff4444;
            color: white;
            border: none;
            padding: 5px 10px;
            cursor: pointer;
            border-radius: 3px;
        }
        .btn-delete:hover {
            background-color: #cc0000;
        }
        .no-records {
            text-align: center;
            padding: 20px;
            color: #666;
        }
        /* 네비게이션 스타일 추가 */
        .navigation {
            margin: 20px 0;
        }
        .navigation a {
            text-decoration: none;
            color: #0077cc;
            margin-right: 20px;
        }
        .navigation a:hover {
            text-decoration: underline;
        }
        /* 메인 컨테이너 스타일 */
        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>위치 히스토리 목록</h1>
        
        <div class="navigation">
            <a href="${pageContext.request.contextPath}/wifi-project/wifi/nearby">홈</a> |
            <a href="${pageContext.request.contextPath}/history.jsp">위치 히스토리 목록</a> |
            <a href="javascript:void(0);" onclick="loadWifiData()">Open API 와이파이 정보 가져오기</a>
        </div>

        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>X좌표</th>
                    <th>Y좌표</th>
                    <th>조회일자</th>
                    <th>비고</th>
                </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${empty historyList}">
                    <%
                        response.sendRedirect(request.getContextPath() + "/wifi-project/wifi/history");
                    %>
                    <tr>
                        <td colspan="5" class="no-records">위치 히스토리가 없습니다.</td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="history" items="${historyList}">
                        <tr>
                            <td>${history.id}</td>
                            <td>${history.lat}</td>
                            <td>${history.lnt}</td>
                            <td>${history.searchDttm}</td>
                            <td>
                                <button class="btn-delete" onclick="deleteHistory(${history.id})">삭제</button>
                            </td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </div>

    <script>
        function deleteHistory(id) {
            if (confirm('정말로 삭제하시겠습니까?')) {
                fetch(`${pageContext.request.contextPath}/wifi-project/wifi/history?id=${id}`, {
                    method: 'DELETE'
                })
                .then(response => response.json())
                .then(data => {
                    if (data.result === 'success') {
                        alert('삭제되었습니다.');
                        location.reload();
                    } else {
                        alert('삭제 실패: ' + (data.error || '알 수 없는 오류가 발생했습니다.'));
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('삭제 중 오류가 발생했습니다.');
                });
            }
        }

        function loadWifiData() {
            fetch('${pageContext.request.contextPath}/wifi-project/wifi/load-wifi')
                .then(response => response.json())
                .then(data => {
                    if (data.count) {
                        alert(data.count + '개의 WIFI 정보를 정상적으로 저장하였습니다.');
                        location.reload();
                    } else {
                        alert('WIFI 정보 저장에 실패했습니다.');
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('WIFI 정보 가져오기에 실패했습니다.');
                });
        }
    </script>
</body>
</html>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>와이파이 정보 가져오기</title>
    <style>
        .center {
            text-align: center;
            margin-top: 50px;
        }
    </style>
</head>
<body>
    <div class="center">
        <h1>와이파이 정보 가져오기</h1>
        <div id="loading">데이터를 가져오는 중입니다...</div>
        <div id="result" style="display: none;"></div>
        <a href="index.jsp" id="homeLink" style="display: none;">홈으로 가기</a>
    </div>

    <script>
        window.onload = function() {
            fetch('/wifi-project/wifi/load', {
                method: 'POST'
            })
            .then(response => response.text())
            .then(count => {
                document.getElementById('loading').style.display = 'none';
                document.getElementById('result').style.display = 'block';
                document.getElementById('result').innerHTML = 
                    `<p>${count}개의 WIFI 정보를 정상적으로 저장하였습니다.</p>`;
                document.getElementById('homeLink').style.display = 'block';
            })
            .catch(error => {
                document.getElementById('loading').style.display = 'none';
                document.getElementById('result').innerHTML = 
                    '<p>WIFI 정보 가져오기에 실패하였습니다.</p>';
                document.getElementById('homeLink').style.display = 'block';
            });
        }
    </script>
</body>
</html>
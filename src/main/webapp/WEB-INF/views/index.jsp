<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, minimum-scale=1.0">
    <title>Chat Tupi | Spring Boot + WebSocket</title>
    <link rel="stylesheet" href="/css/main.css" />
</head>
<body background="maxresdefault.jpg"
      style="background-position: center; background-repeat: no-repeat; background-size: cover;">
<noscript>
    <h2>Opa! Parece que este browser não suporta JavaScript</h2>
</noscript>

<div id="username-page">
    <div class="username-page-container">
        <h1 class="title">닉네임을 입력해 주세요.</h1>
        <form id="usernameForm" name="usernameForm">
            <div class="form-group">
                <input type="text" id="name" placeholder="넥네임"
                       autocomplete="off" class="form-control" />
            </div>
            <div class="form-group">
                <button type="submit" class="accent username-submit">입장하기</button>
            </div>
        </form>
    </div>
</div>

<div id="chat-page" class="hidden">
    <div class="chat-container">
        <div class="chat-header">
            <h2>채팅방</h2>
        </div>
        <div class="connecting">채팅방 입장중...</div>
        <ul id="messageArea">

        </ul>
        <form id="messageForm" name="messageForm" nameForm="messageForm">
            <div class="form-group">
                <div class="input-group clearfix">
                    <input type="text" id="message" placeholder="메시지를 입력해 주세요..."
                           autocomplete="off" class="form-control" />
                    <button type="submit" class="primary">전송</button>
                </div>
            </div>
        </form>
    </div>
</div>

<script
        src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.1.4/sockjs.min.js"></script>
<script
        src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
<script src="/js/main.js"></script>
</body>
</html>
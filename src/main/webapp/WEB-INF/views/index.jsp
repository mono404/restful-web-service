<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, minimum-scale=1.0">
    <title>Spring Boot + WebSocket</title>
    <link rel="stylesheet" href="/css/main.css" />
</head>
<body background="maxresdefault.jpg"
      style="background-position: center; background-repeat: no-repeat; background-size: cover;">
<noscript>
    <h2>해당 브라우저는 자바스크립트를 지원하지 않습니다.</h2>
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

<script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.1.4/sockjs.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
<script src="/js/main.js"></script>
<script>
    var usernamePage = document.querySelector('#username-page');
    var chatPage = document.querySelector('#chat-page');
    var usernameForm = document.querySelector('#usernameForm');
    var messageForm = document.querySelector('#messageForm');
    var messageInput = document.querySelector('#message');
    var messageArea = document.querySelector('#messageArea');
    var connectingElement = document.querySelector('.connecting');

    var stompClient = null;
    var username = null;

    var colors = [
        '#2196F3', '#32c787', '#00BCD4', '#ff5652',
        '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
    ];

    function connect(event) {
        username = document.querySelector('#name').value.trim();

        if(username) {
            usernamePage.classList.add('hidden');
            chatPage.classList.remove('hidden');

            var socket = new SockJS('/websocket');
            stompClient = Stomp.over(socket);

            stompClient.connect({}, onConnected, onError);
        }
        event.preventDefault();
    }


    function onConnected() {
        // Subscribe to the Public Topic
        stompClient.subscribe('/topic/public', onMessageReceived);

        // Tell your username to the server
        stompClient.send("/app/chat.register",
            {},
            JSON.stringify({sender: username, type: 'JOIN'})
        )

        connectingElement.classList.add('hidden');
    }


    function onError(error) {
        connectingElement.textContent = '웹 소켓에 연결할 수 없습니다.';
        connectingElement.style.color = 'red';
    }


    function send(event) {
        var messageContent = messageInput.value.trim();

        if(messageContent && stompClient) {
            var chatMessage = {
                sender: username,
                content: messageInput.value,
                type: 'CHAT'
            };

            stompClient.send("/app/chat.send", {}, JSON.stringify(chatMessage));
            messageInput.value = '';
        }
        event.preventDefault();
    }


    function onMessageReceived(payload) {
        var message = JSON.parse(payload.body);

        var messageElement = document.createElement('li');

        if(message.type === 'JOIN') {
            messageElement.classList.add('event-message');
            message.content = message.sender + '님이 들어왔습니다.';
        } else if (message.type === 'LEAVE') {
            messageElement.classList.add('event-message');
            message.content = message.sender + '님이 나갔습니다.';
        } else {
            messageElement.classList.add('chat-message');

            var avatarElement = document.createElement('i');
            var avatarText = document.createTextNode(message.sender[0]);
            avatarElement.appendChild(avatarText);
            avatarElement.style['background-color'] = getAvatarColor(message.sender);

            messageElement.appendChild(avatarElement);

            var usernameElement = document.createElement('span');
            var usernameText = document.createTextNode(message.sender);
            usernameElement.appendChild(usernameText);
            messageElement.appendChild(usernameElement);
        }

        var textElement = document.createElement('p');
        var messageText = document.createTextNode(message.content);
        textElement.appendChild(messageText);

        messageElement.appendChild(textElement);

        messageArea.appendChild(messageElement);
        messageArea.scrollTop = messageArea.scrollHeight;
    }


    function getAvatarColor(messageSender) {
        var hash = 0;
        for (var i = 0; i < messageSender.length; i++) {
            hash = 31 * hash + messageSender.charCodeAt(i);
        }

        var index = Math.abs(hash % colors.length);
        return colors[index];
    }

    usernameForm.addEventListener('submit', connect, true)
    messageForm.addEventListener('submit', send, true)
</script>
</body>
</html>
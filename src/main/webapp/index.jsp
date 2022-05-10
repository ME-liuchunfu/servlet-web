<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>主页</title>
</head>
<body>

<script>

    //定义WebSocket 对象  new WebSocket () 协议是 ws 协议
    // 注意 url 的地址 ,暂时是固化了
    var webSocket = new WebSocket("ws://localhost:8080/servlet_web/websocket");

    //发送消息
    var sendMsg = function () {
        //获取元素
        var inputElement = document.getElementById("msg");
        //调用  webSocket.send(text) 方法，发送给服务器端消息
        webSocket.send(inputElement.value);
        //清空输入框
        inputElement.value = "";
    }

    //回车事件，发送消息
    var send = function (event) {
        if (event.keyCode == 13) {
            sendMsg();
        }
    }

    //退出按钮事件
    var closeWS = function () {
        //调用close 方法，进行关闭
        webSocket.close();

        //清空消息
        document.getElementById("show").innerHTML = "";
        //清空输入框内的消息
        document.getElementById("msg").value = "";
    }

    //webSocket.onopen 当连接时,绑定事件,避免出来未连接，就点击按钮.
    webSocket.onopen = function () {
        //回车事件
        document.getElementById('msg').onkeydown = send;
        //发送按钮事件
        document.getElementById("sendBn").onclick = sendMsg;
        //退出事件
        document.getElementById("closeBn").onclick = closeWS;
        console.log("WebSocket 连接成功!!");
    }
    //接收消息 onmessage
    webSocket.onmessage = function (event) {
        var show = document.getElementById("show");
        //event.data 用于获取消息   event.data 用于获取消息，并且拼装
        show.innerHTML += event.data + "<br/>";
        //滚动条处理
        show.scrollTop = show.scrollHeight;
    }
    //webSocket 关闭 onclose 事件
    webSocket.onclose = function () {
        //去掉事件
        document.getElementById("msg").onkeydown = null;
        document.getElementById("sendBn").onclick = null;
        //退出
        document.getElementById("closeBn").onclick = null;
        //提示已经被关闭了
        console.log("WebSocket 已经被关闭了!!");
    }


</script>

<!--展示的div -->
<div style="width:600px;height:400px; overflow-y:auto;border:1px solid #333;" id="show">
</div>
<br/>
<!-- 填入内容的框 -->
<input type="text" size="80" id="msg" name="msg" placeholder="请输入聊天内容">
<!-- 发送按钮框 -->
<input type="button" value="发送" id="sendBn" name="sendBn">
<br/>
<br/>
<!-- 退出登录按钮框 -->
<input type="button" value="退出登录" id="closeBn" name="closeBn">

</body>
</html>


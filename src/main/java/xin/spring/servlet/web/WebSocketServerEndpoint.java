package xin.spring.servlet.web;

import xin.spring.servlet.web.utils.IdWorker;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

@ServerEndpoint(value = "/websocket")
public class WebSocketServerEndpoint {

    //用于构建用户名称，是前缀
    private static final String GUEST_PREFIX="访客";

    //定义后面的名称，是个不重复的索引值，从0开始。  AtomicInteger AtomicInteger
    private static final AtomicInteger connectionIds=new AtomicInteger(0);

    //定义Set 集合，用于存放客户端连接， 不重复
    private static final Set<WebSocketServerEndpoint> clientSet=new CopyOnWriteArraySet<>();

    //显示的昵称
    private String nickName;

    //session 对象，用于接收
    private Session session;

    //构造方法，初始化 每一个 ChatEntPoint 对象的  nickName,构建成 访问+数字的形式
    public WebSocketServerEndpoint(){
        //后面生成一个不重复的数字
        nickName=GUEST_PREFIX+ IdWorker.defaultInstance().nextId();
    }

    // OnOpen ，表示每一个客户端连接时的事件
    @OnOpen
    public void start(Session session){
        //单独接收一个客户端与服务器端的Session
        this.session=session;
        //添加到这里面
        clientSet.add(this);

        //设置消息
        String message=String.format("【%s %s 】",nickName,"加入聊天室");

        //用于发送消息
        broadcast(message);

    }
    //OnClose 注解， 每一个关闭时的事件
    @OnClose
    public void end(){
        //移除掉
        clientSet.remove(this);
        //格式化消息
        String message=String.format("【%s %s】",nickName,"离开聊天室");
        //发送消息
        broadcast(message);
    }
    //OnMessage, 接收客户端发送过来的消息的事件  OnMessage OnMessage
    @OnMessage
    public void incoming(String message){
        String filteredMessage=String.format("【%s:%s】",nickName,filter(message));
        //发送消息
        broadcast(filteredMessage);
    }
    // 服务器端错误时的，事件处理
    @OnError
    public void onError(Throwable t) throws Throwable{

        System.out.println("WebScoket 服务端错误"+t.getMessage());
    }
    //发送消息的方法
    private static void broadcast(String message){
        //遍历每一个客户端
        for(WebSocketServerEndpoint client:clientSet){
            try{
                //同步操作
                synchronized(client){
                    //通过 session.getBasicRemote() .sendText() 发送消息  getBasicRemote(). sendText()
                    client.session.getBasicRemote().sendText(message);
                }
            }catch(IOException e){
                System.out.println("聊天错误,向客户端 "+client+"发送消息出现错误 ");
                //移除这个不存在的客户端
                clientSet.remove(client);

                try{
                    //关闭这个session
                    client.session.close();
                }catch(IOException e2){
                    e2.printStackTrace();
                }
                //发送消息， 也检测一下，是否还有其他死客户端
                String msg=String.format("【%s %s 】",client.nickName,"已经断开连接");
                broadcast(msg);
            }
        }
    }
    //格式化前端传递过来的消息
    private static String filter(String message){
        if(null==message){
            return null;
        }
        //定义数据
        char[] content=new char[message.length()];

        //往 char 数据里面放置数据
        message.getChars(0,message.length(), content, 0);

        //判断后，进行转换
        StringBuilder result=new StringBuilder(content.length+50);

        for(int i=0;i<content.length;i++){
            switch(content[i]){
                case '<':{
                    result.append("&lt;");
                    break;
                }
                case '>':{
                    result.append("&gt;");
                    break;
                }
                case '&':{
                    result.append("&amp;");
                    break;
                }
                case '"':{
                    result.append("&quot;");
                    break;
                }
                default:{
                    result.append(content[i]);
                }
            }
        }
        return result.toString();
    }

}

package com.example.websocket.websocket;

import com.example.websocket.vo.ChatUser;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
@Component
@ServerEndpoint(value = "/chat/{userId}", encoders = {ChatEncoder.class}, decoders = {ChatDecoder.class})
public class ChatEndPoint {
    private Session session;
    private static Set<ChatEndPoint> users = new CopyOnWriteArraySet<>();
    private static int onlineCount = 0;

    @OnOpen
    public void chatOpen(Session session, @PathParam(value="userId") String userId) {
        this.session = session;
        users.add(this);
        onlineCount++;
        ChatUser chatUser = new ChatUser();
        chatUser.setUserId(userId);
        chatUser.setMessage(userId + "님이 입장하셨습니다.");
        broadcast(chatUser);
        System.out.println("chat opened - user : " + userId + "entered");
        System.out.println("currently online user number : " + onlineCount );
    }

    @OnMessage
    public void chatMessage(ChatUser chatUser,  @PathParam(value="userId") String userId) {
        System.out.println(chatUser.getUserId() + " : " + chatUser.getMessage());
        broadcast(chatUser);

    }

    @OnClose
    public void chatClose(Session session, @PathParam(value="userId") String userId) {
        onlineCount--;
        users.remove(this);
        System.out.println("chat close - user :" + userId + "leaved");
    }

    @OnError
    public void chatError (Session session, Throwable throwable, @PathParam(value="userId") String userId) {
        users.remove(this);
        onlineCount--;
        System.out.println("chat close - user :" + session.getId() + "leaved by coummunication error");
     }

    public void broadcast(ChatUser chatUser) {
        try {
            for (ChatEndPoint user : users) {
                user.session.getBasicRemote().sendObject(chatUser);
            }
        } catch (Exception e) {
            System.out.println("failed to broadcast");
        }

    }

}

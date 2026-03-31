package com.example.wx.WebSocket;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/webSocket/{token}")
public class WebSocket {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // 在线用户
    private static final Map<String, Session> userSessions = new ConcurrentHashMap<>();

    private String userId;     // 从 token 解析
    private String role;       // 暂时只处理 user
    private String convId;     // 一个用户一条会话

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        // 解析 token
        Map<String, String> info = parseToken(token);
        this.userId = info.get("userName");
        this.role = info.get("role");
        System.out.println("连接 token=" + token + " userId=" + userId + " role=" + role);

        userSessions.put(userId, session);
        convId = "c_" + userId;

        // 下发初始化信息
        Map<String, Object> payload = new HashMap<>();
        payload.put("convId", convId);
        sendSystem(session, "init_ok", payload);

        // 机器人欢迎
        botReply(session, convId, "你好，我是Panbotica自助客服，如需退款服务, 请输入1;");
    }

    @OnMessage
    public void onMessage(Session session, String message) throws Exception {
        Map<String, Object> msg = MAPPER.readValue(message, Map.class);
        String type = (String) msg.get("type");

        switch (type) {
            case "ping": {
                Map<String, Object> pongMsg = new HashMap<>();
                pongMsg.put("type", "pong");
                send(session, pongMsg);
                break;
            }

            case "chat": {
                String convId = (String) msg.get("convId");
                Map payload = (Map) msg.get("payload");
                String text = (String) payload.get("text");

                // 全部交给机器人回复
                botReply(session, convId, replyByBot(text));
                break;
            }

            case "end": {
                String convId = (String) msg.get("convId");
                closeConversation(convId);
                break;
            }

            default: {
                Map<String, Object> err = new HashMap<>();
                err.put("type", "error");
                Map<String, Object> errPayload = new HashMap<>();
                errPayload.put("text", "unknown type");
                err.put("payload", errPayload);
                send(session, err);
            }
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        userSessions.remove(userId);
    }

    @OnError
    public void onError(Session session, Throwable thr) { }

    // ===== 工具方法 =====

    private void closeConversation(String convId) {
        String uid = convId.substring(2);

        Session us = userSessions.get(uid);
        if (us != null) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("type", "end");
            msg.put("from", "system");
            msg.put("convId", convId);
            send(us, msg);
        }
    }

    private void botReply(Session s, String convId, String text) {
        forward(s, "bot_reply", "bot", convId, text);
    }

    private void forward(Session s, String type, String from, String convId, String text) {
        if (s == null) return;

        Map<String, Object> msg = new HashMap<>();
        msg.put("type", type);
        msg.put("from", from);
        msg.put("convId", convId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("text", text);
        msg.put("payload", payload);

        msg.put("ts", System.currentTimeMillis());
        send(s, msg);
    }

    private void sendSystem(Session s, String type, Map<String, Object> payload) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", type);
        msg.put("from", "system");
        msg.put("payload", payload);
        msg.put("ts", System.currentTimeMillis());
        send(s, msg);
    }

    private void send(Session s, Map<String, Object> obj) {
        try {
            s.getBasicRemote().sendText(MAPPER.writeValueAsString(obj));
        } catch (Exception ignore) {}
    }

    private String replyByBot(String text) {
        if (text.contains("退款") || text.contains("退钱") || text.contains("1")) {
            return "正在为您跳转退款页面...";
        }
        else if (text.contains("人工") || text.contains("客服") || text.contains("2")) {
            return "为您接入人工客服中...";
        }
        return "目前我只能回复退款的问题，如有其它问题请联系人工";
    }

    private Map<String, String> parseToken(String token) {
        Map<String, String> m = new HashMap<>();
        if (token == null) return m;
        for (String part : token.split(",")) {
            String[] kv = part.split(":");
            if (kv.length == 2) m.put(kv[0], kv[1]);
        }
        return m;
    }
}

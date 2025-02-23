package org.example.backend_1.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
import okhttp3.*;
import org.example.backend_1.pojo.JsonParse;
import org.example.backend_1.pojo.RoleContent;
import org.example.backend_1.pojo.SingleRecord;
import org.example.backend_1.pojo.Text;
import org.example.backend_1.service.ApiService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class BigModelStreamService {

    private static final String hostUrl = "https://spark-api.xf-yun.com/v3.1/chat";
    private static final String appid = "fb3346c2";
    private static final String apiSecret = "MTE5NWM0Yzc3MjNjNWMxM2JkOWU0YzQ4";
    private static final String apiKey = "9d76b857fc02d4f7e5116cb5d32a6cc7";
    private static final Gson gson = new Gson();
    public static List<RoleContent> historyList = new ArrayList<>();
    public static String totalAnswer = "";
    public static String NewQuestion = "";
    public static SingleRecord singleRecord;

    @Autowired
    private ApiService injectedApiService;

    public static ApiService apiService;

    @PostConstruct
    public void init() {
        apiService = injectedApiService;
    }

    public void askQuestionStream(SingleRecord record, List<RoleContent> history, SseEmitter emitter) throws Exception {
        System.out.println("askQuestionStream");
        singleRecord = record;
        historyList=history;
        totalAnswer="";
        BigModelWebSocketListener.wsClosed = false; // 重置 WebSocket 状态
        NewQuestion = singleRecord.getInput();

        String authUrl = getAuthUrl();
        OkHttpClient client = new OkHttpClient.Builder().build();
        String url = authUrl.replace("http://", "ws://").replace("https://", "wss://");
        Request request = new Request.Builder().url(url).build();

        WebSocketListener listener = new BigModelWebSocketListener(emitter);
        client.newWebSocket(request, listener);

        // 不再等待WebSocket关闭，响应逐步返回到客户端
    }



    private String getAuthUrl() throws Exception {
        System.out.println("getAuthUrl");
        URL url = new URL(BigModelStreamService.hostUrl);
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        String preStr = "host: " + url.getHost() + "\n" + "date: " + date + "\n" + "GET " + url.getPath() + " HTTP/1.1";
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(BigModelStreamService.apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);

        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        String sha = Base64.getEncoder().encodeToString(hexDigits);
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                BigModelStreamService.apiKey, "hmac-sha256", "host date request-line", sha);
        HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse("https://" + url.getHost() + url.getPath()))
                .newBuilder().addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8)))
                .addQueryParameter("date", date).addQueryParameter("host", url.getHost()).build();
        return httpUrl.toString();
    }

    static class BigModelWebSocketListener extends WebSocketListener {
        private static boolean wsClosed = false;
        private final SseEmitter emitter;

        public BigModelWebSocketListener(SseEmitter emitter) {
            this.emitter = emitter;
        }

        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            System.out.println("onOpen");
            sendRequest(webSocket);
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            System.out.println("onMessage");
            try {
                JsonParse myJsonParse = gson.fromJson(text, JsonParse.class);
                if (myJsonParse.header.code != 0) {
                    emitter.send(SseEmitter.event().name("error").data("Error code: " + myJsonParse.header.code));
                    webSocket.close(1000, "");
                } else {
                    List<Text> textList = myJsonParse.payload.getChoices().text;
                    for (Text temp : textList) {
                        totalAnswer += temp.getContent();
                        emitter.send(SseEmitter.event().name("update").data(temp.getContent()));
                    }
                    if (myJsonParse.header.getStatus() == 2) { // 流结束标志
                        wsClosed = true;
                        emitter.send(SseEmitter.event().name("done").data(totalAnswer));
                        singleRecord.setOutput(totalAnswer);
                        apiService.insertOnce(singleRecord);
                        emitter.complete();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(@NotNull WebSocket webSocket, Throwable t, Response response) {
            try {
                emitter.send(SseEmitter.event().name("error").data(t.getMessage()));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                emitter.complete();
            }
        }

//        public static boolean isWsClosed() {
//            return wsClosed;
//        }

        private void sendRequest(WebSocket webSocket) {
            try {
                System.out.println("sendRequest");
                JSONObject requestJson = new JSONObject();
                JSONObject header = new JSONObject();
                header.put("app_id", appid);
                header.put("uid", UUID.randomUUID().toString().substring(0, 10));

                JSONObject parameter = new JSONObject();
                JSONObject chat = new JSONObject();
                chat.put("domain", "generalv3");
                chat.put("temperature", 0.5);
                chat.put("max_tokens", 4096);
                parameter.put("chat", chat);

                JSONObject payload = new JSONObject();
                JSONObject message = new JSONObject();
                JSONArray text = new JSONArray();

                if (!historyList.isEmpty()) {
                    for (RoleContent temp : historyList) {
                        text.add(JSON.toJSON(temp));
                    }
                }

//                RoleContent roleContent = new RoleContent();
//                roleContent.setRole("user");
//                roleContent.setContent(NewQuestion);
//                text.add(JSON.toJSON(roleContent));
//                historyList.add(roleContent);

                message.put("text", text);
                payload.put("message", message);

                requestJson.put("header", header);
                requestJson.put("parameter", parameter);
                requestJson.put("payload", payload);

                webSocket.send(requestJson.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


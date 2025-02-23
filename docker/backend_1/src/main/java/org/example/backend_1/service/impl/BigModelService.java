package org.example.backend_1.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import lombok.Getter;
import okhttp3.*;
import org.example.backend_1.pojo.JsonParse;
import org.example.backend_1.pojo.RoleContent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import org.example.backend_1.pojo.Text;

@Service
public class BigModelService {
    private static final String hostUrl = "https://spark-api.xf-yun.com/v3.1/chat";
    private static final String appid = "fb3346c2";
    private static final String apiSecret = "MTE5NWM0Yzc3MjNjNWMxM2JkOWU0YzQ4";
    private static final String apiKey = "9d76b857fc02d4f7e5116cb5d32a6cc7";
    private static final Gson gson = new Gson();
    public static List<RoleContent> historyList = new ArrayList<>();
    public static String totalAnswer = "";
    public static String NewQuestion = "";

    public String askQuestion(String question, List<RoleContent> history) throws Exception {
        historyList = history;
        totalAnswer = "";
        System.out.println("askQuestion");
        BigModelWebSocketListener.wsClosed = false; // 重置 WebSocket 状态
        totalAnswer = ""; // 清空答案
        NewQuestion = question;

        String authUrl = getAuthUrl();
        OkHttpClient client = new OkHttpClient.Builder().build();
        String url = authUrl.replace("http://", "ws://").replace("https://", "wss://");
        Request request = new Request.Builder().url(url).build();

        WebSocketListener listener = new BigModelWebSocketListener();
        WebSocket webSocket = client.newWebSocket(request, listener);

        while (!BigModelWebSocketListener.isWsClosed()) {
            Thread.sleep(200);
        }
        System.out.println("本轮回答为：" + totalAnswer);
        return totalAnswer;
    }


    private String getAuthUrl() throws Exception {
        System.out.println("getAuthUrl");
        URL url = new URL(BigModelService.hostUrl);
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        String preStr = "host: " + url.getHost() + "\n" + "date: " + date + "\n" + "GET " + url.getPath() + " HTTP/1.1";
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(BigModelService.apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);

        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        String sha = Base64.getEncoder().encodeToString(hexDigits);
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                BigModelService.apiKey, "hmac-sha256", "host date request-line", sha);
        HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse("https://" + url.getHost() + url.getPath()))
                .newBuilder().addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8)))
                .addQueryParameter("date", date).addQueryParameter("host", url.getHost()).build();
        return httpUrl.toString();
    }

    static class BigModelWebSocketListener extends WebSocketListener {
        @Getter
        private static boolean wsClosed = false;

        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            System.out.println("onOpen");
            super.onOpen(webSocket, response);
            sendRequest(webSocket);
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            System.out.println("onMessage");
            JsonParse myJsonParse = gson.fromJson(text, JsonParse.class);
            if (myJsonParse.header.code != 0) {
                System.out.println("发生错误，错误码为：" + myJsonParse.header.code);
                webSocket.close(1000, "");
            }
            List<Text> textList = myJsonParse.payload.getChoices().text;
            for (Text temp : textList) {
                totalAnswer = totalAnswer + temp.getContent();
            }
            if (myJsonParse.header.getStatus() == 2) {
                wsClosed = true;
            }
        }

        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response response) {
            System.out.println("onFailure");
            super.onFailure(webSocket, t, response);
        }

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
//                roleContent.role = "user";
//                roleContent.content = NewQuestion;
//                text.add(JSON.toJSON(roleContent));
//                historyList.add(roleContent);

                message.put("text", text);
                payload.put("message", message);
                System.out.println(message.toString());
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

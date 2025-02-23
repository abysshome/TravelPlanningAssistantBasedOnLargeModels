package org.example.backend_1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import org.example.backend_1.pojo.*;
import org.example.backend_1.service.ApiService;
import org.example.backend_1.service.DialogService;
import org.example.backend_1.utils.UploadGit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
// sss
@RestController
@RequestMapping("dialog")
public class DialogController {
    @Autowired
    private DialogService dialogService;
    @Autowired
    private ApiService apiService;
    @DeleteMapping("")
    public Result deleteDialog(Integer dialogId){
        dialogService.deleteDialogById(dialogId);
        return Result.success("删除成功");
    }

    @PostMapping("")
    public Result addDialog(Dialog dialog, HttpServletRequest request){
        dialogService.addDialog(dialog);
        Integer dialogId=dialog.getId();
        apiService.insertOnce(new SingleRecord(null,"你好，请问你能帮到我什么吗？","欢迎来到TourGuide!我是您的私人旅程规划小助手，请您简要说明您本次的旅行需求，包括时间、地点和预期途径点个数，例如“我早上八点到北京南站，计划在北京游玩一天，晚上六点左右到达北京西站，途径3个景点即可。”让我们开始旅行吧!",
                dialogId,dialog.getUserId()));
        System.out.println(dialogId);
        Dialog resDialog=dialogService.getDialogByDialogId(dialogId);

        if(resDialog.getUserId()==null){
            return Result.error("创建失败,userId不正确");
        }
        return Result.success(new AddDialogReturn(resDialog.getId(),resDialog.getCreateTime(),resDialog.getFinalSolution()));
    }

    @GetMapping("")
    public Result getDialog(User user){
        ArrayList<Dialog> dialogs=dialogService.getDialog(user);
        return Result.success(dialogs);
    }

    @GetMapping("records")
    public Result getRecords(Dialog dialog){
        ArrayList<SingleRecord> records=dialogService.getRecords(dialog);
        return Result.success(records);
    }
    @GetMapping("setSolution")
    public Result setSolution(Integer id,Integer dialogId){
        StringBuilder response = new StringBuilder();
        // 创建请求的URL
        String urlString = "http://python_service:7780/get-plan/?userId=" + id + "&dialogId=" + dialogId;
        try {
            // 创建URL对象
            URL url = new URL(urlString);
            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // 设置请求方法为GET
            connection.setRequestMethod("GET");
            // 设置请求头（如果需要）
            connection.setRequestProperty("Accept", "application/json");
            // 获取响应代码
            int responseCode = connection.getResponseCode();
            // 检查响应代码
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 创建BufferedReader读取响应
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                String jsonString=response.toString();
                System.out.println(jsonString);
                jsonString = jsonString.replace("\\n", "\n")
                        .replace("\\\"", "\"");
                jsonString = jsonString.substring(1, jsonString.length() - 1);
                System.out.println(jsonString);
                ObjectMapper objectMapper = new ObjectMapper();
                RouteData routeData = objectMapper.readValue(jsonString, RouteData.class);
//                responseObject.setHtml(UploadGit.upload(responseObject.getHtml()));
                // 打印响应内容
                System.out.println("Response received:");
                System.out.println(response.toString());
//                dialogService.setSolution(id,dialogId,responseObject.getHtml());
                return Result.success(routeData);
            } else {
                System.out.println("Request failed, status code: " + responseCode);
            }
            // 断开连接
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("错误");
    }

    @GetMapping("/upload")
    public Result upload(String url) throws IOException, InterruptedException {
        return Result.success(UploadGit.upload(url));
    }
    @GetMapping("/html")
    public ResponseEntity<String> getHtmlFile(Integer dialogId) {
        try {
            // 指定本地HTML文件路径
            Path htmlFilePath = Paths.get("/root/WorkSpace/beijing_route_map_(dialogId)%d.html".formatted(dialogId));
            // 读取文件内容
            String htmlContent = Files.readString(htmlFilePath);
            // 返回HTML文件内容
            return new ResponseEntity<>(htmlContent, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

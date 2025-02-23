package org.example.backend_1.controller;

import org.example.backend_1.pojo.Result;
import org.example.backend_1.pojo.RoleContent;
import org.example.backend_1.pojo.SingleRecord;
import org.example.backend_1.service.ApiService;
import org.example.backend_1.service.impl.BigModelService;
import org.example.backend_1.service.impl.BigModelStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.management.relation.Role;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/spark")
public class SparkApi {
    @Autowired
    private ApiService apiService;
    @Autowired
    private BigModelService bigModelService;
    @Autowired
    private BigModelStreamService bigModelStreamService;

    @PostMapping("/ask")
    public Result askQuestion(SingleRecord singleRecord) {
        try {
            String question= singleRecord.getInput();
            String res = bigModelService.askQuestion(question,getMessages(singleRecord.getDialogId(),question));
            singleRecord.setOutput(res);
            apiService.insertOnce(singleRecord);
            return Result.success(singleRecord);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/askStream")
    public SseEmitter askQuestionStream(SingleRecord singleRecord) {
        SseEmitter emitter = new SseEmitter();
        try {
            String question = singleRecord.getInput();
            bigModelStreamService.askQuestionStream(singleRecord,getMessages(singleRecord.getDialogId(),question), emitter);

        } catch (Exception e) {
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data(e.getMessage()));
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                emitter.complete();
            }
        }
        return emitter;
    }

    public List<RoleContent> getMessages(Integer dialogId, String input){
        System.out.println(dialogId);
        List<RoleContent> historyList = new ArrayList<>();
        ArrayList<String> inputs=apiService.getInputsByDialogId(dialogId);
        ArrayList<String> outputs=apiService.getOutputsByDialogId(dialogId);
        List<Map<String, String>> messages = new ArrayList<>();

        for (int i = 0; i < inputs.size(); i++) {
            RoleContent roleContent1 = new RoleContent();
            roleContent1.setRole("user");
            roleContent1.setContent(inputs.get(i));
            historyList.add(roleContent1);

            RoleContent roleContent2 = new RoleContent();
            roleContent2.setRole("assistant");
            roleContent2.setContent(outputs.get(i));
            historyList.add(roleContent2);
//            Map<String,String> message1=new HashMap<>();
//            message1.put("role","user");
//            message1.put("content",inputs.get(i));
//            System.out.println(message1);
//            messages.add(message1);
//
//            Map<String,String> message2=new HashMap<>();
//            message2.put("role","assistant");
//            message2.put("content",outputs.get(i));
//            System.out.println(message2);
//            messages.add(message2);
        }
        RoleContent roleContent = new RoleContent();
        roleContent.setRole("user");
        roleContent.setContent(input);
        historyList.add(roleContent);
        return historyList;
    }
}

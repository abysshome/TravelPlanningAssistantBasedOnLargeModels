package org.example.backend_1.service.impl;

import org.example.backend_1.mapper.ApiMapper;
import org.example.backend_1.pojo.Dialog;
import org.example.backend_1.pojo.Message;
import org.example.backend_1.pojo.SingleRecord;
import org.example.backend_1.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class ApiServiceImpl implements ApiService {
    @Autowired
    private ApiMapper apiMapper;

    @Override
    public void insertOnce(SingleRecord singleRecord) {
        apiMapper.insertOnce(singleRecord);
    }

    @Override
    public ArrayList<String> getInputsByDialogId(Integer dialogId) {
        return apiMapper.getInputsByDialogId(dialogId);
    }

    @Override
    public ArrayList<String> getOutputsByDialogId(Integer dialogId) {
        return apiMapper.getOutputsByDialogId(dialogId);
    }
}

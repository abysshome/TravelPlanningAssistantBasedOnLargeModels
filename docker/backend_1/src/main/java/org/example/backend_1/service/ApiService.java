package org.example.backend_1.service;

import org.example.backend_1.pojo.Dialog;
import org.example.backend_1.pojo.Message;
import org.example.backend_1.pojo.SingleRecord;

import java.util.ArrayList;

public interface ApiService {
    void insertOnce(SingleRecord singleRecord);

    ArrayList<String> getInputsByDialogId(Integer dialogId);

    ArrayList<String> getOutputsByDialogId(Integer dialogId);
}

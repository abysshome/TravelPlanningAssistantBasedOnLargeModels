package org.example.backend_1.service;

import org.example.backend_1.pojo.Dialog;
import org.example.backend_1.pojo.SingleRecord;
import org.example.backend_1.pojo.User;

import java.util.ArrayList;
import java.util.Arrays;

public interface DialogService {

    void addDialog(Dialog dialog);

    ArrayList<Dialog> getDialog(User user);

    ArrayList<SingleRecord> getRecords(Dialog dialog);

    Dialog getDialogByDialogId(Integer dialogId);

    void deleteDialogById(Integer dialogId);

    void setSolution(Integer id, Integer dialogId, String html);
}

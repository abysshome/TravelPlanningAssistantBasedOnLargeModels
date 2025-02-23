package org.example.backend_1.service.impl;

import org.example.backend_1.mapper.DialogMapper;
import org.example.backend_1.pojo.Dialog;
import org.example.backend_1.pojo.SingleRecord;
import org.example.backend_1.pojo.User;
import org.example.backend_1.service.DialogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class DialogServiceImpl implements DialogService {
    @Autowired
    private DialogMapper dialogMapper;

    @Override
    public void addDialog(Dialog dialog) {
        dialogMapper.addDialog(dialog);
    }

    @Override
    public ArrayList<Dialog> getDialog(User user) {
        return dialogMapper.getDialog(user);
    }

    @Override
    public ArrayList<SingleRecord> getRecords(Dialog dialog) {
        return dialogMapper.getRecords(dialog);
    }

    @Override
    public Dialog getDialogByDialogId(Integer dialogId) {
        return dialogMapper.getDialogByDialogId(dialogId);
    }

    @Override
    public void deleteDialogById(Integer dialogId) {
        dialogMapper.deleteDialogById(dialogId);
    }

    @Override
    public void setSolution(Integer id, Integer dialogId, String html) {
        dialogMapper.setSolution(id,dialogId,html);
    }
}

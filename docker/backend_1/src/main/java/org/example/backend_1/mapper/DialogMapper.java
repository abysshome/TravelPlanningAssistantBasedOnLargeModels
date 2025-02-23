package org.example.backend_1.mapper;

import org.apache.ibatis.annotations.*;
import org.example.backend_1.pojo.Dialog;
import org.example.backend_1.pojo.SingleRecord;
import org.example.backend_1.pojo.User;

import java.util.ArrayList;

@Mapper
public interface DialogMapper {
    @Insert("insert 苍穹杯.dialog (create_time, final_solution, user_id, name) values (now(),#{finalSolution},#{userId},#{name});")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void addDialog(Dialog dialog);

    @Select("SELECT * From 苍穹杯.dialog where user_id=#{id};")
    ArrayList<Dialog> getDialog(User user);

    @Select("SELECT * From 苍穹杯.singlerecords where dialog_id=#{id};")
    ArrayList<SingleRecord> getRecords(Dialog dialog);

    @Select("SELECT * FROM 苍穹杯.dialog where id=#{dialogId};")
    Dialog getDialogByDialogId(Integer dialogId);

    @Delete("DELETE FROM 苍穹杯.dialog where id=#{dialogId};")
    void deleteDialogById(Integer dialogId);

    @Update("update 苍穹杯.dialog set final_solution=#{html} where id=#{dialogId};")
    void setSolution(Integer id, Integer dialogId, String html);
}

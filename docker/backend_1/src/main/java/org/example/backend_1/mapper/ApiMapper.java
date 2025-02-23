package org.example.backend_1.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.example.backend_1.pojo.Dialog;
import org.example.backend_1.pojo.Message;
import org.example.backend_1.pojo.SingleRecord;
import java.util.ArrayList;

@Mapper
public interface ApiMapper {
    @Insert("insert 苍穹杯.singlerecords (input, output, dialog_id, user_id) VALUES (#{input},#{output},#{dialogId},#{userId});")
    @Options(useGeneratedKeys = true, keyProperty = "id") // 自动获取生成的主键
    void insertOnce(SingleRecord singleRecord);

    @Insert("insert 苍穹杯.dialog (create_time, final_solution, user_id, name) values (now(),#{finalSolution},#{userId},#{name});")
    void addDialog(Dialog dialog);

    @Select("SELECT input FROM 苍穹杯.singlerecords where dialog_id=#{dialogId};")
    ArrayList<String> getInputsByDialogId(Integer dialogId);

    @Select("SELECT output FROM 苍穹杯.singlerecords where dialog_id=#{dialogId};")
    ArrayList<String> getOutputsByDialogId(Integer dialogId);
}

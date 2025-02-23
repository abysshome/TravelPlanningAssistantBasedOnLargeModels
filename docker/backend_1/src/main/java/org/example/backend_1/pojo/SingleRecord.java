package org.example.backend_1.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SingleRecord {
    private Integer id;
    private String input;
    private String output;
    private Integer dialogId;
    private Integer userId;

    public SingleRecord(String input, String output, Integer dialogId, Integer userId) {
        this.input = input;
        this.output = output;
        this.dialogId = dialogId;
        this.userId = userId;
    }
}

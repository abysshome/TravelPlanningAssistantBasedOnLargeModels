package org.example.backend_1.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dialog {
    private Integer id;
    private String name;
    private Timestamp createTime;
    private String finalSolution;
    private Integer userId;
    public Dialog(String name,  Integer userId, String finalSolution) {
        this.name = name;
        this.finalSolution = finalSolution;
        this.userId = userId;
    }
}

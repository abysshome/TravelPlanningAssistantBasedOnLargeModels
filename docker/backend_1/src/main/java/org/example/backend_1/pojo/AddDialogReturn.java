package org.example.backend_1.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddDialogReturn {
    private Integer id;
    private Timestamp createTime;
    private String finalSolution;
}
